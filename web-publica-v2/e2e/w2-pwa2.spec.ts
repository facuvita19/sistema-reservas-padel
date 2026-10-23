import { test, expect } from '@playwright/test';

const pngSize = (buffer: Buffer) => ({
  width: buffer.readUInt32BE(16),
  height: buffer.readUInt32BE(20),
});

const compact = (source: string) => source.replace(/\s+/g, '');

test('PWA2 publica iconos instalables exactos y maskable', async ({ request }) => {
  const manifest = await (await request.get('/w2/manifest.webmanifest')).json();

  expect(manifest.icons).toEqual(expect.arrayContaining([
    expect.objectContaining({ src: '/w2/icons/icon-192.png', sizes: '192x192', purpose: 'any' }),
    expect.objectContaining({ src: '/w2/icons/icon-512.png', sizes: '512x512', purpose: 'any' }),
    expect.objectContaining({ src: '/w2/icons/icon-maskable-512.png', sizes: '512x512', purpose: 'maskable' }),
  ]));

  for (const [src, size] of [
    ['/w2/icons/icon-192.png', 192],
    ['/w2/icons/icon-512.png', 512],
    ['/w2/icons/icon-maskable-512.png', 512],
  ] as const) {
    const response = await request.get(src);
    expect(response.ok()).toBeTruthy();
    expect(pngSize(await response.body())).toEqual({ width: size, height: size });
  }
});

test('PWA2 anuncia desconexion y recuperacion de forma accesible', async ({ page }) => {
  await page.goto('/w2/');
  const status = page.locator('#w2PwaStatus');

  await page.evaluate(() => dispatchEvent(new Event('offline')));
  await expect(status).toBeVisible();
  await expect(status).toHaveAttribute('role', 'alert');
  await expect(status).toContainText('sin conexión');

  await page.evaluate(() => dispatchEvent(new Event('online')));
  await expect(status).toHaveAttribute('role', 'status');
  await expect(status).toContainText('Volviste a estar en línea');
});

test('PWA2 permite activar una version nueva de forma controlada', async ({ request }) => {
  const sw = compact(await (await request.get('/w2/sw.js')).text());
  const registration = compact(await (await request.get('/w2/pwa-register.js')).text());

  expect(sw).not.toContain('.then(()=>self.skipWaiting())');
  expect(sw).toContain("event.data?.type==='SKIP_WAITING'");
  expect(registration).toContain("waiting?.postMessage({type:'SKIP_WAITING'});");
  expect(registration).toContain('registration.update()');
});

test('PWA2 responde con fallback real durante navegacion offline', async ({ page, context }) => {
  await page.goto('/w2/');
  await page.waitForFunction(() => navigator.serviceWorker?.ready);
  await expect.poll(() => page.evaluate(() => Boolean(navigator.serviceWorker.controller))).toBeTruthy();

  await context.setOffline(true);
  try {
    await page.goto('/w2/sin-red-pwa2', { waitUntil: 'domcontentloaded' });
    await expect(page.getByRole('heading', { name: 'Estás sin conexión' })).toBeVisible();
  } finally {
    await context.setOffline(false);
  }
});

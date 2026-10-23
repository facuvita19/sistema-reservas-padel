import { test, expect } from '@playwright/test';

const compact = (source: string) => source.replace(/\s+/g, '');

test('PWA3 expone instalacion guiada y modo standalone', async ({ page, request }) => {
  await page.goto('/w2/');
  const source = compact(await (await request.get('/w2/pwa-register.js')).text());

  for (const contract of [
    'beforeinstallprompt',
    'appinstalled',
    "matchMedia('(display-mode:standalone)')",
    'navigator.standalone',
    "updateViaCache:'none'",
  ]) {
    expect(source).toContain(contract);
  }

  await page.evaluate(() => dispatchEvent(new Event('appinstalled')));
  await expect(page.locator('#w2PwaStatus')).toBeVisible();
  await expect(page.locator('#w2PwaStatus')).toContainText('quedó instalada');
  await expect(page.locator('html')).toHaveClass(/w2-standalone/);
});

test('PWA3 conserva acciones accesibles de instalacion', async ({ page }) => {
  await page.goto('/w2/');
  await expect(page.locator('#w2PwaInstall')).toHaveAttribute('type', 'button');
  await expect(page.locator('#w2PwaIos')).toHaveAttribute('type', 'button');
  await expect(page.locator('#w2PwaStatus')).toHaveAttribute('aria-live', 'polite');
  await expect(page.locator('#w2PwaStatus')).toHaveAttribute('aria-atomic', 'true');
});

test('PWA3 conserva contratos de actualizacion sin cache intermedia', async ({ request }) => {
  const source = compact(await (await request.get('/w2/pwa-register.js')).text());

  expect(source).toContain("updateViaCache:'none'");
  expect(source).toContain("waiting?.postMessage({type:'SKIP_WAITING'});");
});

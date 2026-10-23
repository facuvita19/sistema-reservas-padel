import { test, expect } from '@playwright/test';

test('Rendimiento R1 desacopla Torneos de global.css', async ({ request }) => {
  const response = await request.get('/w2/torneos/');
  expect(response.ok()).toBeTruthy();
  const html = await response.text();
  expect(html).not.toMatch(/\/_astro\/global\.[^"']+\.css/);
  expect(html).toMatch(/\/_astro\/torneos\.[^"']+\.css/);
});

test('Rendimiento R1 limita el presupuesto CSS de Torneos', async ({ page, request }) => {
  await page.goto('/w2/torneos/');
  const hrefs = await page.locator('link[rel="stylesheet"]').evaluateAll(nodes => nodes.map(node => (node as HTMLLinkElement).href));
  let total = 0;
  for (const href of hrefs) {
    const url = new URL(href);
    const response = await request.get(url.pathname + url.search);
    expect(response.ok()).toBeTruthy();
    total += (await response.body()).byteLength;
  }
  expect(total).toBeLessThan(380 * 1024);
});

test('Rendimiento R1 conserva modales de Torneos sin utilidades Tailwind heredadas', async ({ request }) => {
  const registration = await (await request.get('/w2/torneos/')).text();
  expect(registration).toContain('w2t-registration-overlay');
  expect(registration).toContain('w2t-bracket-overlay');
  for (const legacy of ['z-[80]', 'z-[85]', 'bg-black/78', 'bg-black/82', 'backdrop-blur-md', 'max-w-7xl']) {
    expect(registration).not.toContain(legacy);
  }
});

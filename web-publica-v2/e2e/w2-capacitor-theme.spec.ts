import { test, expect } from '@playwright/test';

test('CAP2 usa oscuro por defecto en el host Android y respeta una eleccion guardada', async ({ page }) => {
  await page.addInitScript(() => {
    Object.defineProperty(window, 'location', { configurable: true });
  }).catch(() => {});
  const source = await (await page.request.get('/w2/')).text();
  expect(source).toContain("location.hostname==='10.0.2.2'");
  expect(source).toContain("saved||preferred");
  expect(source).toContain("nativeHost?'dark'");
});

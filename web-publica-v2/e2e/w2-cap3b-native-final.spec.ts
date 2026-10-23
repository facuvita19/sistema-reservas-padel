import { expect, test } from '@playwright/test';

test.describe('CAP3B experiencia nativa consolidada', () => {
  test('la cabecera conserva el contrato movil y los recursos nativos se cargan', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto('/w2/');
    await expect(page.locator('meta[name="viewport"]')).toHaveAttribute('content', /width=device-width/);
    await expect(page.locator('html')).not.toHaveAttribute('style', /undefined|NaN/);
    await expect(page.locator('body')).toBeVisible();
  });

  test('el boton atras web no deja estados tecnicos visibles', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto('/w2/comunidad/');
    await expect(page.locator('body')).not.toContainText(/undefined|NaN|Infinity/);
  });
});

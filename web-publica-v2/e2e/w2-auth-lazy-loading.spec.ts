import { test, expect } from '@playwright/test';
import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();

test('8V separa nucleo global y modal lazy', () => {
  const core = fs.readFileSync(path.join(root, 'src/scripts/autenticacion.ts'), 'utf8');
  const modal = fs.readFileSync(path.join(root, 'src/scripts/autenticacion-modal.ts'), 'utf8');

  expect(core).toContain("import('./autenticacion-modal')");
  expect(core).toContain('let modalPromise');
  expect(core).not.toContain('formRegistro');
  expect(modal).toContain('formRegistro');
  expect(modal).toContain('formRecuperacion');
});

test('8V no descarga modal antes de interactuar y lo descarga con el primer clic', async ({ page }) => {
  const requests: string[] = [];
  page.on('request', request => requests.push(new URL(request.url()).pathname));

  await page.goto('/w2/', { waitUntil: 'networkidle' });
  expect(requests.some(pathname => pathname.includes('autenticacion-modal'))).toBeFalsy();

  const mobile = (page.viewportSize()?.width ?? 1280) <= 900;
  if (mobile) {
    const menuButton = page.locator('[data-w2-menu]');
    const mobileLogin = page.locator('#abrirAutenticacionMovil');

    await menuButton.click();
    await expect(page.locator('#w2Menu')).toHaveAttribute('aria-hidden', 'false');
    await expect(mobileLogin).toBeVisible();
    await mobileLogin.click();
  } else {
    const desktopLogin = page.locator('#abrirAutenticacion');
    await expect(desktopLogin).toBeVisible();
    await desktopLogin.click();
  }

  await expect(page.locator('#modalAutenticacion')).not.toHaveClass(/hidden/);
  await expect.poll(() => requests.some(pathname => pathname.includes('autenticacion-modal'))).toBeTruthy();
});

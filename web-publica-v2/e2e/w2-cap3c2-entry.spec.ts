import { test, expect, type Page } from '@playwright/test';

async function simularWebViewNativo(page: Page) {
  await page.addInitScript(() => {
    const userAgentOriginal = navigator.userAgent;

    Object.defineProperty(
      navigator,
      'userAgent',
      {
        configurable: true,
        get: () => userAgentOriginal + '; wv)'
      }
    );

    sessionStorage.setItem(
      'w2.native.guest.runtime',
      '1'
    );
  });
}

test(
  'la web ordinaria no bloquea la pintura con bienvenida nativa',
  async ({ page }) => {
    await page.goto('/w2/');

    await expect(
      page.locator('#w2NativeWelcome')
    ).toHaveCount(0);
  }
);

test(
  'el acceso nativo ofrece mantener la sesion sin enfocar campos',
  async ({ page }) => {
    await simularWebViewNativo(page);
    await page.goto('/w2/');

    await page.evaluate(() => {
      document
        .getElementById('abrirAutenticacion')
        ?.click();
    });

    const modal =
      page.locator('#modalAutenticacion');

    await expect(modal).not.toHaveClass(/hidden/);

    const mantenerSesion =
      page.locator('#mantenerSesion');

    await expect(mantenerSesion).toHaveCount(1);
    await expect(mantenerSesion).not.toBeChecked();

    await expect(
      page.locator('#loginEmail')
    ).not.toBeFocused();

    await page.locator('#tabRegistro').click();

    await expect(
      page.locator('#tabRegistro')
    ).toHaveAttribute('aria-selected', 'true');

    await expect(
      page.locator('#registroNombre')
    ).not.toBeFocused();
  }
);

import { test, expect, type Page } from '@playwright/test';

async function simularAplicacionNativa(page: Page) {
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
  });
}

async function responderSesion(
  page: Page,
  autenticado: boolean
) {
  await page.route(
    '**/api/publica/auth/sesion',
    route => route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(
        autenticado
          ? {
              autenticado: true,
              perfil: {
                clienteId: 1,
                nombre: 'Prueba',
                apellido: 'W2'
              }
            }
          : {
              autenticado: false
            }
      )
    })
  );
}

test(
  'la web ordinaria no muestra bienvenida nativa',
  async ({ page }) => {
    await page.goto('/w2/');

    await expect(
      page.locator('#w2NativeWelcome')
    ).toHaveCount(0);
  }
);

test(
  'las acciones de bienvenida nativa quedan apiladas',
  async ({ page }) => {
    await simularAplicacionNativa(page);
    await responderSesion(page, false);
    await page.goto('/w2/');

    const bienvenida =
      page.locator('#w2NativeWelcome');

    const acciones =
      bienvenida.locator('.w2-native-welcome-actions');

    await expect(bienvenida).toBeVisible();
    await expect(acciones).toBeVisible();

    const botones = acciones.locator('button');

    await expect(botones).toHaveCount(2);

    const primero = await botones.nth(0).boundingBox();
    const segundo = await botones.nth(1).boundingBox();

    if (!primero || !segundo) {
      throw new Error(
        'No se pudo medir la geometría de las acciones nativas.'
      );
    }

    expect(
      segundo.y >= primero.y + primero.height
    ).toBeTruthy();

    expect(
      Math.abs(primero.width - segundo.width)
    ).toBeLessThanOrEqual(2);
  }
);

test(
  'invitado no reaparece durante la misma ejecucion',
  async ({ page }) => {
    await simularAplicacionNativa(page);
    await responderSesion(page, false);
    await page.goto('/w2/');

    await page
      .locator('.w2-native-welcome-guest')
      .click();

    await expect(
      page.locator('#w2NativeWelcome')
    ).toBeHidden();

    await page.goto('/w2/torneos/');

    await expect(
      page.locator('#w2NativeWelcome')
    ).toBeHidden();

    await expect(
      page.locator('html')
    ).toHaveClass(/w2-native-welcome-ready/);
  }
);

test(
  'sesion recordada no muestra bienvenida',
  async ({ page }) => {
    await simularAplicacionNativa(page);
    await responderSesion(page, true);
    await page.goto('/w2/');

    await expect(
      page.locator('html')
    ).toHaveClass(/w2-native-welcome-ready/);

    await expect(
      page.locator('html')
    ).not.toHaveClass(/w2-native-welcome-pending/);

    await expect(
      page.locator('#w2NativeWelcome')
    ).toBeHidden();
  }
);

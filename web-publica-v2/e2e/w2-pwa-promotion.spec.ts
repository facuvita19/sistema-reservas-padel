import { test, expect } from '@playwright/test';

test(
  'la promocion de instalacion PWA no aparece en escritorio',
  async ({ page }) => {
    await page.addInitScript(() => {
      window.addEventListener(
        'beforeinstallprompt',
        event => event.preventDefault()
      );
    });

    await page.goto('/w2/');

    await page.evaluate(() => {
      window.dispatchEvent(
        new Event('beforeinstallprompt')
      );
    });

    await expect(
      page.locator('#w2PwaStatus')
    ).toBeHidden();
  }
);

test(
  'la promocion PWA conserva avisos operativos',
  async ({ page }) => {
    await page.goto('/w2/');

    await page.evaluate(() => {
      window.dispatchEvent(new Event('offline'));
    });

    const status = page.locator('#w2PwaStatus');

    await expect(status).toBeVisible();
    await expect(status).toHaveAttribute(
      'data-kind',
      'offline'
    );

    await expect(status).toContainText(
      'sin conexión'
    );
  }
);

test(
  'la actualizacion PWA espera la accion explicita de la persona',
  async ({ request }) => {
    const serviceWorker = await (
      await request.get('/w2/sw.js')
    ).text();

    const registro = await (
      await request.get('/w2/pwa-register.js?v=3')
    ).text();

    expect(serviceWorker).not.toContain(
      ".then(()=>self.skipWaiting())"
    );
    expect(serviceWorker).toContain(
      "event.data?.type==='SKIP_WAITING'"
    );
    expect(registro).toContain(
      "waiting?.postMessage({"
    );
    expect(registro).toContain(
      "type: 'SKIP_WAITING'"
    );
  }
);

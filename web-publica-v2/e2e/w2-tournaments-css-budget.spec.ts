import { test, expect } from '@playwright/test';

test('Rendimiento R1 mantiene Torneos fuera del bundle global y con estilos dedicados', async ({ request }) => {
  const response = await request.get('/w2/torneos/');
  expect(response.ok()).toBeTruthy();
  const html = await response.text();

  expect(html).not.toMatch(/\/_astro\/global\.[^"']+\.css/);

  const estilosInline = [...html.matchAll(/<style[^>]*>([\s\S]*?)<\/style>/gi)]
    .map(match => match[1])
    .join('\n');
  const estilosVinculados = [...html.matchAll(/href=["']([^"']*\/_astro\/[^"']+\.css[^"']*)["']/gi)]
    .map(match => match[1]);

  const tieneCssDedicado =
    estilosVinculados.some(href => /\/_astro\/torneos\.[^"']+\.css/.test(href)) ||
    (estilosInline.includes('.w2t-shell') && estilosInline.includes('.w2t-bracket-overlay'));

  expect(tieneCssDedicado).toBeTruthy();
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

test('Rendimiento R1 conserva modales de Torneos sin utilidades Tailwind heredadas', async ({ page }) => {
  await page.goto('/w2/torneos/');

  const registration = page.locator('.w2t-registration-overlay');
  const bracket = page.locator('.w2t-bracket-overlay');
  await expect(registration).toHaveCount(1);
  await expect(bracket).toHaveCount(1);
  await expect(registration).toHaveClass(/w2t-registration-overlay/);
  await expect(bracket).toHaveClass(/w2t-bracket-overlay/);

  const clases = [
    await registration.getAttribute('class') || '',
    await bracket.getAttribute('class') || ''
  ].join(' ');

  for (const legacy of ['z-[80]', 'z-[85]', 'bg-black/78', 'bg-black/82', 'backdrop-blur-md', 'max-w-7xl']) {
    expect(clases).not.toContain(legacy);
  }
});

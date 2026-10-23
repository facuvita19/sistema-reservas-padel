import { expect, test } from '@playwright/test';

test.describe('CAP3B sistema global de filtros', () => {
  test('Comunidad abre y cierra filtros integrados', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto('/w2/comunidad/');
    const trigger=page.locator('#w2cmOpenFilters');
    const panel=page.locator('#w2cmFilters');
    await trigger.click();
    await expect(trigger).toHaveAttribute('aria-expanded','true');
    await expect(panel).toHaveClass(/open/);
    await panel.locator('#w2cmCloseFilters').click();
    await expect(trigger).toHaveAttribute('aria-expanded','false');
  });

  test('Torneos usa geometria integrada y despliegues internos', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto('/w2/torneos/');
    const trigger=page.locator('#abrirFiltrosTorneos');
    const panel=page.locator('#panelFiltrosTorneos');
    await trigger.click();
    await expect(trigger).toHaveAttribute('aria-expanded','true');
    await expect(panel).toHaveClass(/open/);
    const geometry=await panel.evaluate(node=>{const s=getComputedStyle(node);return {position:s.position,overflowY:s.overflowY,visibility:s.visibility}});
    expect(geometry.position).toBe('relative');
    expect(geometry.overflowY).toBe('visible');
    expect(geometry.visibility).toBe('visible');
    await panel.locator('#cerrarFiltrosTorneos').click();
    await expect(trigger).toHaveAttribute('aria-expanded','false');
  });
});

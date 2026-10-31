import { test, expect, type Page } from '@playwright/test';
import { mockRankingsVisualLab } from './fixtures/rankings-visual-lab';

test.beforeEach(async ({ page }) => {
  await mockRankingsVisualLab(page, { authenticated: true });
});

const settle = (page: Page) => page.evaluate(() => new Promise<void>(resolve => {
  requestAnimationFrame(() => requestAnimationFrame(() => resolve()));
}));

test.describe('T2C1B continuidad interna de Rankings', () => {
  test('categoria cambia contenido sin recrear shell', async ({ page }) => {
    await page.goto('/w2/rankings?categoria=161');
    await page.evaluate(() => {
      const root = document.querySelector('#rankingsContent');
      (window as any).__rankingsShell = {
        root,
        categories: root?.querySelector('.w2c2-categories'),
        head: root?.querySelector('.w2c2-rank-head')
      };
    });
    await page.locator('[data-category="162"]').click();
    await expect(page.locator('#w2RankingRows > .w2r-row')).toHaveCount(5);
    expect(await page.evaluate(() => {
      const saved = (window as any).__rankingsShell;
      const root = document.querySelector('#rankingsContent');
      return saved.root === root
        && saved.categories === root?.querySelector('.w2c2-categories')
        && saved.head === root?.querySelector('.w2c2-rank-head');
    })).toBe(true);
  });

  test('Atras restaura categoria sobre el mismo shell', async ({ page }) => {
    await page.goto('/w2/rankings?categoria=161&buscar=Maria');
    await page.evaluate(() => { (window as any).__rankingsRoot = document.querySelector('#rankingsContent'); });
    await page.locator('[data-category="162"]').click();
    await page.goBack();
    await expect(page.locator('[data-category="161"]')).toHaveAttribute('aria-selected', 'true');
    await expect(page.locator('#w2RankingSearch')).toHaveValue('Maria');
    expect(await page.evaluate(() => (window as any).__rankingsRoot === document.querySelector('#rankingsContent'))).toBe(true);
  });

  test('pageshow persistido conserva URL scroll y shell', async ({ page }) => {
    await page.goto('/w2/rankings?categoria=161&buscar=Maria');
    await page.locator('#rankingsContent').scrollIntoViewIfNeeded();
    const before = await page.evaluate(() => {
      (window as any).__rankingsRoot = document.querySelector('#rankingsContent');
      return { url: location.href, y: scrollY, length: history.length };
    });
    await page.evaluate(() => dispatchEvent(new PageTransitionEvent('pageshow', { persisted: true })));
    await settle(page);
    const after = await page.evaluate(() => ({
      url: location.href,
      y: scrollY,
      length: history.length,
      same: (window as any).__rankingsRoot === document.querySelector('#rankingsContent')
    }));
    expect(after.url).toBe(before.url);
    expect(after.length).toBe(before.length);
    expect(Math.abs(after.y - before.y)).toBeLessThanOrEqual(2);
    expect(after.same).toBe(true);
  });

  test('volver del perfil restaura fila y foco', async ({ page }) => {
    await page.goto('/w2/rankings?categoria=161');
    const row = page.locator('[data-profile-id="9101"]').first();
    await expect(row).toBeVisible();
    await row.scrollIntoViewIfNeeded();
    await row.click();
    await expect(page).toHaveURL(/\/w2\/jugador/);
    await page.goBack();
    await expect(page.locator('#w2RankingSearch')).toHaveValue('');
    await expect(page.locator('[data-profile-id="9101"]').first()).toBeFocused();
  });

  test('auditoria directa no duplica consulta al restaurar', async ({ page }) => {
    let calls = 0;
    page.on('request', request => {
      if (request.url().includes('/rankings/auditoria')) calls++;
    });
    await page.goto('/w2/rankings?categoria=161&auditoria=9101&auditoriaNombre=Maria&auditoriaIniciales=MR');
    await expect(page.locator('#w2rAuditDrawer')).toBeVisible();
    await expect.poll(() => calls).toBe(1);
  });
});

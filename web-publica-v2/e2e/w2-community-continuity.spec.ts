import { test, expect, type Page } from '@playwright/test';
import { mockPublicApi } from './fixtures/api-mock';

const players = [
  { perfilId: 7001, nombre: 'Ana Vitale', iniciales: 'AV', posicion: 'DRIVE', categoria: { id: 61, nombre: '5ta', color: '#e71939' }, disponibleParaJugar: true, aceptaInvitaciones: true, buscaTurnos: true, buscaTorneos: true, contactoWhatsappDisponible: true },
  { perfilId: 7002, nombre: 'Martina Rios', iniciales: 'MR', posicion: 'REVES', categoria: { id: 61, nombre: '5ta', color: '#e71939' }, disponibleParaJugar: true, aceptaInvitaciones: true, buscaTurnos: false, buscaTorneos: true, contactoWhatsappDisponible: false }
];
const settle = (page: Page) => page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => requestAnimationFrame(() => resolve()))));

test.beforeEach(async ({ page }) => {
  await mockPublicApi(page);
  await page.route('**/api/publica/comunidad/jugadores**', route => route.fulfill({ json: { jugadores: players, pagina: 1, tamano: 12, total: players.length, totalPaginas: 1 } }));
});

test.describe('T2C2A continuidad interna de Comunidad', () => {
  test('vistas cambian contenido sin recrear shell', async ({ page }) => {
    await page.goto('/w2/comunidad');
    await page.evaluate(() => { (window as any).__communityShell = { page: document.querySelector('.w2cm-page'), nav: document.querySelector('#w2cmViewNav') }; });
    await page.locator('[data-community-view="TURNOS"]').click();
    expect(await page.evaluate(() => { const saved = (window as any).__communityShell; return saved.page === document.querySelector('.w2cm-page') && saved.nav === document.querySelector('#w2cmViewNav'); })).toBe(true);
  });

  test('Atras y Adelante restauran vista y filtros sobre el mismo shell', async ({ page }) => {
    await page.goto('/w2/comunidad?vista=PAREJAS&buscar=Ana');
    await page.evaluate(() => { (window as any).__communityRoot = document.querySelector('.w2cm-page'); });
    await page.locator('[data-community-view="TURNOS"]').click();
    await page.goBack();
    await expect(page.locator('[data-community-view="PAREJAS"]')).toHaveAttribute('aria-selected', 'true');
    await expect(page.locator('#w2cmSearch')).toHaveValue('Ana');
    expect(await page.evaluate(() => (window as any).__communityRoot === document.querySelector('.w2cm-page'))).toBe(true);
    await page.goForward();
    await expect(page.locator('[data-community-view="TURNOS"]')).toHaveAttribute('aria-selected', 'true');
  });

  test('vista Turnos no consulta catalogo de perfiles', async ({ page }) => {
    let calls = 0;
    page.on('request', request => { if (request.url().includes('/comunidad/jugadores')) calls++; });
    await page.goto('/w2/comunidad?vista=TURNOS');
    await expect(page.locator('.w2cm-turn-card')).toHaveCount(1);
    expect(calls).toBe(0);
  });

  test('pageshow persistido conserva URL scroll y shell', async ({ page }) => {
    await page.goto('/w2/comunidad?vista=PAREJAS&buscar=Ana');
    await page.locator('#w2cmCatalog').scrollIntoViewIfNeeded();
    const before = await page.evaluate(() => { (window as any).__communityRoot = document.querySelector('.w2cm-page'); return { url: location.href, y: scrollY, length: history.length }; });
    await page.evaluate(() => dispatchEvent(new PageTransitionEvent('pageshow', { persisted: true })));
    await settle(page);
    const after = await page.evaluate(() => ({ url: location.href, y: scrollY, length: history.length, same: (window as any).__communityRoot === document.querySelector('.w2cm-page') }));
    expect(after.url).toBe(before.url);
    expect(after.length).toBe(before.length);
    expect(Math.abs(after.y - before.y)).toBeLessThanOrEqual(2);
    expect(after.same).toBe(true);
  });

  test('volver del perfil restaura tarjeta scroll y foco', async ({ page }) => {
    await page.goto('/w2/comunidad?vista=PAREJAS&buscar=Ana');
    const link = page.locator('[data-profile-id="7001"] .w2cm-card-hit');
    await link.scrollIntoViewIfNeeded();
    await link.click();
    await expect(page).toHaveURL(/\/w2\/jugador/);
    await page.goBack();
    await expect(page.locator('#w2cmSearch')).toHaveValue('Ana');
    await expect(page.locator('[data-profile-id="7001"] .w2cm-card-hit')).toBeFocused();
  });

  test('convocatoria conserva retorno contextual y perfil vuelve al detalle', async ({ page }) => {
    await page.goto('/w2/comunidad');
    const link = page.locator('[data-convocatoria-id="8101"] a');
    await link.scrollIntoViewIfNeeded();
    await link.click();
    await expect(page).toHaveURL(/\/w2\/comunidad\/convocatoria/);
    await expect(page.locator('#w2cvBack')).toHaveAttribute('href', '/w2/comunidad');
    await page.locator('#w2cvProfile').click();
    await expect(page).toHaveURL(/\/w2\/jugador/);
    await page.locator('#w2pBackToRanking').click();
    await expect(page).toHaveURL(/\/w2\/comunidad\/convocatoria/);
    await page.locator('#w2cvBack').click();
    await expect(page).toHaveURL(/\/w2\/comunidad\/?$/);
    await expect(page.locator('[data-convocatoria-id="8101"] a')).toBeFocused();
  });
});

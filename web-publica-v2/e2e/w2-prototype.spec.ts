import {test,expect} from '@playwright/test';
import {mockPublicApi} from './fixtures/api-mock';

test.beforeEach(async({page})=>mockPublicApi(page));

for(const route of ['/w2','/w2/rankings','/w2/reservar'])test('W2 carga '+route,async({page})=>{await page.goto(route);await expect(page.locator('h1').first()).toBeVisible();await expect(page.locator('body')).toBeVisible();await expect(page.locator('html')).not.toHaveCSS('overflow-x','scroll')});

test('perfil sin identificador ofrece una salida segura',async({page})=>{await page.goto('/w2/jugador');await expect(page.locator('#profileError')).toBeVisible();await expect(page.locator('#profileErrorText')).toContainText('Seleccioná un jugador desde el ranking');await expect(page.locator('#profileLoading')).toBeHidden()});

test('perfil publico muestra datos deportivos auditables',async({page})=>{await page.goto('/w2/jugador?perfilId=7001');await expect(page.locator('#profileContent')).toBeVisible();await expect(page.locator('#profileName')).toHaveText('Ana Vitale');await expect(page.locator('#profileRank')).toHaveText('#1');await expect(page.locator('#profilePoints')).toHaveText('1845');await expect(page.locator('#profileMatches article')).toHaveCount(2)});

test('ranking filtra jugadores',async({page})=>{await page.goto('/w2/rankings');const search=page.locator('#w2RankingSearch');await expect(search).toBeEnabled();await search.fill('Ana');await expect(page.locator('[data-player]:not(.hidden)')).toHaveCount(1);await expect(page.locator('[data-player]:not(.hidden)')).toContainText('Ana')});

test('ranking navega al perfil publicado',async({page})=>{await page.goto('/w2/rankings');await page.locator('[data-player]').first().click();await expect(page).toHaveURL(/\/w2\/jugador\?perfilId=7001/);await expect(page.locator('#profileName')).toHaveText('Ana Vitale')});

test('reserva selecciona turno y habilita continuidad',async({page})=>{await page.goto('/w2/reservar');const turno=page.locator('[data-w2-slot]').nth(1);await expect(turno).toBeVisible();await turno.focus();await turno.press('Enter');await expect(turno).toHaveClass(/selected/);await expect(page.locator('#w2Summary')).toContainText('19:30');await expect(page.locator('#w2Continue')).toBeEnabled()});

test('menu W2 abre y cierra sin retener foco oculto',async({page,isMobile})=>{test.skip(!isMobile);await page.goto('/w2');const trigger=page.locator('[data-w2-menu]');await trigger.click();const menu=page.locator('#w2Menu');await expect(menu).toHaveAttribute('aria-hidden','false');await page.locator('#abrirAutenticacionMovil').focus();await page.keyboard.press('Escape');await expect(menu).toHaveAttribute('aria-hidden','true');await expect(trigger).toBeFocused()});

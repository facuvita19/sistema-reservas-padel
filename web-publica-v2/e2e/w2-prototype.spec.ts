import {test,expect} from '@playwright/test';
import {mockPublicApi} from './fixtures/api-mock';

test.beforeEach(async({page})=>mockPublicApi(page));

for(const route of ['/w2','/w2/rankings','/w2/reservar'])test('W2 carga '+route,async({page})=>{await page.goto(route);await expect(page.locator('h1').first()).toBeVisible();await expect(page.locator('body')).toBeVisible();await expect(page.locator('html')).not.toHaveCSS('overflow-x','scroll')});

test('perfil sin identificador ofrece una salida segura',async({page})=>{await page.goto('/w2/jugador');await expect(page.locator('#profileError')).toBeVisible();await expect(page.locator('#profileErrorText')).toContainText('Seleccioná un jugador desde el ranking');await expect(page.locator('#profileLoading')).toBeHidden()});

test('perfil publico muestra datos deportivos auditables',async({page})=>{await page.goto('/w2/jugador?perfilId=7001');await expect(page.locator('#profileContent')).toBeVisible();await expect(page.locator('#profileName')).toHaveText('Ana Vitale');await expect(page.locator('#profileRank')).toHaveText('#1');await expect(page.locator('#profilePoints')).toHaveText('1845');await expect(page.locator('#profileMatches article')).toHaveCount(2)});

test('ranking filtra jugadores',async({page})=>{await page.goto('/w2/rankings');const search=page.locator('#w2RankingSearch');await expect(search).toBeEnabled();await search.fill('Ana');await expect(page.locator('[data-player]:not(.hidden)')).toHaveCount(1);await expect(page.locator('[data-player]:not(.hidden)')).toContainText('Ana')});

test('ranking navega al perfil publicado',async({page})=>{await page.goto('/w2/rankings');await page.locator('[data-player]').first().click();await expect(page).toHaveURL(/\/w2\/jugador\?perfilId=7001/);await expect(page.locator('#profileName')).toHaveText('Ana Vitale')});

test('reserva selecciona cancha y horario y avanza a datos',async({page})=>{await page.goto('/w2/reservar?fecha=2026-10-20');const cancha=page.locator('.reserve-court').first();await expect(cancha).toBeVisible();await cancha.click();await page.locator('#buscarHorarios').click();const turno=page.locator('.reserve-time').first();await expect(turno).toBeVisible();await turno.click();await expect(page.locator('[data-panel="3"]')).toBeVisible();await expect(page.locator('#resumenTurno')).toContainText('17:00')});

test('menu W2 abre y cierra sin retener foco oculto',async({page,isMobile})=>{test.skip(!isMobile);await page.goto('/w2');const trigger=page.locator('[data-w2-menu]');await trigger.click();const menu=page.locator('#w2Menu');await expect(menu).toHaveAttribute('aria-hidden','false');await page.locator('#abrirAutenticacionMovil').focus();await page.keyboard.press('Escape');await expect(menu).toHaveAttribute('aria-hidden','true');await expect(trigger).toBeFocused()});

test('R1.4A perfil conserva contexto de Rankings y enlaza auditoria',async({page})=>{await page.goto('/w2/jugador?perfilId=7001&categoria=61&buscar=ana');await expect(page.locator('#profileContent')).toBeVisible();await expect(page.locator('#w2pBackToRanking')).toHaveAttribute('href','/w2/rankings?categoria=61&buscar=ana');await expect(page.locator('#w2pAuditPoints')).toHaveAttribute('href','/w2/rankings?categoria=61&buscar=ana&auditoria=7001&volver=%2Fw2%2Fjugador%3FperfilId%3D7001%26categoria%3D61%26buscar%3Dana');await expect(page.locator('#w2pContextLabel')).toContainText('ana')});
test('R1.4A perfil publica disponibilidad e invitaciones por separado',async({page})=>{await page.goto('/w2/jugador?perfilId=7001');await expect(page.locator('#w2pAvailableFlag')).toBeVisible();await expect(page.locator('#w2pInvitesFlag')).toBeVisible();await expect(page.locator('.w2p-community-flags li')).toHaveCount(2)});

test('R1.4B cerrar auditoria vuelve al perfil que la abrio',async({page})=>{await page.goto('/w2/jugador?perfilId=7001&categoria=61&buscar=ana');await page.locator('#w2pAuditPoints').click();await expect(page.locator('#w2rAuditDrawer')).toBeVisible();await page.locator('#w2rAuditClose').click();await expect(page).toHaveURL('/w2/jugador?perfilId=7001&categoria=61&buscar=ana');await expect(page.locator('#profileContent')).toBeVisible()});

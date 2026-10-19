import {test,expect} from '@playwright/test';
import {mockPublicApi} from './fixtures/api-mock';

test.beforeEach(async({page})=>{await mockPublicApi(page,{authenticated:true});await page.goto('/w2/jugador?perfilId=7001');await expect(page.locator('#profileContent')).toBeVisible()});

test('R3.2B5C publica objetivos reales sin exponer datos privados',async({page})=>{await expect(page.locator('#w2pTurnsFlag')).toHaveText('Busca completar partidos');await expect(page.locator('#w2pTournamentsFlag')).toHaveText('Busca pareja para torneos');await expect(page.locator('#w2pInvitesFlag')).toHaveText('Acepta invitaciones');const text=await page.locator('.w2jp-community').innerText();expect(text).not.toContain('WhatsApp');expect(text).not.toContain('teléfono');expect(text).not.toContain('consentimiento')});

test('R3.2B5C simplifica categoria y enriquece historial temporal',async({page})=>{await expect(page.locator('#rankingDetail')).toContainText('#1 con 1.845 puntos');await expect(page.locator('#rankingDetail')).toContainText('+12 sets');await expect(page.locator('.w2jp-category-history li small').first()).toBeVisible();await expect(page.locator('.w2jp-category-history li span').first()).toContainText('Desde')});

test('R3.2B5C conserva geometria sin desbordamiento',async({page})=>{const overflow=await page.evaluate(()=>document.documentElement.scrollWidth-innerWidth);expect(overflow).toBeLessThanOrEqual(1);await expect(page.locator('.w2jp-community li')).toHaveCount(3)});


test('R3.2B5C estabiliza resumen y duraciones en movil',async({page})=>{await expect(page.locator('#rankingDetail span')).toHaveCount(2);await expect(page.locator('#rankingDetail span').nth(1)).toContainText('14 partidos · +12 sets · +38 games');await expect(page.locator('.w2jp-category-history li small').first()).toContainText(/año|mes/);await expect(page.locator('.w2jp-community>small')).toContainText('completar partidos');await expect(page.locator('.w2jp-community>small')).toContainText('encontrar pareja')});

import {test,expect} from '@playwright/test';
import {mockRankingsVisualLab} from './fixtures/rankings-visual-lab';

test.beforeEach(async({page})=>{await mockRankingsVisualLab(page,{authenticated:true})});

test('R3.1C5 converge oscuro y claro sin perder contraste estructural',async({page})=>{
 await page.addInitScript(()=>localStorage.setItem('w2-theme','dark'));await page.goto('/w2/rankings?categoria=161');await expect(page.locator('#w2RankingRows>.w2r-row')).toHaveCount(24);
 await expect(page.locator('html')).toHaveAttribute('data-w2-theme','dark');
 const dark=await page.locator('.w2r-row').first().evaluate(e=>({bg:getComputedStyle(e).backgroundColor,color:getComputedStyle(e).color}));
 await page.evaluate(()=>{document.documentElement.dataset.w2Theme='light';localStorage.setItem('w2-theme','light')});await expect(page.locator('html')).toHaveAttribute('data-w2-theme','light');
 const light=await page.locator('.w2r-row').first().evaluate(e=>({bg:getComputedStyle(e).backgroundColor,color:getComputedStyle(e).color}));
 expect(light.bg).not.toBe(dark.bg);expect(light.color).not.toBe(dark.color);await expect(page.locator('#w2RankingSearch')).toBeVisible();
});

test('R3.1C5 conserva ranking breve, vacio y auditorias poblada y vacia',async({page})=>{
 await page.goto('/w2/rankings?categoria=162');await expect(page.locator('#w2RankingRows>.w2r-row')).toHaveCount(5);
 await page.locator('[data-category="163"]').click();await expect(page.locator('#rankEmptyState')).toBeVisible();
 await page.goto('/w2/rankings?categoria=161');await page.locator('[data-audit-profile="9101"]').click();await expect(page.locator('.w2r-audit-item')).toHaveCount(12);await page.keyboard.press('Escape');
 await page.goto('/w2/rankings?categoria=163&auditoria=9199&auditoriaNombre=Facundo%20Vitale&auditoriaIniciales=FV&auditoriaCategoria=163&auditoriaCategoriaNombre=7.%C2%AA%20%C2%B7%20Sin%20resultados');await expect(page.locator('#w2rAuditDrawer')).toBeVisible();await expect(page.locator('#w2rAuditEmpty')).toBeVisible();
});

test('R3.1C5 mantiene foco visible, objetivos tactiles y ausencia de desborde',async({page})=>{
 await page.goto('/w2/rankings?categoria=161');const row=page.locator('.w2r-row').first();await row.focus();await expect(row).toBeFocused();
 const data=await page.evaluate(()=>({overflow:document.documentElement.scrollWidth>document.documentElement.clientWidth+1,row:document.querySelector<HTMLElement>('.w2r-row')!.getBoundingClientRect().height,button:document.querySelector<HTMLElement>('.w2r-audit-action')!.getBoundingClientRect().height}));
 expect(data.overflow).toBe(false);expect(data.row).toBeGreaterThanOrEqual(44);expect(data.button).toBeGreaterThanOrEqual(35);
});

import {test,expect} from '@playwright/test';
import {mockRankingsVisualLab} from './fixtures/rankings-visual-lab';

test.beforeEach(async({page})=>{await mockRankingsVisualLab(page)});

test('R3.1C1 compacta el hero y anticipa el ranking en escritorio',async({page,isMobile})=>{
 test.skip(isMobile);
 await page.goto('/w2/rankings?categoria=161');
 await expect(page.locator('#w2RankingRows>a')).toHaveCount(24);
 const data=await page.evaluate(()=>{
  const hero=document.querySelector<HTMLElement>('.w2c2-rank-hero')!.getBoundingClientRect();
  const layout=document.querySelector<HTMLElement>('.w2c2-rank-layout')!.getBoundingClientRect();
  const heading=document.querySelector<HTMLElement>('.w2c2-rank-head')!.getBoundingClientRect();
  return {heroHeight:hero.height,layoutTop:layout.top,headingTop:heading.top,viewport:innerHeight,overflow:document.documentElement.scrollWidth>document.documentElement.clientWidth+1};
 });
 expect(data.heroHeight).toBeLessThanOrEqual(505);
 expect(data.layoutTop).toBeLessThan(data.viewport);
 expect(data.headingTop).toBeLessThan(data.viewport+180);
 expect(data.overflow).toBe(false);
});

test('R3.1C1 preserva composición móvil y ranking extenso',async({page})=>{
 await page.goto('/w2/rankings?categoria=161');
 await expect(page.locator('#rankPodium>a')).toHaveCount(3);
 await expect(page.locator('#w2RankingRows>a')).toHaveCount(24);
 await expect(page.locator('#rankHeroPlayers')).toHaveText('24');
});

test('R3.1C2 integra cabecera, resumen y buscador en ranking poblado',async({page,isMobile})=>{
 await page.goto('/w2/rankings?categoria=161');
 await expect(page.locator('#w2RankingRows>a')).toHaveCount(24);
 const head=page.locator('.w2c2-rank-head'),search=page.locator('#w2RankingSearch'),podium=page.locator('#rankPodium');
 await expect(head).toBeVisible();await expect(search).toBeVisible();await expect(search).toBeEnabled();await expect(podium.locator('>a')).toHaveCount(3);
 const data=await page.evaluate(()=>{const box=(s:string)=>document.querySelector<HTMLElement>(s)!.getBoundingClientRect();return{head:box('.w2c2-rank-head'),copy:box('.w2c2-rank-head>div'),label:box('.w2c2-rank-head>label'),podium:box('#rankPodium'),overflow:document.documentElement.scrollWidth>document.documentElement.clientWidth+1}});
 expect(data.overflow).toBe(false);expect(data.podium.top).toBeGreaterThanOrEqual(data.head.bottom-2);
 if(isMobile){expect(data.label.top).toBeGreaterThanOrEqual(data.copy.bottom-2)}else{expect(data.label.left).toBeGreaterThan(data.copy.left);expect(data.label.bottom).toBeLessThanOrEqual(data.head.bottom+2)}
});
test('R3.1C3 compacta la clasificacion extensa sin perder legibilidad',async({page,isMobile})=>{
 await page.goto('/w2/rankings?categoria=161');
 const filas=page.locator('#w2RankingRows>.w2r-row');await expect(filas).toHaveCount(24);
 const datos=await filas.evaluateAll(items=>items.map(item=>{const r=item.getBoundingClientRect(),s=getComputedStyle(item);return{height:r.height,border:s.borderLeftColor}}));
 expect(datos.every(x=>x.height>=44)).toBe(true);
 if(!isMobile){expect(Math.max(...datos.map(x=>x.height))).toBeLessThanOrEqual(64);expect(datos[0].border).not.toBe('rgb(231, 25, 57)')}
 await expect(filas.nth(3)).toContainText('Carolina');
 await expect(filas.first().locator('.w2r-audit-action')).toBeVisible();
 expect(await page.evaluate(()=>document.documentElement.scrollWidth>document.documentElement.clientWidth+1)).toBe(false);
});

test('R3.1C3 reserva el rojo para hover o foco sin romper el acceso al perfil',async({page,isMobile})=>{
 await page.goto('/w2/rankings?categoria=161');const fila=page.locator('#w2RankingRows>.w2r-row').first();
 await expect(fila).toHaveAttribute('href',/\/w2\/jugador\?perfilId=/);
 if(!isMobile){await fila.hover();await expect.poll(()=>fila.evaluate(e=>getComputedStyle(e).borderLeftColor)).toBe('rgb(215, 25, 45)')}
 await fila.focus();await expect(fila).toBeFocused();await expect.poll(()=>fila.evaluate(e=>getComputedStyle(e).borderLeftColor)).toBe('rgb(215, 25, 45)');
});
test('R3.1C4 auditoria extensa conserva cabecera sticky y doce aportes legibles',async({page,isMobile})=>{
 await page.goto('/w2/rankings?categoria=161');await page.locator('[data-audit-profile="9101"]').click();
 const drawer=page.locator('#w2rAuditDrawer'),items=page.locator('.w2r-audit-item');await expect(drawer).toBeVisible();await expect(items).toHaveCount(12);
 await expect(page.locator('.w2r-audit-summary dt')).toHaveCount(4);await expect(page.locator('#w2rAuditMatches')).toHaveText('12');
 const data=await page.evaluate(()=>{const d=document.querySelector<HTMLElement>('#w2rAuditDrawer')!,h=d.querySelector<HTMLElement>(':scope>header')!;return{scroll:d.scrollHeight> d.clientHeight,position:getComputedStyle(h).position,top:h.getBoundingClientRect().top,drawerTop:d.getBoundingClientRect().top,width:d.getBoundingClientRect().width,viewport:innerWidth}});
 expect(data.scroll).toBe(true);expect(data.position).toBe('sticky');expect(Math.abs(data.top-data.drawerTop)).toBeLessThanOrEqual(3);expect(data.width).toBeLessThanOrEqual(data.viewport);
 if(isMobile)expect(data.width).toBeGreaterThanOrEqual(data.viewport-2);
});

test('R3.1C4 auditoria cierra con Escape y devuelve foco al disparador',async({page})=>{
 await page.goto('/w2/rankings?categoria=161');const boton=page.locator('[data-audit-profile="9101"]');await boton.click();await expect(page.locator('#w2rAuditDrawer')).toBeVisible();await page.keyboard.press('Escape');await expect(page.locator('#w2rAuditDrawer')).toBeHidden();await expect(boton).toBeFocused();await expect(page).not.toHaveURL(/auditoria=/);
});

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

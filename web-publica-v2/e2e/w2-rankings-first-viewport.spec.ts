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

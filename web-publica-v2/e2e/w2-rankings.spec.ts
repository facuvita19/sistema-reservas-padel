import {test,expect} from '@playwright/test';
import {mockPublicApi} from './fixtures/api-mock';

test.beforeEach(async({page})=>{await mockPublicApi(page)});

test('R1.2B presenta podio y ranking poblado con densidad estable',async({page})=>{
 await page.goto('/w2/rankings?categoria=61');
 await expect(page.locator('#rankPodium>a')).toHaveCount(3);
 await expect(page.locator('#w2RankingRows>a')).toHaveCount(8);
 await expect(page.locator('#rankPodium .winner')).toContainText('Ana Vitale');
 await expect(page.locator('#w2RankingRows>a').nth(3)).toContainText('Carolina Fernández Montenegro');
 await expect(page.locator('#rankHeroPlayers')).toHaveText('8');
 await expect(page.locator('#rankEmptyState')).toBeHidden();
});

test('R1.2B conserva alineacion y acceso al perfil en cada fila',async({page})=>{
 await page.goto('/w2/rankings?categoria=61');
 const fila=page.locator('#w2RankingRows>a').first();
 await expect(fila).toHaveAttribute('href','/w2/jugador?perfilId=7001');
 await expect(fila).toContainText('14');
 await expect(fila).toContainText('10');
 await expect(fila.locator('.w2r-points')).toHaveText('1.845');
 await expect(fila).toContainText('71%');
 expect(await fila.evaluate(e=>getComputedStyle(e).cursor)).toBe('pointer');
});

test('R1.2B cambia entre categoria poblada, breve y vacia',async({page})=>{
 await page.goto('/w2/rankings?categoria=61');
 await page.locator('[data-category="62"]').click();
 await expect(page).toHaveURL(/categoria=62/);
 await expect(page.locator('#w2RankingRows>a')).toHaveCount(2);
 await expect(page.locator('#rankHeroPlayers')).toHaveText('2');
 await page.locator('[data-category="63"]').click();
 await expect(page).toHaveURL(/categoria=63/);
 await expect(page.locator('#rankEmptyState')).toBeVisible();
 await expect(page.locator('#w2RankingRows>a')).toHaveCount(0);
});

test('R1.2B filtra nombres largos y restaura busqueda desde URL',async({page})=>{
 await page.goto('/w2/rankings?categoria=61&buscar=Carolina');
 await expect(page.locator('#w2RankingSearch')).toHaveValue('Carolina');
 await expect(page.locator('#w2RankingRows>a')).toHaveCount(1);
 await expect(page.locator('#w2RankingRows>a')).toContainText('Carolina Fernández Montenegro');
 await page.locator('#w2RankingSearch').fill('inexistente');
 await expect(page.locator('#w2RankingEmpty')).toBeVisible();
 await expect(page).toHaveURL(/buscar=inexistente/);
});

test('R1.2C el ranking movil usa tarjetas legibles sin superposiciones',async({page,isMobile})=>{if(!isMobile)return;await page.goto('/w2/rankings?categoria=61');const filas=page.locator('.w2r-row');await expect(filas).toHaveCount(8);const solapadas=await filas.evaluateAll(items=>items.some(item=>{const children=[...item.children].map(x=>(x as HTMLElement).getBoundingClientRect()).filter(r=>r.width>0&&r.height>0);for(let i=0;i<children.length;i++)for(let j=i+1;j<children.length;j++){const a=children[i],b=children[j];const area=Math.max(0,Math.min(a.right,b.right)-Math.max(a.left,b.left))*Math.max(0,Math.min(a.bottom,b.bottom)-Math.max(a.top,b.top));if(area>80)return true}return false}));expect(solapadas).toBe(false);await expect(page.locator('.w2c2-table-head')).toBeHidden()});

test('R1.2D agrupa jugador, formatea puntos y rotula la clasificacion',async({page})=>{await page.goto('/w2/rankings?categoria=61');await expect(page.locator('#rankFullHeading')).toBeVisible();await expect(page.locator('#rankFullCount')).toHaveText('8 jugadores');await expect(page.locator('.w2r-points').first()).toHaveText('1.845');await expect(page.locator('.w2r-podium-points').first()).toContainText('1.845');const jugador=page.locator('.w2r-row').first().locator('.w2r-player');await expect(jugador.locator('.w2r-avatar')).toBeVisible();await expect(jugador.locator('.w2r-player-copy')).toContainText('Ana Vitale')});

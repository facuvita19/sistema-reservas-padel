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

test('R1.2C el ranking movil usa tarjetas legibles sin superposiciones',async({page,isMobile})=>{if(!isMobile)return;await page.goto('/w2/rankings?categoria=61');const filas=page.locator('.w2r-row');await expect(filas).toHaveCount(8);for(const fila of await filas.all()){const datos=await fila.evaluate(e=>{const box=(selector:string)=>(e.querySelector(selector) as HTMLElement).getBoundingClientRect();const tarjeta=(e as HTMLElement).getBoundingClientRect();return {tarjeta,puesto:box('.w2r-position'),avatar:box('.w2r-avatar'),identidad:box('.w2r-player-copy'),flecha:box('.w2r-row-arrow'),puntos:box('.w2r-points'),efectividad:box('.w2r-effectiveness'),pj:box('.w2r-stat:nth-of-type(1)'),pg:box('.w2r-stat:nth-of-type(2)')}});expect(datos.puesto.right).toBeLessThanOrEqual(datos.avatar.left+1);expect(datos.avatar.right).toBeLessThanOrEqual(datos.identidad.left+1);expect(datos.identidad.right).toBeLessThanOrEqual(datos.flecha.left+1);expect(datos.identidad.bottom).toBeLessThanOrEqual(datos.puntos.top+1);expect(datos.puntos.right).toBeLessThanOrEqual(datos.efectividad.left+1);expect(datos.puntos.bottom).toBeLessThanOrEqual(datos.pj.top+1);expect(datos.pj.right).toBeLessThanOrEqual(datos.pg.left+1);for(const item of [datos.puesto,datos.avatar,datos.identidad,datos.flecha,datos.puntos,datos.efectividad,datos.pj,datos.pg]){expect(item.left).toBeGreaterThanOrEqual(datos.tarjeta.left-1);expect(item.right).toBeLessThanOrEqual(datos.tarjeta.right+1);expect(item.top).toBeGreaterThanOrEqual(datos.tarjeta.top-1);expect(item.bottom).toBeLessThanOrEqual(datos.tarjeta.bottom+1)}}await expect(page.locator('.w2c2-table-head')).toBeHidden()});
test('R1.2D agrupa jugador, formatea puntos y rotula la clasificacion',async({page})=>{await page.goto('/w2/rankings?categoria=61');await expect(page.locator('#rankFullHeading')).toBeVisible();await expect(page.locator('#rankFullCount')).toHaveText('8 jugadores');await expect(page.locator('.w2r-points').first()).toHaveText('1.845');await expect(page.locator('.w2r-podium-points').first()).toContainText('1.845');const jugador=page.locator('.w2r-row').first().locator('.w2r-player');await expect(jugador.locator('.w2r-avatar')).toBeVisible();await expect(jugador.locator('.w2r-player-copy')).toContainText('Ana Vitale')});

test('R1.2E compacta el podio y ordena PJ y PG en movil',async({page,isMobile})=>{if(!isMobile)return;await page.goto('/w2/rankings?categoria=61');const podium=page.locator('.w2r-podium-card');await expect(podium).toHaveCount(3);const cajas=await podium.evaluateAll(items=>items.map(x=>x.getBoundingClientRect()));expect(cajas[1].top).toBeLessThan(cajas[0].bottom);expect(cajas[2].top).toBeLessThan(cajas[0].bottom);const fila=page.locator('.w2r-row').first();const pj=fila.locator('.w2r-stat').nth(0),pg=fila.locator('.w2r-stat').nth(1);await expect(pj).toContainText('14');await expect(pg).toContainText('10');const posiciones=await Promise.all([pj.evaluate(e=>e.getBoundingClientRect()),pg.evaluate(e=>e.getBoundingClientRect())]);expect(posiciones[1].left).toBeGreaterThan(posiciones[0].right-1)});

test('R1.2F equilibra densidad de escritorio y conserva lectura movil',async({page,isMobile})=>{await page.goto('/w2/rankings?categoria=61');await expect(page.locator('.w2r-row')).toHaveCount(8);const hero=page.locator('.w2c2-rank-hero');const podium=page.locator('.w2c2-podium');const audit=page.locator('.w2c2-audit');const medidas=await Promise.all([hero.evaluate(e=>e.getBoundingClientRect()),podium.evaluate(e=>e.getBoundingClientRect()),audit.evaluate(e=>e.getBoundingClientRect())]);if(isMobile){expect(medidas[1].width).toBeLessThanOrEqual(390);expect(medidas[2].width).toBeLessThanOrEqual(390)}else{expect(medidas[0].height).toBeLessThanOrEqual(550);expect(medidas[1].height).toBeLessThanOrEqual(181);expect(medidas[2].height).toBeLessThan(300)}});

test('R1.2G alinea cabecera, puesto y jugadores en escritorio',async({page,isMobile})=>{if(isMobile)return;await page.goto('/w2/rankings?categoria=61');const headers=page.locator('.w2c2-table-head span');await expect(headers.nth(0)).toHaveText('PUESTO');await expect(headers.nth(1)).toHaveText('JUGADOR');const fila=page.locator('.w2r-row').first();const cajas=await Promise.all([headers.nth(0).evaluate(e=>e.getBoundingClientRect()),fila.locator('.w2r-position').evaluate(e=>e.getBoundingClientRect()),headers.nth(1).evaluate(e=>e.getBoundingClientRect()),fila.locator('.w2r-player').evaluate(e=>e.getBoundingClientRect())]);expect(Math.abs(cajas[0].left-cajas[1].left)).toBeLessThanOrEqual(1);expect(Math.abs(cajas[2].left-cajas[3].left)).toBeLessThanOrEqual(1);const jugadores=await page.locator('.w2r-player').evaluateAll(items=>items.map(e=>(e as HTMLElement).getBoundingClientRect().left));expect(new Set(jugadores.map(x=>Math.round(x))).size).toBe(1)});

test('R1.2H mantiene identidad alineada y elimina la franja roja movil',async({page,isMobile})=>{await page.goto('/w2/rankings?categoria=61');const filas=page.locator('.w2r-row');await expect(filas).toHaveCount(8);const posiciones=await filas.evaluateAll(items=>items.map(item=>{const avatar=item.querySelector('.w2r-avatar')!.getBoundingClientRect();const nombre=item.querySelector('.w2r-player-copy')!.getBoundingClientRect();return {avatar:Math.round(avatar.left),nombre:Math.round(nombre.left)}}));expect(new Set(posiciones.map(x=>x.avatar)).size).toBe(1);expect(new Set(posiciones.map(x=>x.nombre)).size).toBe(1);expect(posiciones[0].nombre).toBeGreaterThan(posiciones[0].avatar);if(isMobile){const borde=await filas.first().evaluate(e=>getComputedStyle(e).borderLeftColor);expect(borde).not.toBe('rgb(231, 25, 57)')}});

test('R1.2I asigna cada encabezado a su columna real y elimina el pseudo rojo movil',async({page,isMobile})=>{await page.goto('/w2/rankings?categoria=61');await expect(page.locator('.w2r-row')).toHaveCount(8);if(isMobile){const pseudo=await page.locator('.w2c2-table').evaluate(e=>{const s=getComputedStyle(e,'::before');return {content:s.content,display:s.display,background:s.backgroundColor}});expect(pseudo.content==='none'||pseudo.display==='none').toBe(true);expect(pseudo.background).not.toBe('rgb(215, 25, 45)');return}const headers=page.locator('.w2c2-table-head>span');const fila=page.locator('.w2r-row').first();const pares=[[headers.nth(0),fila.locator('.w2r-position')],[headers.nth(1),fila.locator('.w2r-player')],[headers.nth(2),fila.locator('.w2r-stat').nth(0)],[headers.nth(3),fila.locator('.w2r-stat').nth(1)],[headers.nth(4),fila.locator('.w2r-points')],[headers.nth(5),fila.locator('.w2r-effectiveness')]];for(const [h,v] of pares){const [a,b]=await Promise.all([h.evaluate(e=>e.getBoundingClientRect()),v.evaluate(e=>e.getBoundingClientRect())]);expect(Math.abs(a.left-b.left)).toBeLessThanOrEqual(1);expect(Math.abs(a.right-b.right)).toBeLessThanOrEqual(1)}});

test('R1.2J refuerza cabecera, oro del lider, vacio limpio y tarjetas moviles',async({page,isMobile})=>{await page.goto('/w2/rankings?categoria=61');await expect(page.locator('.w2r-row')).toHaveCount(8);if(isMobile){const fila=page.locator('.w2r-row').first();const puntos=await fila.locator('.w2r-points').evaluate(e=>e.getBoundingClientRect());const efect=await fila.locator('.w2r-effectiveness').evaluate(e=>e.getBoundingClientRect());expect(puntos.top).toBeLessThan(puntos.bottom);expect(efect.left).toBeGreaterThan(puntos.left);const borde=await fila.evaluate(e=>getComputedStyle(e).borderLeftColor);expect(borde).not.toBe('rgb(231, 25, 57)');return}await expect(page.locator('.w2c2-table-head small')).toHaveCount(4);const oro=await page.locator('.w2r-podium-card.winner .w2r-podium-avatar').evaluate(e=>getComputedStyle(e).borderColor);expect(oro).toBe('rgb(215, 173, 74)');await page.locator('[data-category="63"]').click();await expect(page.locator('#rankEmptyState')).toBeVisible();await expect(page.locator('.w2c2-table-head')).toBeHidden();await expect(page.locator('#w2RankingRows')).toBeHidden()});

test('R1.2K reconstruye la jerarquia interna de cada tarjeta movil',async({page,isMobile})=>{if(!isMobile)return;await page.goto('/w2/rankings?categoria=61');const fila=page.locator('.w2r-row').first();await expect(fila).toBeVisible();const medidas=await fila.evaluate(e=>{const box=(s:string)=>(e.querySelector(s) as HTMLElement).getBoundingClientRect();return {fila:(e as HTMLElement).getBoundingClientRect(),puesto:box('.w2r-position'),avatar:box('.w2r-avatar'),nombre:box('.w2r-player-copy'),puntos:box('.w2r-points'),efect:box('.w2r-effectiveness'),pj:box('.w2r-stat:nth-of-type(1)'),pg:box('.w2r-stat:nth-of-type(2)')}});expect(medidas.avatar.left).toBeGreaterThan(medidas.puesto.right-1);expect(medidas.nombre.left).toBeGreaterThan(medidas.avatar.right-1);expect(Math.abs(medidas.avatar.top-medidas.nombre.top)).toBeLessThan(5);expect(medidas.puntos.top).toBeGreaterThan(medidas.nombre.bottom);expect(medidas.efect.top).toBeGreaterThanOrEqual(medidas.nombre.bottom);expect(medidas.efect.left).toBeGreaterThan(medidas.puntos.right);expect(medidas.pj.top).toBeGreaterThan(medidas.puntos.bottom);expect(medidas.pg.left).toBeGreaterThan(medidas.pj.right);expect(medidas.fila.height).toBeLessThan(190)});

test('R1.2L ordena el podio clasico y aplica oro plata bronce solo en escritorio',async({page,isMobile})=>{await page.goto('/w2/rankings?categoria=61');const cards=page.locator('.w2r-podium-card');await expect(cards).toHaveCount(3);if(isMobile){const tops=await cards.evaluateAll(items=>items.map(x=>Math.round(x.getBoundingClientRect().top)));expect(Math.max(...tops)-Math.min(...tops)).toBeLessThan(3);return}const cajas=await cards.evaluateAll(items=>items.map(x=>x.getBoundingClientRect()));expect(cajas[0].left).toBeGreaterThan(cajas[1].left);expect(cajas[0].left).toBeLessThan(cajas[2].left);expect(cajas[1].top).toBeGreaterThan(cajas[0].top);expect(Math.abs(cajas[2].top-cajas[1].top)).toBeLessThanOrEqual(1);const bordes=await cards.evaluateAll(items=>items.map(x=>getComputedStyle(x).borderColor));expect(bordes[0]).toBe('rgba(215, 173, 74, 0.78)');expect(bordes[1]).toBe('rgba(174, 187, 194, 0.68)');expect(bordes[2]).toBe('rgba(178, 116, 69, 0.72)')});

test('R1.2M alinea segundo y tercero y centra avatares grandes en escritorio',async({page,isMobile})=>{if(isMobile)return;await page.goto('/w2/rankings?categoria=61');const cards=page.locator('.w2r-podium-card');await expect(cards).toHaveCount(3);const cajas=await cards.evaluateAll(items=>items.map(x=>x.getBoundingClientRect()));expect(Math.abs(cajas[1].top-cajas[2].top)).toBeLessThanOrEqual(1);expect(cajas[1].top).toBeGreaterThan(cajas[0].top);for(let i=0;i<3;i++){const card=cards.nth(i);const [c,a]=await Promise.all([card.evaluate(e=>e.getBoundingClientRect()),card.locator('.w2r-podium-avatar').evaluate(e=>e.getBoundingClientRect())]);expect(Math.abs((a.left+a.width/2)-(c.left+c.width/2))).toBeLessThanOrEqual(1);expect(a.width).toBeGreaterThanOrEqual(63);expect(a.height).toBeGreaterThanOrEqual(63)}});

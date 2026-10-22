import {test,expect} from '@playwright/test';
import {mockPublicApi} from './fixtures/api-mock';

test.beforeEach(async({page})=>{await mockPublicApi(page,{authenticated:true});await page.goto('/w2/jugador?perfilId=7001')});

test('R3.2B5A skip link solo aparece con navegacion por teclado',async({page})=>{const skip=page.locator('.w2-skip');await expect(skip).toHaveCSS('opacity','0');await page.keyboard.press('Tab');await expect(skip).toBeFocused();await expect(skip).toHaveCSS('opacity','1');await page.keyboard.press('Enter');await expect(page.locator('#w2-content')).toBeFocused()});

test('R3.2B5A recorrido informa seccion activa y mueve foco',async({page,isMobile})=>{test.skip(isMobile);const index=page.locator('.w2jp-profile-index');await expect(index).toBeVisible();const first=index.locator('a').first();await expect(first).toHaveAttribute('aria-current','location');await index.locator('a[href="#w2jpActivity"]').click();await expect(page.locator('#w2jpActivity')).toBeFocused();await expect(index.locator('a[href="#w2jpActivity"]')).toHaveAttribute('aria-current','location');await expect(index).toHaveCSS('position','static')});

test('R3.2B5A movil no presenta navegacion flotante ni obstruccion inferior',async({page,isMobile})=>{test.skip(!isMobile);await expect(page.locator('.w2jp-profile-index')).toBeHidden();const last=page.locator('.w2jp-community');await last.evaluate(element=>element.scrollIntoView({block:'end',behavior:'auto'}));await expect.poll(async()=>last.evaluate(element=>{const r=element.getBoundingClientRect();return Math.max(0,r.bottom-innerHeight)})).toBeLessThanOrEqual(1);const obstructing=await page.evaluate(()=>[...document.querySelectorAll<HTMLElement>('body *')].filter(e=>{const s=getComputedStyle(e),r=e.getBoundingClientRect(),intersects=r.right>0&&r.left<innerWidth&&r.bottom>0&&r.top<innerHeight;return s.position==='fixed'&&parseFloat(s.bottom||'999')<=1&&s.visibility!=='hidden'&&s.display!=='none'&&s.pointerEvents!=='none'&&intersects}).map(e=>e.className));expect(obstructing).toEqual([])});


test('R3.2B5A posiciona el skip link sin cubrir la cabecera',async({page})=>{await page.keyboard.press('Tab');const skip=page.locator('.w2-skip'),header=page.locator('.w2-header');await expect(skip).toBeFocused();const[s,h]=await Promise.all([skip.boundingBox(),header.boundingBox()]);expect(s&&h).toBeTruthy();expect(s!.y).toBeGreaterThanOrEqual(h!.y+h!.height+8);expect(s!.x).toBeGreaterThanOrEqual(10);expect(s!.x+s!.width).toBeLessThanOrEqual((await page.evaluate(()=>innerWidth))-10)});


test('R3.2B5A mantiene compacto el skip link en movil',async({page,isMobile})=>{test.skip(!isMobile);await page.keyboard.press('Tab');const skip=page.locator('.w2-skip');await expect(skip).toBeFocused();const box=await skip.boundingBox();expect(box).not.toBeNull();expect(box!.width).toBeLessThan((await page.evaluate(()=>innerWidth))*.72);expect(box!.height).toBeLessThanOrEqual(48)});


test('R3.3I-B2 limita retornos de Comunidad y conserva Mi cuenta',async({page})=>{for(const [volver,esperado] of [['https://evil.example/ataque','/w2/comunidad'],['/w2/comunidad-falsa','/w2/comunidad'],['/w2/cuenta?seccion=deporte','/w2/cuenta?seccion=deporte']]){await page.goto('/w2/jugador?perfilId=7001&origen=comunidad&volver='+encodeURIComponent(volver));await expect(page.locator('#w2pBackToRanking')).toHaveAttribute('href',esperado)}});

test('R3.3I-B2 usa color seguro ante categoria malformada',async({page})=>{await page.route('**/api/publica/jugadores/7001',r=>r.fulfill({json:{perfilId:7001,nombre:'Perfil color seguro',iniciales:'PS',posicion:'DRIVE',disponibleParaJugar:true,aceptaInvitaciones:false,buscaTurnos:false,buscaTorneos:false,categoria:{id:61,nombre:'5ta',color:'red;--ataque:url(javascript:alert(1))',desde:'2026-01-01'},ranking:null,partidos:0,victorias:0,derrotas:0,efectividad:0,historial:[],categorias:[]}}));await page.goto('/w2/jugador?perfilId=7001');await expect(page.locator('#w2jpHero')).toHaveCSS('--w2jp-category-accent','#d7ad4a')});

import {test,expect} from '@playwright/test';
import {mockPublicApi} from './fixtures/api-mock';

test.beforeEach(async({page})=>{await mockPublicApi(page,{authenticated:true});await page.goto('/w2/jugador?perfilId=7001')});

test('R3.2B5A skip link solo aparece con navegacion por teclado',async({page})=>{const skip=page.locator('.w2-skip');await expect(skip).toHaveCSS('opacity','0');await page.keyboard.press('Tab');await expect(skip).toBeFocused();await expect(skip).toHaveCSS('opacity','1');await page.keyboard.press('Enter');await expect(page.locator('#w2-content')).toBeFocused()});

test('R3.2B5A recorrido informa seccion activa y mueve foco',async({page,isMobile})=>{test.skip(isMobile);const index=page.locator('.w2jp-profile-index');await expect(index).toBeVisible();const first=index.locator('a').first();await expect(first).toHaveAttribute('aria-current','location');await index.locator('a[href="#w2jpActivity"]').click();await expect(page.locator('#w2jpActivity')).toBeFocused();await expect(index.locator('a[href="#w2jpActivity"]')).toHaveAttribute('aria-current','location');await expect(index).toHaveCSS('position','static')});

test('R3.2B5A movil no presenta navegacion flotante ni obstruccion inferior',async({page,isMobile})=>{test.skip(!isMobile);await expect(page.locator('.w2jp-profile-index')).toBeHidden();const last=page.locator('.w2jp-community');await last.scrollIntoViewIfNeeded();const box=await last.boundingBox();expect(box).not.toBeNull();expect(box!.y+box!.height).toBeLessThanOrEqual((await page.evaluate(()=>innerHeight))+1);const obstructing=await page.evaluate(()=>[...document.querySelectorAll<HTMLElement>('body *')].filter(e=>{const s=getComputedStyle(e),r=e.getBoundingClientRect(),intersects=r.right>0&&r.left<innerWidth&&r.bottom>0&&r.top<innerHeight;return s.position==='fixed'&&parseFloat(s.bottom||'999')<=1&&s.visibility!=='hidden'&&s.display!=='none'&&s.pointerEvents!=='none'&&intersects}).map(e=>e.className));expect(obstructing).toEqual([])});


test('R3.2B5A posiciona el skip link sin cubrir la cabecera',async({page})=>{await page.keyboard.press('Tab');const skip=page.locator('.w2-skip'),header=page.locator('.w2-header');await expect(skip).toBeFocused();const[s,h]=await Promise.all([skip.boundingBox(),header.boundingBox()]);expect(s&&h).toBeTruthy();expect(s!.y).toBeGreaterThanOrEqual(h!.y+h!.height+8);expect(s!.x).toBeGreaterThanOrEqual(10);expect(s!.x+s!.width).toBeLessThanOrEqual((await page.evaluate(()=>innerWidth))-10)});


test('R3.2B5A mantiene compacto el skip link en movil',async({page,isMobile})=>{test.skip(!isMobile);await page.keyboard.press('Tab');const skip=page.locator('.w2-skip');await expect(skip).toBeFocused();const box=await skip.boundingBox();expect(box).not.toBeNull();expect(box!.width).toBeLessThan((await page.evaluate(()=>innerWidth))*.72);expect(box!.height).toBeLessThanOrEqual(48)});

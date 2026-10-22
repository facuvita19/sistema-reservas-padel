import {test,expect,type Page} from '@playwright/test';import {mockPublicApi} from './fixtures/api-mock';
test.beforeEach(async({page})=>{await mockPublicApi(page,{authenticated:true});await page.goto('/w2')});
async function abrir(page:Page){if((page.viewportSize()?.width||1000)<=900){await page.locator('[data-w2-menu]').click();const mobile=page.locator('#w2NotificationsMobile');await expect(mobile).toBeVisible();await expect(page.locator('#w2NotificationsMobileCount')).toHaveText('1');await mobile.click()}else{const trigger=page.locator('#w2NotificationsTrigger');await expect(trigger).toBeVisible();await expect(page.locator('#w2NotificationsCount')).toHaveText('1');await trigger.click()}const panel=page.locator('#w2NotificationsPanel');await expect(panel).toBeVisible();return panel}
test('R3.4B5B muestra contador y centro accesible',async({page})=>{const panel=await abrir(page);await expect(panel).toContainText('Nueva solicitud para tu turno');await expect(panel.locator('.w2-notification.unread')).toHaveCount(1);await page.keyboard.press('Escape');await expect(panel).toBeHidden();if((page.viewportSize()?.width||1000)>900)await expect(page.locator('#w2NotificationsTrigger')).toBeFocused()});
test('R3.4B5B marca todas como leidas',async({page})=>{await abrir(page);await page.locator('#w2NotificationsReadAll').click();await expect(page.locator('.w2-notification.unread')).toHaveCount(0);const count=(page.viewportSize()?.width||1000)<=900?page.locator('#w2NotificationsMobileCount'):page.locator('#w2NotificationsCount');await expect(count).toBeHidden()});
test('R3.4B5B no expone centro a visitantes',async({page})=>{await page.route('**/api/publica/auth/sesion',r=>r.fulfill({json:{autenticado:false,perfil:null,vencimiento:null}}));await page.reload();await expect(page.locator('#w2Notifications')).toBeHidden();await expect(page.locator('#w2NotificationsMobile')).toBeHidden()});



test('R3.3I-B1 neutraliza contenido hostil y URLs externas',async({page})=>{
 await page.route('**/api/publica/cliente/comunidad/notificaciones',r=>r.fulfill({json:{notificaciones:[{notificacionId:991,tipo:'SOLICITUD_RECIBIDA',titulo:'<img src=x onerror=alert(1)>Titulo seguro',mensaje:'<script>alert(1)</script>Mensaje seguro',recursoTipo:'CONVOCATORIA',recursoId:1,urlContextual:'javascript:alert(1)',fechaCreacion:'2026-10-21T20:00:00',leida:false}],noLeidas:1,total:1}}));
 await page.reload();const panel=await abrir(page);
 await expect(panel.locator('img,script')).toHaveCount(0);await expect(panel).toContainText('Titulo seguro');await expect(panel).toContainText('Mensaje seguro');
 await expect(panel.locator('.w2-notification')).toHaveAttribute('href','/w2/cuenta?seccion=deporte');
});


test('R3.3I-B2 ignora identificadores de notificacion invalidos',async({page})=>{let lecturas=0;await page.route('**/api/publica/cliente/comunidad/notificaciones',r=>r.fulfill({json:{notificaciones:[{notificacionId:-9,tipo:'SOLICITUD_RECIBIDA',titulo:'Aviso seguro',mensaje:'Sin identificador operativo',recursoTipo:'CONVOCATORIA',recursoId:null,urlContextual:'/w2/comunidad',fechaCreacion:'2026-10-21T20:00:00',leida:false}],noLeidas:1,total:1}}));await page.route('**/api/publica/cliente/comunidad/notificaciones/*/leer',r=>{lecturas++;return r.fulfill({json:{}})});await page.reload();const panel=await abrir(page);await panel.locator('.w2-notification').click();expect(lecturas).toBe(0)});

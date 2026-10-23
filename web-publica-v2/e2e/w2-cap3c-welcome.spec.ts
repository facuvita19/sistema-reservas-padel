import {test,expect} from '@playwright/test';
test('la web no muestra bienvenida nativa',async({page})=>{await page.goto('/w2/');await expect(page.locator('#w2NativeWelcome')).toHaveCount(0)});
test('los recursos nativos se publican',async({request})=>{expect((await request.get('/w2/native-welcome.css')).ok()).toBeTruthy();expect((await request.get('/w2/native-welcome.js')).ok()).toBeTruthy()});

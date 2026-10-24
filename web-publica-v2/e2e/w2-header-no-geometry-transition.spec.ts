import {expect,test} from '@playwright/test';
test.skip(({isMobile})=>isMobile,'Contrato de escritorio.');
test('la geometria de cabecera no tiene transiciones',async({page})=>{await page.goto('/w2/torneos',{waitUntil:'domcontentloaded'});for(const selector of ['.w2-nav','.w2-links']){await expect(page.locator(selector)).toHaveCSS('transition-property','none');await expect(page.locator(selector)).toHaveCSS('animation-name','none')}});

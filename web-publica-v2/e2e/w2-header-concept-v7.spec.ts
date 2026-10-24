import {expect,test} from '@playwright/test';
test.skip(({isMobile})=>isMobile,'Contrato escritorio.');
test('tema usa exactamente un SVG visible por estado',async({page})=>{
 await page.addInitScript(()=>localStorage.setItem('w2-theme','dark'));
 await page.goto('/w2/rankings',{waitUntil:'domcontentloaded'});
 const toggle=page.locator('[data-w2-theme-toggle]');
 const sun=toggle.locator('>svg.w2-theme-sun');
 const moon=toggle.locator('>svg.w2-theme-moon');
 await expect(sun).toBeVisible();
 await expect(moon).toBeHidden();
 expect(await toggle.evaluate(el=>getComputedStyle(el,'::before').display)).toBe('none');
 expect(await toggle.evaluate(el=>getComputedStyle(el,'::after').display)).toBe('none');
 await toggle.click();
 await expect(sun).toBeHidden();
 await expect(moon).toBeVisible();
 await toggle.click();
 await expect(sun).toBeVisible();
 await expect(moon).toBeHidden();
});

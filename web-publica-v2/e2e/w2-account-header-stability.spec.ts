import{test,expect}from'@playwright/test';

const routes=['/w2/','/w2/comunidad','/w2/rankings','/w2/torneos','/w2/reservar','/w2/cuenta'];

test.describe('T1D3 cabecera inmutable al abrir Cuenta',()=>{
 for(const route of routes){
  test(route+' conserva geometria arriba y con scroll',async({page})=>{
   await page.goto(route);
   const result=await page.evaluate(async()=>{
    const header=document.querySelector<HTMLElement>('.w2-header')!;
    const nav=header.querySelector<HTMLElement>('.w2-nav')!;
    const frame=()=>new Promise<void>(resolve=>requestAnimationFrame(()=>requestAnimationFrame(()=>resolve())));
    const capture=()=>{const hs=getComputedStyle(header),hr=header.getBoundingClientRect(),nr=nav.getBoundingClientRect(),bs=getComputedStyle(document.body);return{headerTop:hr.top,headerHeight:hr.height,navTop:nr.top,navHeight:nr.height,position:hs.position,top:hs.top,bodyOverflow:bs.overflow,bodyOverflowY:bs.overflowY,scrollY}};
    const topClosed=capture();document.body.classList.add('account-menu-open');await frame();const topOpen=capture();document.body.classList.remove('account-menu-open');await frame();
    scrollTo({top:Math.min(650,Math.max(0,document.documentElement.scrollHeight-innerHeight)),behavior:'auto'});await frame();
    const scrollClosed=capture();document.body.classList.add('account-menu-open');await frame();const scrollOpen=capture();document.body.classList.remove('account-menu-open');
    return{topClosed,topOpen,scrollClosed,scrollOpen};
   });
   for(const pair of [[result.topClosed,result.topOpen],[result.scrollClosed,result.scrollOpen]] as const){
    const[closed,open]=pair;expect(Math.abs(open.headerTop-closed.headerTop)).toBeLessThanOrEqual(1);expect(open.headerTop).toBeGreaterThanOrEqual(-1);expect(Math.abs(open.headerHeight-closed.headerHeight)).toBeLessThanOrEqual(1);expect(open.position).toBe(closed.position);expect(open.top).toBe('0px');expect(open.bodyOverflowY).toBe('visible');
   }
  });
 }
});

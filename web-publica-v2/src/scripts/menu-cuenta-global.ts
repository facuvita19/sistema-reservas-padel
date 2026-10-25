/* W2_ACCOUNT_MENU_AUTHORITATIVE_8Q: propietario unico del menu desktop y cierres asociados. */
const FLAG='data-account-menu-v79-ready';
function initAccountMenuV79(){
 if(document.documentElement.hasAttribute(FLAG))return;
 document.documentElement.setAttribute(FLAG,'true');
 const trigger=document.getElementById('botonUsuario') as HTMLButtonElement|null;
 const menu=document.getElementById('desplegableUsuario') as HTMLElement|null;
 const mobilePanel=document.getElementById('menuMovil') as HTMLElement|null;
 const mobileBackdrop=document.getElementById('fondoMenuMovil') as HTMLElement|null;
 const mobileButton=document.getElementById('abrirMenuMovil') as HTMLButtonElement|null;
 if(menu&&menu.parentElement!==document.body)document.body.appendChild(menu);
 const items=()=>menu?[...menu.querySelectorAll<HTMLElement>('[role="menuitem"]')].filter(item=>item.offsetParent!==null):[];
 const desktopOpen=()=>Boolean(menu&&!menu.classList.contains('hidden'));
 const positionDesktop=()=>{if(!trigger||!menu)return;const r=trigger.getBoundingClientRect(),margin=12,width=Math.min(350,Math.max(280,innerWidth-margin*2)),left=Math.min(innerWidth-width-margin,Math.max(margin,r.right-width));menu.style.setProperty('--account-menu-left',left+'px');menu.style.setProperty('--account-menu-top',Math.max(margin,r.bottom+10)+'px');menu.style.setProperty('--account-menu-width',width+'px')};
 const closeDesktop=(focus=false)=>{if(!trigger||!menu)return;const open=desktopOpen();menu.classList.add('hidden');menu.setAttribute('aria-hidden','true');trigger.setAttribute('aria-expanded','false');document.body.classList.remove('account-menu-open');if(focus&&open)trigger.focus({preventScroll:true})};
 const openDesktop=()=>{if(!trigger||!menu)return;positionDesktop();menu.classList.remove('hidden');menu.setAttribute('aria-hidden','false');trigger.setAttribute('aria-expanded','true');document.body.classList.add('account-menu-open');requestAnimationFrame(()=>items()[0]?.focus({preventScroll:true}))};
 const closeMobile=()=>{mobilePanel?.classList.remove('open');mobileBackdrop?.classList.remove('open');mobileButton?.setAttribute('aria-expanded','false');document.body.classList.remove('site-menu-active')};
 trigger?.addEventListener('click',()=>desktopOpen()?closeDesktop(true):openDesktop());
 document.addEventListener('pointerdown',event=>{if(!desktopOpen()||!menu||!trigger)return;const node=event.target as Node;if(!menu.contains(node)&&!trigger.contains(node))closeDesktop(false)},true);
 menu?.addEventListener('keydown',event=>{const available=items(),current=available.indexOf(document.activeElement as HTMLElement);if(event.key==='Escape'){event.preventDefault();event.stopPropagation();closeDesktop(true);return}if(event.key==='ArrowDown'||event.key==='ArrowUp'){event.preventDefault();const step=event.key==='ArrowDown'?1:-1;available[(current+step+available.length)%available.length]?.focus();return}if(event.key==='Home'||event.key==='End'){event.preventDefault();available[event.key==='Home'?0:available.length-1]?.focus();return}if(event.key==='Tab'&&available.length){if(event.shiftKey&&document.activeElement===available[0]){event.preventDefault();available.at(-1)?.focus()}else if(!event.shiftKey&&document.activeElement===available.at(-1)){event.preventDefault();available[0]?.focus()}}});
 document.addEventListener('keydown',event=>{if(event.key==='Escape'&&desktopOpen()){event.preventDefault();event.stopPropagation();closeDesktop(true)}});
 document.addEventListener('click',event=>{const target=(event.target as HTMLElement).closest('a,button');if(!target)return;if(target.closest('#desplegableUsuario')&&target.id!=='botonUsuario')queueMicrotask(()=>closeDesktop(false));if(target.closest('#menuMovil')&&target.id!=='abrirMenuMovil'&&target.id!=='cerrarMenuMovil')queueMicrotask(closeMobile)},true);
 addEventListener('scroll',()=>closeDesktop(false),{passive:true});
 addEventListener('resize',()=>{if(matchMedia('(max-width:960px)').matches)closeDesktop(false);else{closeMobile();if(desktopOpen())positionDesktop()}},{passive:true});
 addEventListener('pagehide',()=>{closeDesktop(false);closeMobile()});
 addEventListener('w2-account-menu-close',(event:Event)=>closeDesktop(Boolean((event as CustomEvent<{focus?:boolean}>).detail?.focus)));
 addEventListener('sesion-cliente-cambiada',(event:any)=>{if(!event.detail?.autenticado){closeDesktop(false);closeMobile()}const name=event.detail?.perfil?.nombre||'Cuenta';trigger?.setAttribute('aria-label','Abrir menú de '+name)});
 const observer=new MutationObserver(()=>{if(menu?.classList.contains('hidden'))document.body.classList.remove('account-menu-open');if(!mobilePanel?.classList.contains('open'))mobileButton?.setAttribute('aria-expanded','false')});
 if(menu)observer.observe(menu,{attributes:true,attributeFilter:['class']});if(mobilePanel)observer.observe(mobilePanel,{attributes:true,attributeFilter:['class']});
}
if(document.readyState==='loading')addEventListener('DOMContentLoaded',initAccountMenuV79,{once:true});else initAccountMenuV79();

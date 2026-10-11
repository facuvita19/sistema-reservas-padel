const FLAG='data-account-menu-v79-ready';
function initAccountMenuV79(){
 if(document.documentElement.hasAttribute(FLAG))return;
 document.documentElement.setAttribute(FLAG,'true');
 const trigger=document.getElementById('botonUsuario') as HTMLButtonElement|null;
 const menu=document.getElementById('desplegableUsuario') as HTMLElement|null;
 const mobilePanel=document.getElementById('menuMovil') as HTMLElement|null;
 const mobileBackdrop=document.getElementById('fondoMenuMovil') as HTMLElement|null;
 const mobileButton=document.getElementById('abrirMenuMovil') as HTMLButtonElement|null;
 const closeDesktop=(focus=false)=>{if(!trigger||!menu)return;menu.classList.add('hidden');menu.setAttribute('aria-hidden','true');trigger.setAttribute('aria-expanded','false');document.body.classList.remove('account-menu-open');if(focus)trigger.focus({preventScroll:true})};
 const desktopOpen=()=>Boolean(menu&&!menu.classList.contains('hidden'));
 const closeMobile=()=>{mobilePanel?.classList.remove('open');mobileBackdrop?.classList.remove('open');mobileButton?.setAttribute('aria-expanded','false');document.body.classList.remove('site-menu-active')};
 document.addEventListener('pointerdown',e=>{if(!desktopOpen()||!menu||!trigger)return;const n=e.target as Node;if(!menu.contains(n)&&!trigger.contains(n))closeDesktop(false)},true);
 document.addEventListener('keydown',e=>{if(e.key==='Escape'&&desktopOpen()){e.preventDefault();e.stopPropagation();closeDesktop(true)}});
 document.addEventListener('click',e=>{const target=(e.target as HTMLElement).closest('a,button');if(!target)return;if(target.closest('#desplegableUsuario')&&target.id!=='botonUsuario')queueMicrotask(()=>closeDesktop(false));if(target.closest('#menuMovil')&&target.id!=='abrirMenuMovil'&&target.id!=='cerrarMenuMovil')queueMicrotask(closeMobile)},true);
 addEventListener('resize',()=>{if(matchMedia('(max-width:960px)').matches)closeDesktop(false);else closeMobile()},{passive:true});
 addEventListener('pagehide',()=>{closeDesktop(false);closeMobile()});
 addEventListener('sesion-cliente-cambiada',(e:any)=>{if(!e.detail?.autenticado){closeDesktop(false);closeMobile()}const name=e.detail?.perfil?.nombre||'Cuenta';trigger?.setAttribute('aria-label','Abrir menú de '+name)});
 const observer=new MutationObserver(()=>{if(menu?.classList.contains('hidden'))document.body.classList.remove('account-menu-open');if(!mobilePanel?.classList.contains('open'))mobileButton?.setAttribute('aria-expanded','false')});
 if(menu)observer.observe(menu,{attributes:true,attributeFilter:['class']});if(mobilePanel)observer.observe(mobilePanel,{attributes:true,attributeFilter:['class']});
}
if(document.readyState==='loading')addEventListener('DOMContentLoaded',initAccountMenuV79,{once:true});else initAccountMenuV79();

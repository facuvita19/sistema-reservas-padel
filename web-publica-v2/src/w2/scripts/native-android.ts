import { App } from '@capacitor/app';
import { Capacitor } from '@capacitor/core';
import { Keyboard } from '@capacitor/keyboard';

const visible=(element:HTMLElement|null)=>Boolean(element&&element.getClientRects().length&&getComputedStyle(element).visibility!=='hidden'&&getComputedStyle(element).display!=='none');
const click=(selector:string)=>{const element=document.querySelector<HTMLElement>(selector);if(!element||!visible(element))return false;element.click();return true};
const closeOpenSelect=()=>{const element=document.querySelector<HTMLElement>('.w2cm-select.open,.premium-select.open,.w2t-select.open');if(!element||!visible(element))return false;element.classList.remove('open','open-up');element.querySelector<HTMLElement>('[aria-expanded="true"]')?.setAttribute('aria-expanded','false');return true};
const closeByClass=(selector:string,classes:string[])=>{const elements=[...document.querySelectorAll<HTMLElement>(selector)].reverse();const element=elements.find(visible);if(!element)return false;classes.forEach(name=>element.classList.remove(name));element.setAttribute('aria-hidden','true');element.setAttribute('inert','');return true};

const nativePlatform=Capacitor.isNativePlatform();

function closeKeyboard(){
  if(!nativePlatform)return false;
  const active=document.activeElement;
  if(!document.documentElement.classList.contains('w2-keyboard-open')||!(active instanceof HTMLInputElement||active instanceof HTMLTextAreaElement||active instanceof HTMLSelectElement))return false;
  active.blur();
  void Keyboard.hide().catch(()=>{});
  return true;
}

function closeTopLayer(){
  if(document.documentElement.classList.contains('w2-native-welcome-open'))return true;
  const filterPanel=document.querySelector<HTMLElement>('[data-w2-filter-panel].open');
  if(filterPanel){
    const close=filterPanel.querySelector<HTMLElement>('#w2cmCloseFilters,#cerrarFiltrosTorneos,[data-w2-filter-close]');
    if(close){close.click();return true}
    filterPanel.classList.remove('open');
    filterPanel.setAttribute('aria-hidden','true');
    const trigger=document.querySelector<HTMLButtonElement>('[aria-controls="'+filterPanel.id+'"]');
    trigger?.setAttribute('aria-expanded','false');
    trigger?.focus({preventScroll:true});
    return true;
  }
  if(closeKeyboard())return true;
  if(click('#cerrarAutenticacion'))return true;
  if(click('#cerrarExitoInscripcion')||click('#cancelarInscripcion')||click('#cerrarInscripcion'))return true;
  if(click('#w2rDetailClose')||click('#w2jpInviteClose'))return true;
  if(click('#w2cmCloseFilters')||click('#cerrarFiltrosTorneos')||click('#aplicarFiltrosTorneos'))return true;
  if(click('.pc-panel.open [data-pc-close]')||click('.ps-panel.open [data-ps-close]'))return true;
  if(click('.w2cal-popover.open .w2cal-backdrop')||click('.pc-backdrop.open')||click('.ps-backdrop.open'))return true;
  if(closeOpenSelect())return true;
  if(click('[data-w2-notifications-close]')||click('#w2NotificationsClose'))return true;
  if(click('[data-w2-menu-close]'))return true;
  if(closeByClass('.w2-account-menu:not(.hidden),.site-account-v59-menu:not(.hidden)',['open']))return true;
  return false;
}

function initializeNativeAndroid(){
  let lastExit=0;
  const home=()=>location.pathname.replace(/\/+$/,'')==='/w2';
  const announceExit=()=>{let node=document.getElementById('w2NativeExitHint');if(!node){node=document.createElement('div');node.id='w2NativeExitHint';node.className='w2-native-exit-hint';node.setAttribute('role','status');node.setAttribute('aria-live','polite');document.body.appendChild(node)}node.textContent='Volvé a presionar para salir';node.classList.add('show');setTimeout(()=>node?.classList.remove('show'),1800)};
  const editableFocused=()=>{const active=document.activeElement;if(!(active instanceof HTMLElement))return false;const editable=active.matches('input,textarea,[contenteditable="true"]');if(!editable)return false;active.blur();return true};

  void App.addListener('backButton',({canGoBack})=>{
    if(document.documentElement.classList.contains('w2-keyboard-open')&&editableFocused())return;
    if(closeTopLayer())return;
    if(canGoBack&&!home()){history.back();return}
    const now=Date.now();
    if(now-lastExit<1800){void App.exitApp();return}
    lastExit=now;
    announceExit();
  });

  const root=document.documentElement;
  let keyboardHeight=0;
  let keyboardOpen=false;
  let focusTimer=0;

  function syncKeyboard(open:boolean,height=0){
    keyboardOpen=open;
    keyboardHeight=open?Math.max(0,Number(height)||0):0;
    root.classList.toggle('w2-keyboard-open',open);
    root.style.setProperty('--w2-native-keyboard-height',keyboardHeight+'px');
    root.dataset.w2KeyboardMode=open?'overlay':'closed';
    if(!open)return;
    window.clearTimeout(focusTimer);
    focusTimer=window.setTimeout(()=>{
      const active=document.activeElement;
      if(!(active instanceof HTMLElement)||!active.matches('input,textarea,[contenteditable="true"],select'))return;
      active.scrollIntoView({block:'center',inline:'nearest',behavior:'auto'});
    },180);
  }

  void Keyboard.addListener('keyboardWillShow',info=>syncKeyboard(true,info.keyboardHeight));
  void Keyboard.addListener('keyboardDidShow',info=>syncKeyboard(true,info.keyboardHeight));
  void Keyboard.addListener('keyboardWillHide',()=>syncKeyboard(false,0));
  void Keyboard.addListener('keyboardDidHide',()=>syncKeyboard(false,0));

  document.addEventListener('focusin',event=>{
    const target=event.target;
    if(!(target instanceof HTMLElement)||!target.matches('input,textarea,[contenteditable="true"],select'))return;
    if(keyboardOpen)window.setTimeout(()=>target.scrollIntoView({block:'center',inline:'nearest',behavior:'auto'}),90);
  });

  syncKeyboard(false,0);
}

if(nativePlatform){
  initializeNativeAndroid();
}

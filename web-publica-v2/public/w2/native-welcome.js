/* CAP3C4: una unica fuente autoritativa para la entrada nativa. */
(() => {
  const nativeHost = location.protocol === 'capacitor:' || location.hostname === '10.0.2.2' || /; wv\)/i.test(navigator.userAgent);
  if (!nativeHost) return;

  const root = document.documentElement;
  const GUEST_KEY = 'w2.native.guest.runtime';
  const STATES = Object.freeze({ CHECKING:'checking', WELCOME:'welcome', AUTHENTICATING:'authenticating', GUEST:'guest', AUTHENTICATED:'authenticated' });
  let state = STATES.CHECKING;
  let overlay = null;
  let authenticated = false;

  const isGuest = () => { try { return sessionStorage.getItem(GUEST_KEY) === '1'; } catch { return false; } };
  const setGuest = value => { try { value ? sessionStorage.setItem(GUEST_KEY,'1') : sessionStorage.removeItem(GUEST_KEY); } catch {} };
  const releasePaint = () => { root.classList.add('w2-native-welcome-ready'); root.classList.remove('w2-native-welcome-pending'); };

  const template = () => {
    const node = document.createElement('section');
    node.id='w2NativeWelcome'; node.className='w2-native-welcome'; node.hidden=true;
    node.setAttribute('role','dialog'); node.setAttribute('aria-modal','true'); node.setAttribute('aria-labelledby','w2NativeWelcomeTitle');
    node.innerHTML=`<main class="w2-native-welcome-main"><div class="w2-native-welcome-brand"><img src="/w2/icons/icon-native-512.png" alt="" width="68" height="68"><span><small>PLATAFORMA DEPORTIVA</small><b>W2 · Pádel Las Flores</b></span></div><div class="w2-native-welcome-copy"><p>TU CLUB, SIEMPRE CERCA</p><h1 id="w2NativeWelcomeTitle">Entrá a jugar.<br><strong>Seguí creciendo.</strong></h1><span>Reservas, torneos, rankings y comunidad en una sola experiencia.</span></div><div class="w2-native-welcome-actions"><button class="w2-native-welcome-login" type="button">Ingresar con mi cuenta</button><button class="w2-native-welcome-guest" type="button">Continuar como invitado</button><p class="w2-native-welcome-status" role="status" aria-live="polite"></p></div></main>`;
    return node;
  };

  const render = next => {
    state=next; root.dataset.w2NativeEntryState=next;
    if(!overlay) return;
    const visible=next===STATES.WELCOME;
    overlay.hidden=!visible;
    overlay.classList.toggle('w2-native-welcome-suspended',next===STATES.AUTHENTICATING);
    root.classList.toggle('w2-native-welcome-open',visible);
    if(next!==STATES.CHECKING) releasePaint();
    dispatchEvent(new CustomEvent('w2-native-welcome-change',{detail:{open:visible,state:next}}));
    if(visible) requestAnimationFrame(()=>overlay?.querySelector('button')?.focus({preventScroll:true}));
  };

  const resolveSession = session => {
    if(session?.autenticado && session?.perfil){ authenticated=true; setGuest(false); render(STATES.AUTHENTICATED); overlay?.remove(); overlay=null; return; }
    if(authenticated || state===STATES.AUTHENTICATED) return;
    if(state===STATES.AUTHENTICATING) return;
    render(isGuest()?STATES.GUEST:STATES.WELCOME);
  };

  const init = () => {
    overlay=template(); document.body.prepend(overlay);
    const modal=document.getElementById('modalAutenticacion');
    overlay.querySelector('.w2-native-welcome-login')?.addEventListener('click',()=>{ render(STATES.AUTHENTICATING); document.getElementById('abrirAutenticacion')?.click(); });
    overlay.querySelector('.w2-native-welcome-guest')?.addEventListener('click',()=>{ setGuest(true); render(STATES.GUEST); });

    if(modal) new MutationObserver(()=>{
      const open=!modal.classList.contains('hidden');
      if(open){ render(STATES.AUTHENTICATING); return; }
      if(state===STATES.AUTHENTICATING && !authenticated) render(isGuest()?STATES.GUEST:STATES.WELCOME);
    }).observe(modal,{attributes:true,attributeFilter:['class']});

    addEventListener('sesion-cliente-resuelta',event=>{
      const detail=event.detail||{};
      if(detail.origen==='logout'){ authenticated=false; setGuest(false); render(STATES.WELCOME); return; }
      resolveSession(detail);
    });

    /* Si autenticacion.ts ya resolvio antes de este script, usamos su dataset estable. */
    if(root.dataset.w2SessionResolved==='true'){
      resolveSession({autenticado:root.dataset.session==='authenticated',perfil:root.dataset.session==='authenticated'?{}:null});
      return;
    }
    if(isGuest()){ render(STATES.GUEST); return; }
    render(STATES.CHECKING);
  };

  document.readyState==='loading' ? addEventListener('DOMContentLoaded',init,{once:true}) : init();
})();

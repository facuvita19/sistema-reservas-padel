/* CAP3C4: carga exclusiva de bienvenida para host nativo 7S. */
(()=>{
  try{
    const native=location.protocol==='capacitor:'||location.hostname==='10.0.2.2'||/; wv\)/i.test(navigator.userAgent);
    if(!native)return;
    const root=document.documentElement;
    const guest=sessionStorage.getItem('w2.native.guest.runtime')==='1';
    if(!guest)root.classList.add('w2-native-welcome-pending');
    /* CAP3C5: carga exclusiva de estilos nativos 7V. Orden de cascada: estructura, CAP3B y bienvenida. */
    const nativeStyles=[
      ['/w2/native-mobile.css','mobile'],
      ['/w2/cap3b-native.css','cap3b'],
      ['/w2/native-welcome.css','welcome']
    ];
    for(const [href,kind] of nativeStyles){
      if(document.querySelector(`link[data-w2-native-style="${kind}"]`))continue;
      const style=document.createElement('link');
      style.rel='stylesheet';
      style.href=href;
      style.dataset.w2NativeStyle=kind;
      document.head.appendChild(style);
    }

    if(!document.querySelector('script[data-w2-native-welcome]')){
      const script=document.createElement('script');
      script.type='module';
      script.src='/w2/native-welcome.js';
      script.dataset.w2NativeWelcome='script';
      document.head.appendChild(script);
    }
  }catch{
    document.documentElement.classList.add('w2-native-welcome-pending');
  }
})();

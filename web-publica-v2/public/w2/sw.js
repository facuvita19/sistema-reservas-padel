const VERSION='w2-pwa-v4';
const SHELL=VERSION+'-shell';
const RUNTIME=VERSION+'-runtime';
const OFFLINE='/w2/offline.html';

const PRECACHE=[
  '/w2/',
  '/w2/comunidad',
  '/w2/torneos',
  '/w2/rankings',
  '/w2/manifest.webmanifest',
  '/w2/icons/icon-192.png',
  '/w2/icons/icon-512.png',
  '/w2/icons/icon-maskable-512.png',
  OFFLINE
];

const ALWAYS_NETWORK=new Set([
  '/w2/sw.js',
  '/w2/pwa-register.js',
  '/w2/pwa-status.css'
]);

self.addEventListener('install',event=>{
  event.waitUntil(
    caches
      .open(SHELL)
      .then(cache=>cache.addAll(PRECACHE))
  );
});

self.addEventListener('message',event=>{
  if(event.data?.type==='SKIP_WAITING'){
    void self.skipWaiting();
  }
});

self.addEventListener('activate',event=>{
  event.waitUntil(
    caches
      .keys()
      .then(keys=>
        Promise.all(
          keys
            .filter(key=>
              key.startsWith('w2-pwa-') &&
              key!==SHELL &&
              key!==RUNTIME
            )
            .map(key=>caches.delete(key))
        )
      )
      .then(()=>self.clients.claim())
  );
});

self.addEventListener('fetch',event=>{
  const request=event.request;

  if(request.method!=='GET'){
    return;
  }

  const url=new URL(request.url);

  if(
    url.origin!==self.location.origin ||
    url.pathname.startsWith('/api/')
  ){
    return;
  }

  if(ALWAYS_NETWORK.has(url.pathname)){
    event.respondWith(
      fetch(request,{
        cache:'no-store'
      }).catch(()=>caches.match(request))
    );

    return;
  }

  if(request.mode==='navigate'){
    event.respondWith(
      fetch(request)
        .then(response=>{
          if(
            response.ok &&
            url.pathname.startsWith('/w2/')
          ){
            const copy=response.clone();

            void caches
              .open(RUNTIME)
              .then(cache=>cache.put(request,copy));
          }

          return response;
        })
        .catch(async()=>{
          return (
            await caches.match(request) ||
            await caches.match(OFFLINE)
          );
        })
    );

    return;
  }

  if(
    !url.pathname.startsWith('/w2/') &&
    !url.pathname.startsWith('/_astro/')
  ){
    return;
  }

  event.respondWith(
    caches.match(request).then(cached=>{
      const fresh=fetch(request).then(response=>{
        if(response.ok){
          const copy=response.clone();

          void caches
            .open(RUNTIME)
            .then(cache=>cache.put(request,copy));
        }

        return response;
      }).catch(()=>cached);

      return cached||fresh;
    })
  );
});

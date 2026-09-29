const CACHE = 'padel-web-network-first-1';
const SHELL = [
    '/',
    '/index.html',
    '/styles.css?v=perfil-1',
    '/app.js?v=perfil-1',
    '/manifest.webmanifest',
    '/offline.html',
    '/icon-192.png',
    '/icon-512.png',
    '/icon-maskable-512.png'
];

self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE)
            .then(cache => cache.addAll(SHELL))
            .then(() => self.skipWaiting())
    );
});

self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys()
            .then(keys => Promise.all(
                keys.filter(key => key !== CACHE)
                    .map(key => caches.delete(key))
            ))
            .then(() => self.clients.claim())
    );
});

self.addEventListener('message', event => {
    if (event.data === 'SKIP_WAITING') {
        self.skipWaiting();
    }
});

async function networkFirst(request, fallback) {
    try {
        const response = await fetch(request, { cache: 'no-store' });
        if (response.ok) {
            const cache = await caches.open(CACHE);
            await cache.put(request, response.clone());
        }
        return response;
    } catch (error) {
        const cached = await caches.match(request);
        if (cached) return cached;
        if (fallback) {
            const fallbackResponse = await caches.match(fallback);
            if (fallbackResponse) return fallbackResponse;
        }
        throw error;
    }
}

self.addEventListener('fetch', event => {
    const request = event.request;
    const url = new URL(request.url);

    if (request.method !== 'GET' || url.port === '8080') {
        return;
    }

    if (request.mode === 'navigate') {
        event.respondWith(networkFirst(request, '/offline.html'));
        return;
    }

    const isApplicationAsset = url.pathname.endsWith('/app.js')
            || url.pathname.endsWith('/styles.css')
            || url.pathname.endsWith('/service-worker.js')
            || url.pathname.endsWith('/manifest.webmanifest');

    if (isApplicationAsset) {
        event.respondWith(networkFirst(request));
        return;
    }

    event.respondWith(
        caches.match(request).then(cached => {
            if (cached) return cached;
            return fetch(request).then(response => {
                if (response.ok) {
                    const copy = response.clone();
                    caches.open(CACHE).then(cache => cache.put(request, copy));
                }
                return response;
            });
        })
    );
});
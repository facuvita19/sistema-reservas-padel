(() => {
  const root = document.getElementById('w2PwaStatus');
  const message = document.getElementById('w2PwaMessage');
  const update = document.getElementById('w2PwaUpdate');
  const dismiss = document.getElementById('w2PwaDismiss');
  const install = document.getElementById('w2PwaInstall');
  const ios = document.getElementById('w2PwaIos');

  const DISMISS_KEY = 'w2.pwa.install.dismissedAt';
  const DISMISS_DURATION = 30 * 24 * 60 * 60 * 1000;

  let waiting = null;
  let requestedUpdate = false;
  let deferredInstall = null;

  const standalone =
    matchMedia('(display-mode: standalone)').matches ||
    navigator.standalone === true;

  const nativeHost =
    location.protocol === 'capacitor:' ||
    location.hostname === '10.0.2.2' ||
    /; wv\)/i.test(navigator.userAgent);

  const mobileBrowser =
    !nativeHost &&
    (
      matchMedia('(max-width: 767px)').matches ||
      /android|iphone|ipad|ipod/i.test(navigator.userAgent)
    );

  const installDismissed = () => {
    try {
      const value = Number(localStorage.getItem(DISMISS_KEY) || 0);

      return (
        Number.isFinite(value) &&
        value > 0 &&
        Date.now() - value < DISMISS_DURATION
      );
    } catch {
      return false;
    }
  };

  const rememberInstallDismissal = () => {
    try {
      localStorage.setItem(
        DISMISS_KEY,
        String(Date.now())
      );
    } catch {}
  };

  document.documentElement.classList.toggle(
    'w2-standalone',
    standalone
  );

  const show = (text, kind, options = {}) => {
    if (!root || !message) {
      return;
    }

    message.textContent = text;
    root.dataset.kind = kind;
    root.classList.remove('hidden');

    update?.classList.toggle(
      'hidden',
      !options.update
    );

    install?.classList.toggle(
      'hidden',
      !options.install
    );

    ios?.classList.toggle(
      'hidden',
      !options.ios
    );

    root.setAttribute(
      'role',
      kind === 'offline'
        ? 'alert'
        : 'status'
    );
  };

  const hide = () => {
    root?.classList.add('hidden');
  };

  const syncConnection = () => {
    if (navigator.onLine) {
      if (root?.dataset.kind === 'offline') {
        hide();
      }

      return;
    }

    show(
      'Estás sin conexión. Algunas operaciones necesitan internet.',
      'offline'
    );
  };

  addEventListener('online', () => {
    show(
      'Volviste a estar en línea.',
      'online'
    );

    /* RENDIMIENTO R2.4 - sincronizacion de conectividad en tiempo ocioso 7P */
    if ('requestIdleCallback' in window) {
      window.requestIdleCallback(syncConnection, { timeout: 2600 });
    } else {
      setTimeout(syncConnection, 2600);
    }
  });

  addEventListener('offline', () => {
    show(
      'Estás sin conexión. Algunas operaciones necesitan internet.',
      'offline'
    );
  });

  dismiss?.addEventListener('click', () => {
    if (
      root?.dataset.kind === 'install' ||
      root?.dataset.kind === 'ios'
    ) {
      rememberInstallDismissal();
    }

    hide();
  });

  update?.addEventListener('click', () => {
    requestedUpdate = true;
    update.disabled = true;

    waiting?.postMessage({
      type: 'SKIP_WAITING'
    });
  });

  install?.addEventListener('click', async () => {
    if (!deferredInstall) {
      return;
    }

    install.disabled = true;
    deferredInstall.prompt();

    await deferredInstall.userChoice;

    deferredInstall = null;
    hide();
  });

  ios?.addEventListener('click', () => {
    show(
      'En iPhone o iPad, abrí Compartir y elegí “Agregar a inicio”.',
      'ios',
      {
        ios: true
      }
    );
  });

  addEventListener('beforeinstallprompt', event => {
    event.preventDefault();
    deferredInstall = event;

    if (
      standalone ||
      !mobileBrowser ||
      installDismissed()
    ) {
      return;
    }

    show(
      'Instalá W2 para acceder más rápido.',
      'install',
      {
        install: true
      }
    );
  });

  addEventListener('appinstalled', () => {
    deferredInstall = null;

    document.documentElement.classList.add(
      'w2-standalone'
    );

    show(
      'W2 quedó instalada en tu dispositivo.',
      'installed'
    );

    setTimeout(hide, 3200);
  });

  const iosEligible =
    mobileBrowser &&
    /iphone|ipad|ipod/i.test(navigator.userAgent) &&
    !standalone &&
    !installDismissed();

  if (iosEligible) {
    setTimeout(() => {
      show(
        'Agregá W2 a tu pantalla de inicio.',
        'ios',
        {
          ios: true
        }
      );
    }, 900);
  }

  syncConnection();

  if (
    !('serviceWorker' in navigator) ||
    !window.isSecureContext
  ) {
    return;
  }

  let refreshing = false;

  navigator.serviceWorker.addEventListener(
    'controllerchange',
    () => {
      if (
        !requestedUpdate ||
        refreshing
      ) {
        return;
      }

      refreshing = true;
      location.reload();
    }
  );

  addEventListener(
    'load',
    async () => {
      try {
        const registration =
          await navigator.serviceWorker.register(
            '/w2/sw.js',
            {
              scope: '/w2/',
              updateViaCache: 'none'
            }
          );

        const announce = worker => {
          waiting = worker;

          show(
            'Hay una versión nueva de W2 lista.',
            'update',
            {
              update: true
            }
          );
        };

        if (
          registration.waiting &&
          navigator.serviceWorker.controller
        ) {
          announce(registration.waiting);
        }

        registration.addEventListener(
          'updatefound',
          () => {
            const worker = registration.installing;

            if (!worker) {
              return;
            }

            worker.addEventListener(
              'statechange',
              () => {
                if (
                  worker.state === 'installed' &&
                  navigator.serviceWorker.controller
                ) {
                  announce(worker);
                }
              }
            );
          }
        );

        /* RENDIMIENTO R2.3 - actualizaciones PWA condicionadas por visibilidad 7M */
  const UPDATE_INTERVAL_MS = 60 * 60 * 1000;
  let updateTimer = 0;

  const checkForUpdate = () => {
    if (document.visibilityState === 'visible') void registration.update();
  };
  const stopUpdateChecks = () => {
    if (!updateTimer) return;
    clearInterval(updateTimer);
    updateTimer = 0;
  };
  const startUpdateChecks = () => {
    if (updateTimer || document.visibilityState !== 'visible') return;
    updateTimer = window.setInterval(checkForUpdate, UPDATE_INTERVAL_MS);
  };

  startUpdateChecks();
  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
      checkForUpdate();
      startUpdateChecks();
    } else {
      stopUpdateChecks();
    }
  });
  addEventListener('pagehide', stopUpdateChecks, { once: true });
      } catch {}
    },
    {
      once: true
    }
  );
})();

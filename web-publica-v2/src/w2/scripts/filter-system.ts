type FilterConfig = {
  panel: HTMLElement;
  trigger: HTMLButtonElement;
  anchor: HTMLElement;
  closeSelector: string;
};

const configs: FilterConfig[] = [];

const removeLegacyFilterBackdrops = () => {
  for (const id of [
    'w2cmFilterBackdrop',
    'fondoFiltrosTorneos'
  ]) {
    document.getElementById(id)?.remove();
  }
};

const stripLegacy = (panel: HTMLElement) => {
  panel.classList.remove(
    'tournament-filter-portal',
    'filters-visible'
  );

  for (const property of [
    'top',
    'right',
    'bottom',
    'left',
    'height',
    'max-height',
    'transform',
    'translate'
  ]) {
    panel.style.removeProperty(property);
  }

  panel.removeAttribute('inert');
  panel.setAttribute('aria-modal', 'false');
};

const place = (config: FilterConfig) => {
  const { panel, anchor } = config;

  if (
    panel.parentElement !== anchor.parentElement ||
    panel.previousElementSibling !== anchor
  ) {
    anchor.insertAdjacentElement('afterend', panel);
  }

  stripLegacy(panel);
};

const closePanel = (
  config: FilterConfig,
  restoreFocus = true
) => {
  config.panel.classList.remove('open');
  config.panel.setAttribute('aria-hidden', 'true');
  config.trigger.setAttribute('aria-expanded', 'false');

  if (restoreFocus) {
    requestAnimationFrame(() => {
      config.trigger.focus({
        preventScroll: true
      });
    });
  }
};

const openPanel = (config: FilterConfig) => {
  for (const other of configs) {
    if (other !== config) {
      closePanel(other, false);
    }
  }

  place(config);
  config.panel.classList.add('open');
  config.panel.setAttribute('aria-hidden', 'false');
  config.trigger.setAttribute('aria-expanded', 'true');

  requestAnimationFrame(() => {
    config.panel.scrollIntoView({
      block: 'nearest',
      inline: 'nearest',
      behavior: matchMedia(
        '(prefers-reduced-motion: reduce)'
      ).matches
        ? 'auto'
        : 'smooth'
    });
  });
};

const togglePanel = (config: FilterConfig) => {
  if (config.panel.classList.contains('open')) {
    closePanel(config);
  } else {
    openPanel(config);
  }
};

const bindClose = (config: FilterConfig) => {
  const close =
    config.panel.querySelector<HTMLElement>(
      config.closeSelector
    );

  if (!close || close.dataset.w2FilterBound === 'true') {
    return;
  }

  close.dataset.w2FilterBound = 'true';

  close.addEventListener(
    'click',
    event => {
      event.preventDefault();
      event.stopImmediatePropagation();

      close.classList.add('w2-filter-pressed');

      window.setTimeout(() => {
        close.classList.remove('w2-filter-pressed');
        closePanel(config);
      }, 80);
    },
    {
      capture: true
    }
  );
};

const bindApply = (
  config: FilterConfig,
  selector: string
) => {
  const apply =
    config.panel.querySelector<HTMLButtonElement>(
      selector
    );

  if (!apply || apply.dataset.w2FilterBound === 'true') {
    return;
  }

  apply.dataset.w2FilterBound = 'true';

  apply.addEventListener('click', () => {
    requestAnimationFrame(() => {
      closePanel(config, false);
    });
  });
};

function register(
  panelId: string,
  triggerId: string,
  anchorSelector: string,
  closeSelector: string,
  applySelector?: string
) {
  const panel = document.getElementById(panelId);
  const trigger =
    document.getElementById(
      triggerId
    ) as HTMLButtonElement | null;
  const anchor =
    document.querySelector<HTMLElement>(
      anchorSelector
    );

  if (!panel || !trigger || !anchor) {
    return;
  }

  panel.classList.add('w2-filter-panel');
  panel.dataset.w2FilterPanel = '';

  const config: FilterConfig = {
    panel,
    trigger,
    anchor,
    closeSelector
  };

  configs.push(config);
  place(config);
  closePanel(config, false);

  trigger.dataset.w2FilterBound = 'true';

  trigger.addEventListener(
    'click',
    event => {
      event.preventDefault();
      event.stopImmediatePropagation();
      togglePanel(config);
    },
    {
      capture: true
    }
  );

  bindClose(config);

  if (applySelector) {
    bindApply(config, applySelector);
  }
}

function setupHistory() {
  const panel =
    document.getElementById('filtrosHistorial');

  if (!panel) {
    return;
  }

  panel.classList.add(
    'w2-filter-panel',
    'w2-filter-history'
  );
  panel.dataset.w2FilterPanel = '';

  let trigger =
    document.querySelector<HTMLButtonElement>(
      '[data-w2-history-filter-trigger]'
    );

  if (!trigger) {
    trigger = document.createElement('button');
    trigger.type = 'button';
    trigger.className = 'w2-filter-trigger';
    trigger.dataset.w2HistoryFilterTrigger = '';
    trigger.textContent = 'Filtros';
    trigger.setAttribute(
      'aria-controls',
      'filtrosHistorial'
    );

    panel.parentElement?.insertBefore(
      trigger,
      panel
    );
  }

  if (!panel.querySelector('[data-w2-filter-close]')) {
    const header = document.createElement('header');
    header.className = 'w2-filter-header';
    header.innerHTML =
      '<div>' +
      '<p class="w2-filter-context">TU ACTIVIDAD</p>' +
      '<h3>Filtros de reservas</h3>' +
      '</div>' +
      '<button type="button" data-w2-filter-close ' +
      'aria-label="Cerrar filtros">×</button>';

    panel.prepend(header);
  }

  const config: FilterConfig = {
    panel,
    trigger,
    anchor: trigger,
    closeSelector: '[data-w2-filter-close]'
  };

  configs.push(config);
  closePanel(config, false);

  trigger.addEventListener(
    'click',
    event => {
      event.preventDefault();
      event.stopImmediatePropagation();
      togglePanel(config);
    },
    {
      capture: true
    }
  );

  bindClose(config);

  const syncAvailability = () => {
    const available =
      !panel.classList.contains('hidden');

    trigger?.classList.toggle(
      'hidden',
      !available
    );

    if (!available) {
      closePanel(config, false);
    }
  };

  new MutationObserver(syncAvailability).observe(
    panel,
    {
      attributes: true,
      attributeFilter: ['class']
    }
  );

  syncAvailability();
}

function init() {
  removeLegacyFilterBackdrops();

  register(
    'w2cmFilters',
    'w2cmOpenFilters',
    '#w2cmCatalog>header',
    '#w2cmCloseFilters',
    '#w2cmApplyFilters'
  );

  register(
    'panelFiltrosTorneos',
    'abrirFiltrosTorneos',
    '.w2t-tools',
    '#cerrarFiltrosTorneos',
    '#aplicarFiltrosTorneos'
  );

  setupHistory();

  document.addEventListener('keydown', event => {
    if (event.key !== 'Escape') {
      return;
    }

    const config = [...configs]
      .reverse()
      .find(item =>
        item.panel.classList.contains('open')
      );

    if (!config) {
      return;
    }

    event.preventDefault();
    closePanel(config);
  });
}

if (document.readyState === 'loading') {
  addEventListener(
    'DOMContentLoaded',
    init,
    {
      once: true
    }
  );
} else {
  init();
}

export {};

const ROOT = document.documentElement;
const DELAY = 180;
let timer = 0;
let navigating = false;

function resetNavigation() {
  navigating = false;
  window.clearTimeout(timer);
  timer = 0;
  ROOT.removeAttribute('data-w2-navigation');
  ROOT.removeAttribute('data-w2-navigation-target');
}

function internalW2Link(anchor: HTMLAnchorElement, event: MouseEvent) {
  if (event.defaultPrevented || event.button !== 0) return null;
  if (event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return null;
  if (anchor.hasAttribute('download')) return null;
  if (anchor.target && anchor.target.toLowerCase() !== '_self') return null;
  if ((anchor.rel || '').split(/\s+/).includes('external')) return null;
  if (anchor.dataset.w2Transition === 'off') return null;
  const raw = anchor.getAttribute('href');
  if (!raw || raw.startsWith('#') || raw.startsWith('mailto:') || raw.startsWith('tel:')) return null;
  let target: URL;
  try { target = new URL(anchor.href, location.href); } catch { return null; }
  if (target.origin !== location.origin || !target.pathname.startsWith('/w2/')) return null;
  if (target.pathname === location.pathname && target.search === location.search && target.hash) return null;
  return target;
}

function beginNavigation(target: URL) {
  if (navigating) return;
  navigating = true;
  ROOT.dataset.w2NavigationTarget = target.pathname;
  timer = window.setTimeout(() => {
    if (navigating) ROOT.dataset.w2Navigation = 'pending';
  }, DELAY);
}

document.addEventListener('click', event => {
  const anchor = (event.target as Element | null)?.closest<HTMLAnchorElement>('a[href]');
  if (!anchor) return;
  const target = internalW2Link(anchor, event);
  if (target) beginNavigation(target);
});

addEventListener('pageshow', resetNavigation);
addEventListener('pagehide', () => window.clearTimeout(timer));
addEventListener('beforeunload', () => {
  if (navigating && !ROOT.hasAttribute('data-w2-navigation')) ROOT.dataset.w2Navigation = 'leaving';
});

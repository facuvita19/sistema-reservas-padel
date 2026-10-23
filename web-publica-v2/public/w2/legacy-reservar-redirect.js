(() => {
  const destination =
    '/w2/reservar' +
    window.location.search +
    window.location.hash;

  window.location.replace(destination);
})();

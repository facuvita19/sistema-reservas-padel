export const moneda = (valor: number, codigo = 'ARS') => new Intl.NumberFormat('es-AR', {
  style: 'currency', currency: codigo, maximumFractionDigits: Number.isInteger(valor) ? 0 : 2,
}).format(Number(valor || 0));

export const fecha = (valor: string) => new Intl.DateTimeFormat('es-AR', {
  day: 'numeric', month: 'long', year: 'numeric',
}).format(new Date(`${valor}T12:00:00`));

export const fechaCorta = (valor: string) => new Intl.DateTimeFormat('es-AR', {
  day: 'numeric', month: 'short',
}).format(new Date(`${valor}T12:00:00`));

export const fechaHora = (valor: string) => new Intl.DateTimeFormat('es-AR', {
  day: 'numeric', month: 'long', hour: '2-digit', minute: '2-digit',
}).format(new Date(valor));

export const etiqueta = (valor: string) => valor.toLowerCase().replaceAll('_', ' ')
  .replace(/^./, letra => letra.toUpperCase());

export const hora = (valor: string) => valor?.slice(0, 5) || '-';
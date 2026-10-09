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
export function rangoFechas(inicio:string,fin:string){
  const a=new Date(inicio+'T00:00:00');
  const b=new Date(fin+'T00:00:00');
  if(Number.isNaN(a.getTime())||Number.isNaN(b.getTime()))return `${fecha(inicio)} al ${fecha(fin)}`;
  const mismoAnio=a.getFullYear()===b.getFullYear();
  const mismoMes=mismoAnio&&a.getMonth()===b.getMonth();
  const mes=valor=>new Intl.DateTimeFormat('es-AR',{month:'long'}).format(valor);
  if(mismoMes)return `${a.getDate()} al ${b.getDate()} de ${mes(b)} de ${b.getFullYear()}`;
  if(mismoAnio)return `${a.getDate()} de ${mes(a)} al ${b.getDate()} de ${mes(b)} de ${b.getFullYear()}`;
  return `${a.getDate()} de ${mes(a)} de ${a.getFullYear()} al ${b.getDate()} de ${mes(b)} de ${b.getFullYear()}`;
}

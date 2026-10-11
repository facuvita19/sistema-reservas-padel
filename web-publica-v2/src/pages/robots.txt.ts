const sitio=(import.meta.env.PUBLIC_SITE_URL||'').trim().replace(/\/$/,'');
export const prerender=true;
export function GET(){const lineas=['User-agent: *','Allow: /','Disallow: /mis-datos/','Disallow: /mis-reservas/','Disallow: /restablecer/'];if(sitio)lineas.push('Sitemap: '+sitio+'/sitemap.xml');return new Response(lineas.join('\n')+'\n',{headers:{'Content-Type':'text/plain; charset=utf-8'}})}

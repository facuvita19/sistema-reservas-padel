package config;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;

public record ConfiguracionMercadoPago(boolean habilitado,String accessToken,String webhookSecret,URI apiBaseUrl,URI publicBaseUrl,String entorno) {
    public static ConfiguracionMercadoPago cargar(){return cargar(System::getenv,System::getProperty);}
    static ConfiguracionMercadoPago cargar(Function<String,String> entorno,Function<String,String> propiedades){
        boolean habilitado=bool(valor("MP_ENABLED","mp.enabled",entorno,propiedades));
        String token=valor("MP_ACCESS_TOKEN","mp.access.token",entorno,propiedades);
        String secreto=valor("MP_WEBHOOK_SECRET","mp.webhook.secret",entorno,propiedades);
        URI api=uri(valor("MP_API_BASE_URL","mp.api.base.url",entorno,propiedades),"https://api.mercadopago.com",false);
        URI publica=uri(valor("MP_PUBLIC_BASE_URL","mp.public.base.url",entorno,propiedades),null,true);
        String modo=normalizar(valor("MP_ENVIRONMENT","mp.environment",entorno,propiedades));if(modo==null)modo="sandbox";
        ConfiguracionMercadoPago c=new ConfiguracionMercadoPago(habilitado,normalizar(token),normalizar(secreto),api,publica,modo.toLowerCase());
        if(habilitado)c.validarCompleta();return c;
    }
    public static ConfiguracionMercadoPago desde(Map<String,String> valores){return cargar(valores::get,k->valores.get(k));}
    public boolean disponible(){return habilitado&&accessToken!=null&&webhookSecret!=null&&apiBaseUrl!=null&&publicBaseUrl!=null;}
    public URI webhookUrl(){return publicBaseUrl.resolve("/api/publica/pagos/mercado-pago/webhook");}
    public URI retornoExitoso(){return publicBaseUrl.resolve("/w2/cuenta?seccion=reservas&pago=aprobado");}
    public URI retornoPendiente(){return publicBaseUrl.resolve("/w2/cuenta?seccion=reservas&pago=pendiente");}
    public URI retornoFallido(){return publicBaseUrl.resolve("/w2/cuenta?seccion=reservas&pago=rechazado");}
    public void validarCompleta(){if(accessToken==null)falta("MP_ACCESS_TOKEN");if(webhookSecret==null)falta("MP_WEBHOOK_SECRET");if(publicBaseUrl==null)falta("MP_PUBLIC_BASE_URL");if(apiBaseUrl==null)falta("MP_API_BASE_URL");if(!"https".equalsIgnoreCase(apiBaseUrl.getScheme()))throw new IllegalStateException("MP_API_BASE_URL debe usar HTTPS.");if(!"https".equalsIgnoreCase(publicBaseUrl.getScheme())&&!esLocal(publicBaseUrl))throw new IllegalStateException("MP_PUBLIC_BASE_URL debe usar HTTPS fuera del entorno local.");if(!entorno.equals("sandbox")&&!entorno.equals("production"))throw new IllegalStateException("MP_ENVIRONMENT debe ser sandbox o production.");}
    public String resumenSeguro(){return "Mercado Pago "+(disponible()?"configurado":"no disponible")+" ("+entorno+")";}
    private static String valor(String env,String prop,Function<String,String> e,Function<String,String> p){String v=e.apply(env);return normalizar(v)!=null?v:p.apply(prop);}
    private static boolean bool(String v){return v!=null&&Boolean.parseBoolean(v.trim());}
    private static URI uri(String v,String defecto,boolean publica){String x=normalizar(v);if(x==null)x=defecto;if(x==null)return null;try{URI u=URI.create(x.endsWith("/")?x:x+"/");if(u.getHost()==null)throw new IllegalArgumentException();return u;}catch(RuntimeException ex){throw new IllegalStateException((publica?"MP_PUBLIC_BASE_URL":"MP_API_BASE_URL")+" no es una URL válida.");}}
    private static String normalizar(String v){return v==null||v.isBlank()?null:v.trim();}
    private static boolean esLocal(URI u){return "localhost".equalsIgnoreCase(u.getHost())||"127.0.0.1".equals(u.getHost());}
    private static void falta(String nombre){throw new IllegalStateException("Falta configurar "+nombre+".");}
    @Override public String toString(){return resumenSeguro();}
}

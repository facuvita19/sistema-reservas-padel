import type { Page, Route } from '@playwright/test';

const complejo={nombre:'Arena Pádel E2E',direccion:'Av. Pruebas 123',telefono:'1100000000',whatsapp:'5491100000000',email:'e2e@example.test',moneda:'ARS',colorPrincipal:'#3d9b70',anticipacionMinimaHoras:2,minutosReservaPendiente:10,porcentajeSenia:25,pagoTransferenciaDisponible:true,pagoAlias:'arena.e2e',pagoTitular:'Arena E2E',pagoEntidad:'Banco de prueba',pagoInstrucciones:'Datos exclusivos de pruebas automatizadas.'};
const canchas=[{id:1,nombre:'Cancha E2E',tipo:'Pádel',superficie:'Césped sintético',descripcion:'Cancha preparada para pruebas.',tieneIluminacion:true,duracionMinutos:90,precio:25000,importeSenia:6250}];
const torneos=[{id:1,nombre:'Torneo E2E',descripcion:'Competencia controlada para pruebas.',fechaInicio:'2026-10-24',fechaFin:'2026-10-26',estadoPortal:'PROXIMO',estado:'PUBLICADO',inscripcionDisponible:true,cantidadCategorias:1,ramas:['FEMENINA'],formatos:['GRUPOS_ELIMINACION'],precioMinimo:30000,precioMaximo:30000,cupoTotal:20,parejasConfirmadas:3,cuposDisponibles:17,partidosTotales:0,partidosFinalizados:0}];
const detalle={...torneos[0],reglamento:'Reglamento de prueba E2E.',categorias:[{id:11,nombre:'4ta',rama:'FEMENINA',formatoCompetencia:'GRUPOS_ELIMINACION',precioInscripcion:30000,cupoParejas:20,parejasConfirmadas:3,cuposDisponibles:17,disponible:true,premioDescripcion:'Premio de prueba'}]};

function json(route:Route,data:unknown,status=200){return route.fulfill({status,contentType:'application/json; charset=utf-8',body:JSON.stringify(data)})}
export async function mockPublicApi(page:Page){
 await page.route('**/api/publica/**',async route=>{
  const request=route.request(),url=new URL(request.url()),p=url.pathname.replace('/api/publica',''),method=request.method();
  if(p==='/complejo')return json(route,complejo);
  if(p==='/canchas')return json(route,canchas);
  if(p.startsWith('/disponibilidad'))return json(route,{disponibleEseDia:true,horarios:[{horaInicio:'17:00:00',horaFin:'18:30:00',precio:25000,importeSenia:6250}]});
  if(p==='/auth/sesion')return json(route,{autenticado:false,perfil:null,vencimiento:null});
  if(p==='/auth/login'&&method==='POST')return json(route,{autenticado:false,mensaje:'Credenciales inválidas de prueba.'},401);
  if(p==='/auth/recuperar'&&method==='POST')return json(route,{mensaje:'Si la cuenta existe, recibirás instrucciones.'});
  if(p==='/auth/logout'&&method==='POST')return json(route,{ok:true});
  if(p==='/torneos')return json(route,torneos);
  if(p==='/torneos/1')return json(route,detalle);
  if(p.startsWith('/solicitudes/'))return json(route,{mensaje:'Código de prueba inexistente.'},404);
  if(p==='/cliente/perfil'||p==='/cliente/reservas')return json(route,{mensaje:'Sesión requerida.'},401);
  return json(route,{mensaje:'Endpoint E2E no simulado: '+p},404);
 });
}

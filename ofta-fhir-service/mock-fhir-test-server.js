// Servidor mock temporal para probar la integración Retrofit/FHIR desde el teléfono vía `adb reverse`.
// NO reemplaza al microservicio HAPI FHIR (`ofta-fhir-service`); úsalo solo cuando Maven/Docker no estén disponibles.
// Ejecutar: node mock-fhir-test-server.js  (puerto 8080)
const http = require('http');
let pid = 2000, drid = 3000;
const patients = new Map();
const reports = new Map();
const json = (res, code, obj) => { res.writeHead(code, {'Content-Type':'application/fhir+json'}); res.end(JSON.stringify(obj)); };
const readBody = (req) => new Promise(r => { let b=''; req.on('data',c=>b+=c); req.on('end',()=>{ try{r(JSON.parse(b||'{}'))}catch(e){r({})} }); });
const filtrarPorIdentifier = (recursos, url) => {
  const q = url.searchParams.get('identifier');
  if (!q) return recursos;
  return recursos.filter(r => (r.identifier || []).some(i => i.value === q || `${i.system}|${i.value}` === q));
};
http.createServer(async (req,res) => {
  const url = new URL(req.url, 'http://127.0.0.1');
  if (req.method==='GET' && url.pathname==='/api/status') return json(res,200,{service:'ofta-fhir-service-mock',status:'UP',fhirBasePath:'/fhir',fhirVersion:'R4',note:'Mock de prueba USB (no HAPI). Usar ofta-fhir-service real para evaluación completa.'});
  if (req.method==='GET' && url.pathname==='/fhir/metadata') return json(res,200,{resourceType:'CapabilityStatement',status:'active',fhirVersion:'4.0.1',format:['json'],rest:[{mode:'server',resource:[{type:'Patient'},{type:'DiagnosticReport'}]}]});
  if (url.pathname==='/fhir/Patient') {
    if (req.method==='POST') { const b=await readBody(req); b.resourceType='Patient'; b.id=String(++pid); patients.set(b.id,b); return json(res,201,b); }
    if (req.method==='GET') { const lista=filtrarPorIdentifier([...patients.values()],url); return json(res,200,{resourceType:'Bundle',type:'searchset',total:lista.length,entry:lista.map(r=>({resource:r}))}); }
  }
  if (url.pathname.startsWith('/fhir/Patient/')) {
    const id=url.pathname.split('/').pop();
    if (req.method==='GET') return patients.has(id)?json(res,200,patients.get(id)):json(res,404,{resourceType:'OperationOutcome',issue:[{severity:'error',diagnostics:'Not found'}]});
    if (req.method==='PUT') { const b=await readBody(req); b.resourceType='Patient'; b.id=id; patients.set(id,b); return json(res,200,b); }
  }
  if (url.pathname==='/fhir/DiagnosticReport') {
    if (req.method==='POST') { const b=await readBody(req); b.resourceType='DiagnosticReport'; b.id=String(++drid); reports.set(b.id,b); return json(res,201,b); }
    if (req.method==='GET') { const lista=filtrarPorIdentifier([...reports.values()],url); return json(res,200,{resourceType:'Bundle',type:'searchset',total:lista.length,entry:lista.map(r=>({resource:r}))}); }
  }
  if (url.pathname.startsWith('/fhir/DiagnosticReport/')) {
    const id=url.pathname.split('/').pop();
    if (req.method==='GET') return reports.has(id)?json(res,200,reports.get(id)):json(res,404,{resourceType:'OperationOutcome',issue:[{severity:'error',diagnostics:'Not found'}]});
    if (req.method==='PUT') { const b=await readBody(req); b.resourceType='DiagnosticReport'; b.id=id; reports.set(id,b); return json(res,200,b); }
  }
  json(res,404,{resourceType:'OperationOutcome',issue:[{severity:'error',diagnostics:'Unknown '+req.method+' '+url.pathname}]});
}).listen(8080,'127.0.0.1',()=>console.log('Mock FHIR test server on 127.0.0.1:8080 (adb reverse activo)'));

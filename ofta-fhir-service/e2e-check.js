// Verificación E2E del microservicio HAPI FHIR real (no usa el mock).
// Requisito: ofta-fhir-service en ejecución en 127.0.0.1:8080
//   cd ofta-fhir-service && mvn spring-boot:run
// Ejecutar: node e2e-check.js
const BASE = process.env.FHIR_BASE || 'http://127.0.0.1:8080';
const RUT_SYSTEM = 'https://oftaapp.demo/identifier/rut';
const EXAM_SYSTEM = 'https://oftaapp.demo/identifier/examen';
const RUT = '15.790.867-2';
const EXAM_ID = 'exa-e2e-' + Date.now();
const cabeceras = { 'Content-Type': 'application/fhir+json', Accept: 'application/fhir+json' };

let fallos = 0;
function verificar(condicion, etiqueta, detalle) {
  console.log(`${condicion ? 'OK  ' : 'FAIL'} ${etiqueta}${detalle ? ' :: ' + detalle : ''}`);
  if (!condicion) fallos++;
}
async function llamar(ruta, opciones) {
  const res = await fetch(BASE + ruta, opciones);
  const texto = await res.text();
  let cuerpo;
  try { cuerpo = JSON.parse(texto); } catch { cuerpo = texto; }
  return { status: res.status, cuerpo };
}

(async () => {
  const status = await llamar('/api/status');
  verificar(status.status === 200 && status.cuerpo.status === 'UP', 'GET /api/status', `HTTP ${status.status} ${status.cuerpo.status}`);

  const meta = await llamar('/fhir/metadata');
  verificar(meta.status === 200 && meta.cuerpo.fhirVersion?.startsWith('4.'), 'GET /fhir/metadata', `HTTP ${meta.status} fhirVersion=${meta.cuerpo.fhirVersion}`);

  const paciente = {
    resourceType: 'Patient',
    identifier: [{ system: RUT_SYSTEM, value: RUT }],
    name: [{ text: 'Camila Rojas', family: 'Rojas', given: ['Camila'] }],
    birthDate: '1995-03-12', active: true
  };
  const creado = await llamar('/fhir/Patient', { method: 'POST', headers: cabeceras, body: JSON.stringify(paciente) });
  const patientId = creado.cuerpo?.id;
  verificar(creado.status === 201 && !!patientId, 'POST /fhir/Patient', `HTTP ${creado.status} id=${patientId}`);

  const busquedaPaciente = await llamar('/fhir/Patient?identifier=' + encodeURIComponent(`${RUT_SYSTEM}|${RUT}`));
  verificar(busquedaPaciente.status === 200 && busquedaPaciente.cuerpo.entry?.length === 1 &&
    busquedaPaciente.cuerpo.entry[0].resource.id === patientId,
    'GET /fhir/Patient?identifier=RUT', `HTTP ${busquedaPaciente.status} total=${busquedaPaciente.cuerpo.total}`);

  const actualizado = await llamar('/fhir/Patient/' + patientId, {
    method: 'PUT', headers: cabeceras,
    body: JSON.stringify({ ...paciente, id: patientId, telecom: [{ system: 'email', value: 'camila@example.com' }] })
  });
  verificar(actualizado.status === 200, 'PUT /fhir/Patient/{id}', `HTTP ${actualizado.status}`);

  const trasActualizar = await llamar('/fhir/Patient?identifier=' + encodeURIComponent(`${RUT_SYSTEM}|${RUT}`));
  verificar(trasActualizar.cuerpo.entry?.length === 1, 'Sin duplicados de Patient tras PUT', `total=${trasActualizar.cuerpo.total}`);

  const reporte = {
    resourceType: 'DiagnosticReport',
    identifier: [{ system: EXAM_SYSTEM, value: EXAM_ID }],
    status: 'registered',
    code: { text: 'Fondo de ojo' },
    subject: { reference: `Patient/${patientId}`, display: 'Camila Rojas' },
    effectiveDateTime: '2026-05-10T10:30:00Z',
    conclusion: 'Sin hallazgos relevantes.'
  };
  const creadoReporte = await llamar('/fhir/DiagnosticReport', { method: 'POST', headers: cabeceras, body: JSON.stringify(reporte) });
  const reportId = creadoReporte.cuerpo?.id;
  verificar(creadoReporte.status === 201 && !!reportId, 'POST /fhir/DiagnosticReport', `HTTP ${creadoReporte.status} id=${reportId}`);

  const busquedaReporte = await llamar('/fhir/DiagnosticReport?identifier=' + encodeURIComponent(`${EXAM_SYSTEM}|${EXAM_ID}`));
  verificar(busquedaReporte.status === 200 && busquedaReporte.cuerpo.entry?.length === 1,
    'GET /fhir/DiagnosticReport?identifier=examen', `HTTP ${busquedaReporte.status} total=${busquedaReporte.cuerpo.total}`);

  const porSujeto = await llamar('/fhir/DiagnosticReport?subject=Patient/' + patientId);
  verificar(porSujeto.cuerpo.entry?.length === 1 &&
    porSujeto.cuerpo.entry[0].resource.subject.reference === `Patient/${patientId}`,
    'GET /fhir/DiagnosticReport?subject (referencia al Patient real)', `total=${porSujeto.cuerpo.total}`);

  const cambioEstado = await llamar('/fhir/DiagnosticReport/' + reportId, {
    method: 'PUT', headers: cabeceras,
    body: JSON.stringify({ ...reporte, id: reportId, status: 'final' })
  });
  verificar(cambioEstado.status === 200 && cambioEstado.cuerpo.status === 'final', 'PUT /fhir/DiagnosticReport/{id}', `HTTP ${cambioEstado.status} status=${cambioEstado.cuerpo.status}`);

  const trasCambio = await llamar('/fhir/DiagnosticReport?identifier=' + encodeURIComponent(`${EXAM_SYSTEM}|${EXAM_ID}`));
  verificar(trasCambio.cuerpo.entry?.length === 1, 'Sin duplicados de DiagnosticReport tras cambio de estado', `total=${trasCambio.cuerpo.total}`);

  console.log(fallos === 0 ? '\nE2E HAPI FHIR REAL: OK' : `\nE2E HAPI FHIR REAL: ${fallos} fallo(s)`);
  process.exit(fallos === 0 ? 0 : 1);
})().catch(error => { console.error('E2E error:', error.message); process.exit(1); });

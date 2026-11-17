package cl.duoc.seguros.siniestros;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class SiniestrosService {
    private static final ZoneId CHILE = ZoneId.of("America/Santiago");
    private static final Map<String, String> LIQUIDADORES = Map.of(
        "LIQ-01", "VEHICULO", "LIQ-02", "HOGAR",
        "LIQ-03", "VEHICULO", "LIQ-04", "HOGAR"
    );
    private final SiniestroRepo siniestros;
    private final GestionRepo gestiones;
    private final SolicitudRepo solicitudes;
    private final DocumentoRepo documentos;
    private final AvisoRepo avisos;
    private final CatalogosCliente catalogos;

    public SiniestrosService(SiniestroRepo siniestros, GestionRepo gestiones, SolicitudRepo solicitudes,
                             DocumentoRepo documentos, AvisoRepo avisos, CatalogosCliente catalogos) {
        this.siniestros = siniestros;
        this.gestiones = gestiones;
        this.solicitudes = solicitudes;
        this.documentos = documentos;
        this.avisos = avisos;
        this.catalogos = catalogos;
    }

    private String texto(String valor, String nombre) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta el campo " + nombre);
        }
        return valor.trim();
    }

    private void rol(Actor actor, String esperado) {
        if (!esperado.equalsIgnoreCase(actor.rol())) {
            throw new SecurityException("Rol no autorizado");
        }
    }

    private String rut(String valor) {
        String limpio = texto(valor, "RUT").replace(".", "").replace("-", "").replace(" ", "").toUpperCase();
        if (!limpio.matches("[0-9]{7,8}[0-9K]")) {
            throw new IllegalArgumentException("RUT inválido");
        }
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        int suma = 0;
        int factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int verificador = 11 - suma % 11;
        String esperado = verificador == 11 ? "0" : verificador == 10 ? "K" : Integer.toString(verificador);
        if (!esperado.equals(limpio.substring(limpio.length() - 1))) {
            throw new IllegalArgumentException("Dígito verificador del RUT inválido");
        }
        return cuerpo + "-" + esperado;
    }

    private Siniestro encontrar(String numero) {
        return siniestros.findByNumero(numero).orElseThrow(NoSuchElementException::new);
    }

    private void acceso(Siniestro siniestro, Actor actor) {
        switch (texto(actor.rol(), "rol").toUpperCase()) {
            case "ASEGURADO" -> {
                if (!siniestro.aseguradoRut.equals(rut(actor.id()))) {
                    throw new SecurityException("Siniestro de otra persona");
                }
            }
            case "LIQUIDADOR" -> {
                if (!Objects.equals(siniestro.liquidadorId, actor.id())) {
                    throw new SecurityException("Siniestro no asignado");
                }
            }
            case "ANALISTA" -> {
                if (!"ADM-01".equals(actor.id()) && !"ADM-02".equals(actor.id())) {
                    throw new SecurityException("Analista desconocido");
                }
            }
            default -> throw new SecurityException("Rol desconocido");
        }
    }

    private void liquidador(Siniestro siniestro, Actor actor) {
        rol(actor, "LIQUIDADOR");
        acceso(siniestro, actor);
    }

    public SiniestroResponse reportar(Actor actor, ReporteRequest request) {
        rol(actor, "ASEGURADO");
        String aseguradoRut = rut(actor.id());
        String companiaCodigo = texto(request.compania(), "compania").toUpperCase();
        String tipoCodigo = texto(request.tipo(), "tipo").toUpperCase();
        CatalogosCliente.Compania compania = catalogos.compania(companiaCodigo);
        CatalogosCliente.Tipo tipo = catalogos.tipo(tipoCodigo);
        if (!compania.activa()) {
            throw new IllegalStateException("No se aceptan reportes para compañías inactivas");
        }
        if (!compania.lineas().contains(tipo.linea())) {
            throw new IllegalArgumentException("La compañía no admite este tipo de siniestro");
        }
        if (request.fechaOcurrencia() == null) {
            throw new IllegalArgumentException("Falta la fecha de ocurrencia");
        }
        LocalDate hoy = LocalDate.now(CHILE);
        if (request.fechaOcurrencia().isAfter(hoy)) {
            throw new IllegalArgumentException("La fecha no puede ser futura");
        }
        Siniestro s = new Siniestro();
        s.aseguradoRut = aseguradoRut;
        s.compania = companiaCodigo;
        s.tipo = tipoCodigo;
        s.tipoNombre = tipo.nombre();
        s.linea = tipo.linea();
        s.fechaOcurrencia = request.fechaOcurrencia();
        s.fechaReporte = LocalDateTime.now(CHILE);
        s.comuna = texto(request.comuna(), "comuna");
        s.descripcion = texto(request.descripcion(), "descripcion");
        s.etapa = "Recibido";
        s.actualizado = s.fechaReporte;
        s.fechaEtapa = s.fechaReporte;
        s.fueraDePlazo = ChronoUnit.DAYS.between(request.fechaOcurrencia(), hoy) > 30;
        siniestros.saveAndFlush(s);
        if (s.id > 999999) {
            throw new IllegalStateException("Se agotó la numeración anual");
        }
        s.numero = String.format("SIN-%02d-%06d", hoy.getYear() % 100, s.id);
        agregarGestion(s, actor.id(), "REPORTE", "Recibimos el reporte de tu siniestro.", true, null, false);
        return convertir(s);
    }

    @Transactional(readOnly = true)
    public List<SiniestroResponse> listar(Actor actor) {
        List<Siniestro> lista = switch (texto(actor.rol(), "rol").toUpperCase()) {
            case "ASEGURADO" -> siniestros.findByAseguradoRut(rut(actor.id()));
            case "LIQUIDADOR" -> {
                if (!LIQUIDADORES.containsKey(actor.id())) {
                    throw new SecurityException("Liquidador desconocido");
                }
                yield siniestros.findByLiquidadorId(actor.id());
            }
            case "ANALISTA" -> {
                if (!"ADM-01".equals(actor.id()) && !"ADM-02".equals(actor.id())) {
                    throw new SecurityException("Analista desconocido");
                }
                yield siniestros.findAll();
            }
            default -> throw new SecurityException("Rol desconocido");
        };
        return lista.stream().sorted(Comparator.comparing((Siniestro s) -> "Cerrado".equals(s.etapa))
            .thenComparing((Siniestro s) -> s.actualizado, Comparator.reverseOrder())).map(this::convertir).toList();
    }

    @Transactional(readOnly = true)
    public SiniestroResponse detalle(String numero, Actor actor) {
        Siniestro s = encontrar(numero);
        acceso(s, actor);
        return convertir(s);
    }

    @Transactional(readOnly = true)
    public List<GestionResponse> historial(String numero, Actor actor) {
        Siniestro s = encontrar(numero);
        acceso(s, actor);
        boolean asegurado = "ASEGURADO".equalsIgnoreCase(actor.rol());
        return gestiones.findBySiniestroIdOrderByFechaHoraAscIdAsc(s.id).stream()
            .filter(g -> !asegurado || g.visibleAsegurado)
            .map(g -> new GestionResponse(String.valueOf(g.id), g.fechaHora.toString(), g.descripcion, g.etapaDestino, g.responsable))
            .toList();
    }

    public SiniestroResponse asignar(String numero, Actor actor, AsignacionRequest request) {
        rol(actor, "ANALISTA");
        Siniestro s = encontrar(numero);
        acceso(s, actor);
        String id = texto(request.liquidadorId(), "liquidadorId");
        if (!LIQUIDADORES.containsKey(id)) {
            throw new IllegalArgumentException("Liquidador inexistente o inactivo");
        }
        if (!LIQUIDADORES.get(id).equals(s.linea)) {
            throw new IllegalArgumentException("El liquidador no pertenece al equipo del siniestro");
        }
        if ("Cerrado".equals(s.etapa)) {
            throw new IllegalStateException("No se puede asignar un siniestro cerrado");
        }
        s.liquidadorId = id;
        s.equipo = s.linea.equals("VEHICULO") ? "Vehículos" : "Hogar";
        agregarGestion(s, actor.id(), "ASIGNACION", "Tu caso fue asignado al equipo " + s.equipo + ".", true, null, true);
        if ("Recibido".equals(s.etapa)) {
            s.etapa = "En evaluación";
            s.fechaEtapa = LocalDateTime.now(CHILE);
            agregarGestion(s, actor.id(), "ETAPA", "Tu caso pasó a En evaluación.", true, s.etapa, true);
        }
        return convertir(s);
    }

    public GestionResponse gestionar(String numero, Actor actor, GestionRequest request) {
        Siniestro s = encontrar(numero);
        liquidador(s, actor);
        if ("Cerrado".equals(s.etapa)) {
            throw new IllegalStateException("No se pueden registrar gestiones en un caso cerrado");
        }
        Gestion g = agregarGestion(s, actor.id(), "GESTION", texto(request.descripcion(), "descripcion"),
            request.visibleAsegurado(), null, false);
        return new GestionResponse(g.id.toString(), g.fechaHora.toString(), g.descripcion, null, g.responsable);
    }

    public SolicitudResponse solicitar(String numero, Actor actor, SolicitudRequest request) {
        Siniestro s = encontrar(numero);
        liquidador(s, actor);
        if (!"En evaluación".equals(s.etapa)) {
            throw new IllegalStateException("Solo se solicitan documentos en evaluación");
        }
        SolicitudDocumento d = new SolicitudDocumento();
        d.siniestro = s;
        d.tipo = texto(request.tipoDocumento(), "tipoDocumento");
        d.comentario = request.comentario();
        d.solicitante = actor.id();
        d.fechaSolicitud = LocalDateTime.now(CHILE);
        d.entregado = false;
        solicitudes.save(d);
        agregarGestion(s, actor.id(), "SOLICITUD", "Necesitamos el documento: " + d.tipo, true, null, true);
        return convertir(d);
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> solicitudes(String numero, Actor actor) {
        Siniestro s = encontrar(numero);
        acceso(s, actor);
        return solicitudes.findBySiniestroIdOrderByFechaSolicitudAscIdAsc(s.id).stream().map(this::convertir).toList();
    }

    public DocumentoResponse adjuntar(String numero, Actor actor, String tipo, Long solicitudId, MultipartFile archivo) {
        Siniestro s = encontrar(numero);
        acceso(s, actor);
        if ("ANALISTA".equalsIgnoreCase(actor.rol())) {
            throw new SecurityException("El analista no puede adjuntar documentos");
        }
        if ("Cerrado".equals(s.etapa)) {
            throw new IllegalStateException("No se permiten adjuntos en casos cerrados");
        }
        if (archivo == null || archivo.isEmpty() || archivo.getSize() > 5L * 1024 * 1024) {
            throw new IllegalArgumentException("El archivo debe ocupar entre 1 byte y 5 MB");
        }
        if (documentos.countBySiniestroId(s.id) >= 15) {
            throw new IllegalStateException("El límite es 15 documentos por siniestro");
        }
        byte[] datos;
        try {
            datos = archivo.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("No fue posible leer el documento");
        }
        boolean pdf = datos.length >= 4 && datos[0] == '%' && datos[1] == 'P' && datos[2] == 'D' && datos[3] == 'F';
        boolean png = datos.length >= 8 && (datos[0] & 255) == 137 && datos[1] == 'P' && datos[2] == 'N' && datos[3] == 'G'
            && datos[4] == 13 && datos[5] == 10 && datos[6] == 26 && datos[7] == 10;
        boolean jpg = datos.length >= 3 && (datos[0] & 255) == 255 && (datos[1] & 255) == 216 && (datos[2] & 255) == 255;
        if (!(pdf || png || jpg)) {
            throw new IllegalArgumentException("Solo se aceptan archivos PDF, PNG o JPEG");
        }
        SolicitudDocumento solicitud = null;
        if (solicitudId != null) {
            solicitud = solicitudes.findById(solicitudId).orElseThrow(NoSuchElementException::new);
            if (!solicitud.siniestro.id.equals(s.id)) {
                throw new SecurityException("Solicitud de otro siniestro");
            }
            if (solicitud.entregado) {
                throw new IllegalStateException("La solicitud ya fue entregada");
            }
        }
        DocumentoAdjunto d = new DocumentoAdjunto();
        d.siniestro = s;
        d.tipo = texto(tipo, "tipoDocumento");
        d.nombreArchivo = texto(archivo.getOriginalFilename(), "nombreArchivo");
        d.tipoMime = pdf ? "application/pdf" : png ? "image/png" : "image/jpeg";
        d.contenido = datos;
        d.fechaCarga = LocalDateTime.now(CHILE);
        d.solicitudId = solicitudId;
        documentos.save(d);
        if (solicitud != null) {
            solicitud.entregado = true;
        }
        agregarGestion(s, actor.id(), "DOCUMENTO", "Recibimos el documento: " + d.tipo, true, null, false);
        return convertir(d);
    }

    @Transactional(readOnly = true)
    public List<DocumentoResponse> documentos(String numero, Actor actor) {
        Siniestro s = encontrar(numero);
        acceso(s, actor);
        return documentos.findBySiniestroIdOrderByFechaCargaAscIdAsc(s.id).stream().map(this::convertir).toList();
    }

    @Transactional(readOnly = true)
    public DocumentoAdjunto descargar(String numero, Long idDocumento, Actor actor) {
        Siniestro s = encontrar(numero);
        acceso(s, actor);
        DocumentoAdjunto d = documentos.findById(idDocumento).orElseThrow(NoSuchElementException::new);
        if (!d.siniestro.id.equals(s.id)) {
            throw new SecurityException("Documento de otro siniestro");
        }
        return d;
    }

    public SiniestroResponse avanzar(String numero, Actor actor, CambioEtapaRequest request) {
        Siniestro s = encontrar(numero);
        liquidador(s, actor);
        String destino = texto(request.etapa(), "etapa");
        if ("En liquidación".equals(destino)) {
            if (!"En evaluación".equals(s.etapa)) {
                throw new IllegalStateException("El siniestro debe estar En evaluación");
            }
            if (solicitudes.existsBySiniestroIdAndEntregadoFalse(s.id)) {
                throw new IllegalStateException("Faltan documentos solicitados por entregar");
            }
            s.etapa = destino;
            s.fechaEtapa = LocalDateTime.now(CHILE);
            agregarGestion(s, actor.id(), "ETAPA", "Tu caso pasó a En liquidación.", true, destino, true);
            return convertir(s);
        }
        if (!"Cerrado".equals(destino)) {
            throw new IllegalArgumentException("Etapa inválida o retroceso no permitido");
        }
        String resultado = texto(request.resultado(), "resultado");
        if (!List.of("Aprobado", "Rechazado", "Desistido").contains(resultado)) {
            throw new IllegalArgumentException("Resultado de cierre inválido");
        }
        boolean desdeLiquidacion = "En liquidación".equals(s.etapa);
        boolean desdeEvaluacion = "En evaluación".equals(s.etapa);
        if (!(desdeLiquidacion && List.of("Aprobado", "Rechazado").contains(resultado)
            || desdeEvaluacion && List.of("Rechazado", "Desistido").contains(resultado))) {
            throw new IllegalStateException("Resultado no permitido desde esta etapa");
        }
        cerrar(s, actor.id(), resultado, texto(request.mensajeCierre(), "mensajeCierre"), null);
        return convertir(s);
    }

    public SiniestroResponse desistir(String numero, Actor actor, DesistimientoRequest request) {
        rol(actor, "ASEGURADO");
        Siniestro s = encontrar(numero);
        acceso(s, actor);
        if (!List.of("Recibido", "En evaluación").contains(s.etapa)) {
            throw new IllegalStateException("Solo se puede desistir antes de liquidación");
        }
        cerrar(s, actor.id(), "Desistido", "Tu caso fue cerrado a solicitud tuya.", texto(request.motivo(), "motivo"));
        return convertir(s);
    }

    @Transactional(readOnly = true)
    public List<AvisoResponse> avisos(Actor actor) {
        rol(actor, "ASEGURADO");
        return avisos.findByDestinatarioRutOrderByFechaHoraDescIdDesc(rut(actor.id())).stream()
            .map(a -> new AvisoResponse(a.id, a.siniestro.numero, a.mensaje, a.fechaHora.toString(), a.leido)).toList();
    }

    public void leerAviso(Long id, Actor actor) {
        rol(actor, "ASEGURADO");
        Aviso aviso = avisos.findById(id).orElseThrow(NoSuchElementException::new);
        if (!aviso.destinatarioRut.equals(rut(actor.id()))) {
            throw new SecurityException("Aviso de otra persona");
        }
        aviso.leido = true;
    }

    private void cerrar(Siniestro s, String responsable, String resultado, String mensaje, String motivo) {
        s.etapa = "Cerrado";
        s.fechaEtapa = LocalDateTime.now(CHILE);
        s.resultado = resultado;
        s.mensajeCierre = mensaje;
        s.motivoDesistimiento = motivo;
        agregarGestion(s, responsable, "CIERRE",
            "Tu caso terminó con resultado " + resultado + ". " + mensaje, true, "Cerrado", true);
    }

    private Gestion agregarGestion(Siniestro s, String responsable, String tipo, String descripcion,
                                   boolean visible, String etapaDestino, boolean generaAviso) {
        Gestion g = new Gestion();
        g.siniestro = s;
        g.fechaHora = LocalDateTime.now(CHILE);
        g.responsable = responsable;
        g.tipo = tipo;
        g.descripcion = descripcion;
        g.visibleAsegurado = visible;
        g.etapaDestino = etapaDestino;
        gestiones.save(g);
        s.actualizado = g.fechaHora;
        if (generaAviso && visible) {
            Aviso a = new Aviso();
            a.siniestro = s;
            a.destinatarioRut = s.aseguradoRut;
            a.mensaje = s.numero + ": " + descripcion;
            a.fechaHora = g.fechaHora;
            a.leido = false;
            avisos.save(a);
        }
        return g;
    }

    private SiniestroResponse convertir(Siniestro s) {
        String paso = switch (s.etapa) {
            case "Recibido" -> "Estamos revisando tu reporte para asignarlo. No necesitas hacer nada por ahora.";
            case "En evaluación" -> solicitudes.existsBySiniestroIdAndEntregadoFalse(s.id)
                ? "Necesitamos que nos envíes los documentos pendientes."
                : "Tu liquidador está revisando tu caso. Si necesitamos algo más, te avisaremos.";
            case "En liquidación" -> "Tenemos todo lo necesario. Estamos definiendo la resolución con tu compañía.";
            default -> "Tu caso terminó. Revisa el resultado y el mensaje de cierre.";
        };
        return new SiniestroResponse(s.numero, s.numero, s.compania, s.tipoNombre,
            s.fechaOcurrencia.toString(), s.fechaReporte.toString(), s.etapa, s.equipo,
            s.actualizado.toString(), paso, s.fueraDePlazo, s.resultado, s.mensajeCierre);
    }

    private SolicitudResponse convertir(SolicitudDocumento s) {
        return new SolicitudResponse(s.id, s.tipo, s.comentario, s.fechaSolicitud.toString(),
            s.entregado ? "Entregado" : "Pendiente");
    }

    private DocumentoResponse convertir(DocumentoAdjunto d) {
        return new DocumentoResponse(d.id, d.tipo, d.nombreArchivo, d.fechaCarga.toString(), d.solicitudId);
    }
}

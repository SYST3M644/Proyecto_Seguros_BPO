package cl.duoc.seguros.siniestros;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SiniestroRepo extends JpaRepository<Siniestro, Long> {
    Optional<Siniestro> findByNumero(String numero);
    List<Siniestro> findByAseguradoRut(String rut);
    List<Siniestro> findByLiquidadorId(String liquidadorId);
}
interface GestionRepo extends JpaRepository<Gestion, Long> {
    List<Gestion> findBySiniestroIdOrderByFechaHoraAscIdAsc(Long siniestroId);
}
interface AvisoRepo extends JpaRepository<Aviso, Long> {
    List<Aviso> findByDestinatarioRutOrderByFechaHoraDescIdDesc(String rut);
}
interface SolicitudRepo extends JpaRepository<SolicitudDocumento, Long> {
    List<SolicitudDocumento> findBySiniestroIdOrderByFechaSolicitudAscIdAsc(Long siniestroId);
    boolean existsBySiniestroIdAndEntregadoFalse(Long siniestroId);
}
interface DocumentoRepo extends JpaRepository<DocumentoAdjunto, Long> {
    List<DocumentoAdjunto> findBySiniestroIdOrderByFechaCargaAscIdAsc(Long siniestroId);
    long countBySiniestroId(Long siniestroId);
}

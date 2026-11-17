package cl.duoc.seguros.siniestros;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documentos_adjuntos")
public class DocumentoAdjunto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    Siniestro siniestro;
    @Column(nullable = false)
    String tipo;
    @Column(nullable = false)
    String nombreArchivo;
    @Column(nullable = false)
    String tipoMime;
    @Lob @Column(nullable = false)
    byte[] contenido;
    @Column(nullable = false)
    LocalDateTime fechaCarga;
    Long solicitudId;

    protected DocumentoAdjunto() {}
}

package cl.duoc.seguros.siniestros;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_documento")
public class SolicitudDocumento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    Siniestro siniestro;
    @Column(nullable = false)
    String tipo;
    @Column(length = 300)
    String comentario;
    @Column(nullable = false)
    String solicitante;
    @Column(nullable = false)
    LocalDateTime fechaSolicitud;
    @Column(nullable = false)
    boolean entregado;

    protected SolicitudDocumento() {}
}

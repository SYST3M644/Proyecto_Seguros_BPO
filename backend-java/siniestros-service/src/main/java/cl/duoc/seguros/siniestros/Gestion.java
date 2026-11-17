package cl.duoc.seguros.siniestros;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gestiones")
public class Gestion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    Siniestro siniestro;
    @Column(nullable = false)
    LocalDateTime fechaHora;
    @Column(nullable = false, length = 500)
    String descripcion;
    @Column(nullable = false)
    String responsable;
    @Column(nullable = false)
    boolean visibleAsegurado;
    String etapaDestino;
    @Column(nullable = false)
    String tipo;

    protected Gestion() {}
}

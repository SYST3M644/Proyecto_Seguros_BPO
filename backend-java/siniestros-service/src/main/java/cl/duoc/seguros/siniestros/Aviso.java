package cl.duoc.seguros.siniestros;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "avisos")
public class Aviso {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    Siniestro siniestro;
    @Column(nullable = false)
    String destinatarioRut;
    @Column(nullable = false, length = 300)
    String mensaje;
    @Column(nullable = false)
    LocalDateTime fechaHora;
    @Column(nullable = false)
    boolean leido;

    protected Aviso() {}
}

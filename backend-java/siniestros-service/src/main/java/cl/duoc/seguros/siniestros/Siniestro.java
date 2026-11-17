package cl.duoc.seguros.siniestros;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "siniestros")
public class Siniestro {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(unique = true, length = 15)
    String numero;
    @Column(nullable = false, length = 12)
    String aseguradoRut;
    @Column(nullable = false)
    String compania;
    @Column(nullable = false)
    String tipo;
    @Column(nullable = false)
    String tipoNombre;
    @Column(nullable = false)
    String linea;
    @Column(nullable = false)
    LocalDate fechaOcurrencia;
    @Column(nullable = false)
    LocalDateTime fechaReporte;
    @Column(nullable = false)
    String comuna;
    @Column(nullable = false, length = 500)
    String descripcion;
    @Column(nullable = false)
    String etapa;
    String liquidadorId;
    String equipo;
    @Column(nullable = false)
    LocalDateTime actualizado;
    @Column(nullable = false)
    LocalDateTime fechaEtapa;
    @Column(nullable = false)
    boolean fueraDePlazo;
    String resultado;
    @Column(length = 500)
    String mensajeCierre;
    @Column(length = 300)
    String motivoDesistimiento;

    protected Siniestro() {}
}

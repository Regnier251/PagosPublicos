package com.arely.pagospublicos.tercero.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.persistence.*;

@Entity
@Table(name = "cuentas_bancarias")
public class CuentaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tercero_id", nullable = false)
    private Tercero tercero;

    @NotBlank(message = "El banco es obligatorio")
    @Size(max = 100, message = "El banco no puede exceder 100 caracteres")
    @Column(name = "banco", nullable = false, length = 100)
    private String banco;

    @Pattern(regexp = "\\d{18}", message = "La CLABE debe tener exactamente 18 dígitos")
    @Column(name = "clabe", length = 18, unique = true)
    private String clabe;

    @NotBlank(message = "El titular es obligatorio")
    @Size(max = 255, message = "El titular no puede exceder 255 caracteres")
    @Column(name = "titular", nullable = false)
    private String titular;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstatusCuenta estatus;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tercero getTercero() { return tercero; }
    public void setTercero(Tercero tercero) { this.tercero = tercero; }

    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }

    public String getClabe() { return clabe; }
    public void setClabe(String clabe) { this.clabe = clabe; }

    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }

    public EstatusCuenta getEstatus() { return estatus; }
    public void setEstatus(EstatusCuenta estatus) { this.estatus = estatus; }
}
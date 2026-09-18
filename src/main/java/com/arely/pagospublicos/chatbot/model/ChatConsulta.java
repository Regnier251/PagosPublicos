package com.arely.pagospublicos.chatbot.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_consultas")
public class ChatConsulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_usuario", nullable = false, length = 100)
    private String nombreUsuario;

    @Column(nullable = false, columnDefinition = "text")
    private String pregunta;

    @Column(nullable = false, columnDefinition = "text")
    private String respuesta;

    /** Nombres de herramientas ejecutadas, separadas por coma; null si ninguna */
    @Column(name = "herramientas_usadas", length = 500)
    private String herramientasUsadas;

    @Column(name = "duracion_ms", nullable = false)
    private Long duracionMs;

    @CreationTimestamp
    @Column(name = "consultado_en", nullable = false, updatable = false)
    private LocalDateTime consultadoEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getPregunta() { return pregunta; }
    public void setPregunta(String pregunta) { this.pregunta = pregunta; }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

    public String getHerramientasUsadas() { return herramientasUsadas; }
    public void setHerramientasUsadas(String herramientasUsadas) { this.herramientasUsadas = herramientasUsadas; }

    public Long getDuracionMs() { return duracionMs; }
    public void setDuracionMs(Long duracionMs) { this.duracionMs = duracionMs; }

    public LocalDateTime getConsultadoEn() { return consultadoEn; }
}
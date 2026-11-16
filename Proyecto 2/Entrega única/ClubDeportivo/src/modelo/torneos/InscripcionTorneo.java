package modelo.torneos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import excepciones.ClubException;
import modelo.deportes.Equipo;
import modelo.personas.Persona;

public class InscripcionTorneo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private boolean preferencial;
    private double valorPagado;

    public boolean isPreferencial() { return preferencial; }
    public double getValorPagado() { return valorPagado; }
    public void registrarPago(double valor, boolean prioridad) throws ClubException
    {
        if (!Double.isFinite(valor) || valor < 0) throw new ClubException("Pago inválido");
        valorPagado = valor;
        preferencial = prioridad;
    }

    private String codigo;
    private String codigoTorneo;
    private String nombreParticipacion;

    private ModalidadTorneo modalidad;
    private List<Persona> participantes;
    private Equipo equipo;

    private LocalDateTime fechaInscripcion;

    public InscripcionTorneo(
            String codigo,
            String codigoTorneo,
            String nombreParticipacion,
            ModalidadTorneo modalidad,
            List<Persona> participantes,
            Equipo equipo,
            LocalDateTime fechaInscripcion) throws ClubException
    {
        if (codigo == null || codigo.trim().isEmpty())
        {
            throw new ClubException(
                "El código de inscripción es obligatorio");
        }

        if (codigoTorneo == null || codigoTorneo.trim().isEmpty())
        {
            throw new ClubException(
                "El código del torneo es obligatorio");
        }

        if (nombreParticipacion == null
                || nombreParticipacion.trim().isEmpty())
        {
            throw new ClubException(
                "El nombre de la participación es obligatorio");
        }

        if (modalidad == null)
        {
            throw new ClubException(
                "Debe indicar la modalidad");
        }

        if (participantes == null || participantes.isEmpty())
        {
            throw new ClubException(
                "La inscripción debe incluir participantes");
        }

        if (fechaInscripcion == null)
        {
            throw new ClubException(
                "Debe indicar la fecha de inscripción");
        }

        Set<String> documentos = new HashSet<String>();

        for (Persona persona : participantes)
        {
            if (persona == null)
            {
                throw new ClubException(
                    "La lista contiene un participante nulo");
            }

            String documento = persona.getDocumento();

            if (documento == null || documento.trim().isEmpty())
            {
                throw new ClubException(
                    "Todos los participantes deben tener documento");
            }

            if (!documentos.add(documento.trim()))
            {
                throw new ClubException(
                    "Una persona no puede aparecer dos veces "
                    + "en la misma inscripción");
            }
        }

        if (modalidad == ModalidadTorneo.INDIVIDUAL
                && participantes.size() != 1)
        {
            throw new ClubException(
                "Una inscripción individual requiere una persona");
        }

        if (modalidad == ModalidadTorneo.DOBLES
                && participantes.size() != 2)
        {
            throw new ClubException(
                "Una inscripción de dobles requiere dos personas");
        }

        if (modalidad != ModalidadTorneo.EQUIPOS && equipo != null)
        {
            throw new ClubException(
                "Solo una inscripción por equipos puede asociar un equipo");
        }

        this.codigo = codigo.trim();
        this.codigoTorneo = codigoTorneo.trim();
        this.nombreParticipacion = nombreParticipacion.trim();
        this.modalidad = modalidad;
        this.participantes = new ArrayList<Persona>(participantes);
        this.equipo = equipo;
        this.fechaInscripcion = fechaInscripcion;
    }

    public String getCodigo()
    {
        return codigo;
    }

    public String getCodigoTorneo()
    {
        return codigoTorneo;
    }

    public String getNombreParticipacion()
    {
        return nombreParticipacion;
    }

    public ModalidadTorneo getModalidad()
    {
        return modalidad;
    }

    public List<Persona> getParticipantes()
    {
        return new ArrayList<Persona>(participantes);
    }

    public Equipo getEquipo()
    {
        return equipo;
    }

    public LocalDateTime getFechaInscripcion()
    {
        return fechaInscripcion;
    }
}

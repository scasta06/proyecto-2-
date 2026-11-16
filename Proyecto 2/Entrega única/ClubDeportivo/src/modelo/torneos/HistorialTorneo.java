package modelo.torneos;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import excepciones.ClubException;
import modelo.personas.Persona;

public class HistorialTorneo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Torneo torneo;
    private List<InscripcionTorneo> inscripciones;
    private List<EncuentroTorneo> encuentros;
    private List<InscripcionTorneo> retiradas;

    public List<InscripcionTorneo> getInscripcionesRetiradas()
    {
        if (retiradas == null) retiradas = new ArrayList<InscripcionTorneo>();
        return new ArrayList<InscripcionTorneo>(retiradas);
    }

    public HistorialTorneo(Torneo torneo)
    {
        if (torneo == null)
        {
            throw new IllegalArgumentException(
                "El historial debe pertenecer a un torneo");
        }

        this.torneo = torneo;
        this.inscripciones = new ArrayList<InscripcionTorneo>();
        this.encuentros = new ArrayList<EncuentroTorneo>();
    }

    public void registrarInscripcionConfirmada(
            InscripcionTorneo inscripcion) throws ClubException
    {
        if (torneo.getEstado() != EstadoTorneo.INSCRIPCIONES_ABIERTAS)
        {
            throw new ClubException(
                "No se pueden incorporar inscripciones después del inicio");
        }

        if (inscripcion == null)
        {
            throw new ClubException("Debe indicar una inscripción");
        }

        if (!torneo.getCodigo().equalsIgnoreCase(
                inscripcion.getCodigoTorneo()))
        {
            throw new ClubException(
                "La inscripción pertenece a otro torneo");
        }

        if (inscripcion.getModalidad() != torneo.getModalidad())
        {
            throw new ClubException(
                "La modalidad no corresponde al torneo");
        }

        for (InscripcionTorneo existente : inscripciones)
        {
            if (existente.getCodigo().equalsIgnoreCase(
                    inscripcion.getCodigo()))
            {
                throw new ClubException(
                    "El código de inscripción ya está registrado");
            }
        }

        if (inscripciones.size() >= torneo.getCuposTotales())
        {
            throw new ClubException("El torneo no tiene cupos disponibles");
        }

        for (InscripcionTorneo existente : inscripciones)
        {
            for (Persona registrada : existente.getParticipantes())
            {
                for (Persona nueva : inscripcion.getParticipantes())
                {
                    if (registrada.getDocumento().equals(nueva.getDocumento()))
                    {
                        throw new ClubException("La persona ya está inscrita en el torneo");
                    }
                }
            }
        }

        inscripciones.add(inscripcion);
    }

    public void retirarInscripcion(String codigo, Persona solicitante) throws ClubException
    {
        if (torneo.getEstado() != EstadoTorneo.INSCRIPCIONES_ABIERTAS)
        {
            throw new ClubException("Solo puede retirarse antes de iniciar el torneo");
        }
        if (!LocalDate.now().isBefore(torneo.getFechaInicio())) throw new ClubException("El plazo de retiro terminó");
        InscripcionTorneo inscripcion = buscarInscripcion(codigo);
        boolean propia = false;
        for (Persona participante : inscripcion.getParticipantes())
        {
            if (participante == solicitante) propia = true;
        }
        if (!propia)
        {
            throw new ClubException("La inscripción no pertenece al solicitante");
        }
        List<EncuentroTorneo> relacionados = new ArrayList<EncuentroTorneo>();
        for (EncuentroTorneo encuentro : encuentros)
        {
            if (encuentro.getCodigoInscripcionA().equalsIgnoreCase(inscripcion.getCodigo())
                    || encuentro.getCodigoInscripcionB().equalsIgnoreCase(inscripcion.getCodigo()))
            {
                if (encuentro.getEstado() != EstadoEncuentro.PROGRAMADO)
                {
                    throw new ClubException("No se puede retirar una participación con resultados");
                }
                relacionados.add(encuentro);
            }
        }
        encuentros.removeAll(relacionados);
        inscripciones.remove(inscripcion);
        if (retiradas == null) retiradas = new ArrayList<InscripcionTorneo>();
        retiradas.add(inscripcion);
    }

    public InscripcionTorneo buscarInscripcion(String codigo)
            throws ClubException
    {
        if (codigo == null || codigo.trim().isEmpty())
        {
            throw new ClubException(
                "Debe indicar el código de inscripción");
        }

        for (InscripcionTorneo inscripcion : inscripciones)
        {
            if (inscripcion.getCodigo().equalsIgnoreCase(codigo.trim()))
            {
                return inscripcion;
            }
        }

        throw new ClubException(
            "La inscripción no pertenece al historial de este torneo");
    }

    public void agregarEncuentro(EncuentroTorneo encuentro)
            throws ClubException
    {
        if (torneo.getEstado() == EstadoTorneo.FINALIZADO)
        {
            throw new ClubException(
                "No se pueden agregar encuentros a un torneo finalizado");
        }

        if (encuentro == null)
        {
            throw new ClubException("Debe indicar un encuentro");
        }

        if (encuentro.getEstado() != EstadoEncuentro.PROGRAMADO)
        {
            throw new ClubException(
                "El encuentro debe registrarse antes de cargar su resultado");
        }

        buscarInscripcion(encuentro.getCodigoInscripcionA());
        buscarInscripcion(encuentro.getCodigoInscripcionB());

        LocalDate fecha = encuentro.getFechaHora().toLocalDate();

        if (fecha.isBefore(torneo.getFechaInicio())
                || fecha.isAfter(torneo.getFechaFin()))
        {
            throw new ClubException(
                "El encuentro debe estar dentro de las fechas del torneo");
        }

        for (EncuentroTorneo existente : encuentros)
        {
            if (existente.getCodigo().equalsIgnoreCase(
                    encuentro.getCodigo()))
            {
                throw new ClubException(
                    "El código del encuentro ya está registrado");
            }
        }

        encuentros.add(encuentro);
    }

    public EncuentroTorneo buscarEncuentro(String codigo)
            throws ClubException
    {
        if (codigo == null || codigo.trim().isEmpty())
        {
            throw new ClubException(
                "Debe indicar el código del encuentro");
        }

        for (EncuentroTorneo encuentro : encuentros)
        {
            if (encuentro.getCodigo().equalsIgnoreCase(codigo.trim()))
            {
                return encuentro;
            }
        }

        throw new ClubException(
            "No se encontró el encuentro en este torneo");
    }

    public void registrarVictoria(
            String codigoEncuentro,
            String codigoGanador,
            String resultado) throws ClubException
    {
        validarTorneoEnCurso();

        buscarEncuentro(codigoEncuentro)
            .registrarVictoria(codigoGanador, resultado);
    }

    public void registrarEmpate(
            String codigoEncuentro,
            String resultado) throws ClubException
    {
        validarTorneoEnCurso();

        if (!torneo.getDeporte().isPermiteEmpate())
        {
            throw new ClubException(
                "El deporte del torneo no permite empates");
        }

        buscarEncuentro(codigoEncuentro).registrarEmpate(resultado);
    }

    private void validarTorneoEnCurso() throws ClubException
    {
        if (torneo.getEstado() != EstadoTorneo.EN_CURSO)
        {
            throw new ClubException(
                "Solo se registran resultados con el torneo en curso");
        }
    }

    public List<InscripcionTorneo> getInscripciones()
    {
        return new ArrayList<InscripcionTorneo>(inscripciones);
    }

    public List<EncuentroTorneo> getEncuentros()
    {
        List<EncuentroTorneo> copia =
            new ArrayList<EncuentroTorneo>(encuentros);

        copia.sort(Comparator.comparing(
            EncuentroTorneo::getFechaHora));

        return copia;
    }

    public boolean tieneEncuentrosPendientes()
    {
        for (EncuentroTorneo encuentro : encuentros)
        {
            if (encuentro.getEstado() == EstadoEncuentro.PROGRAMADO)
            {
                return true;
            }
        }

        return false;
    }
}

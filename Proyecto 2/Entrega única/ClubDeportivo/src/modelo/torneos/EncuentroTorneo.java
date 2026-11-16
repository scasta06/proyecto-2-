package modelo.torneos;

import java.io.Serializable;
import java.time.LocalDateTime;

import excepciones.ClubException;

public class EncuentroTorneo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String codigo;
    private String codigoInscripcionA;
    private String codigoInscripcionB;
    private LocalDateTime fechaHora;

    private EstadoEncuentro estado;

    private String resultado;
    private String codigoInscripcionGanadora;
    private boolean empate;

    public EncuentroTorneo(
            String codigo,
            String codigoInscripcionA,
            String codigoInscripcionB,
            LocalDateTime fechaHora) throws ClubException
    {
        this.codigo = validarTexto(codigo, "Código del encuentro");

        this.codigoInscripcionA = validarTexto(
            codigoInscripcionA, "Primera inscripción");

        this.codigoInscripcionB = validarTexto(
            codigoInscripcionB, "Segunda inscripción");

        if (this.codigoInscripcionA.equalsIgnoreCase(
                this.codigoInscripcionB))
        {
            throw new ClubException(
                "Una inscripción no puede enfrentarse a sí misma");
        }

        if (fechaHora == null)
        {
            throw new ClubException(
                "Debe indicar la fecha y hora del encuentro");
        }

        this.fechaHora = fechaHora;
        this.estado = EstadoEncuentro.PROGRAMADO;
        this.resultado = null;
        this.codigoInscripcionGanadora = null;
        this.empate = false;
    }

    public void registrarVictoria(
            String codigoGanador,
            String detalleResultado) throws ClubException
    {
        validarPendiente();

        String ganador = validarTexto(
            codigoGanador, "Inscripción ganadora");

        String detalle = validarTexto(
            detalleResultado, "Resultado");

        String ganadorRegistrado;

        if (codigoInscripcionA.equalsIgnoreCase(ganador))
        {
            ganadorRegistrado = codigoInscripcionA;
        }
        else if (codigoInscripcionB.equalsIgnoreCase(ganador))
        {
            ganadorRegistrado = codigoInscripcionB;
        }
        else
        {
            throw new ClubException(
                "El ganador debe ser una de las dos inscripciones");
        }

        this.codigoInscripcionGanadora = ganadorRegistrado;
        this.resultado = detalle;
        this.empate = false;
        this.estado = EstadoEncuentro.FINALIZADO;
    }

    public void registrarEmpate(
            String detalleResultado) throws ClubException
    {
        validarPendiente();

        String detalle = validarTexto(
            detalleResultado, "Resultado");

        this.resultado = detalle;
        this.codigoInscripcionGanadora = null;
        this.empate = true;
        this.estado = EstadoEncuentro.FINALIZADO;
    }

    private void validarPendiente() throws ClubException
    {
        if (estado != EstadoEncuentro.PROGRAMADO)
        {
            throw new ClubException(
                "El encuentro ya tiene un resultado registrado");
        }
    }

    private static String validarTexto(
            String texto, String campo) throws ClubException
    {
        if (texto == null || texto.trim().isEmpty())
        {
            throw new ClubException(campo + " es obligatorio");
        }

        return texto.trim();
    }

    public String getCodigo()
    {
        return codigo;
    }

    public String getCodigoInscripcionA()
    {
        return codigoInscripcionA;
    }

    public String getCodigoInscripcionB()
    {
        return codigoInscripcionB;
    }

    public LocalDateTime getFechaHora()
    {
        return fechaHora;
    }

    public EstadoEncuentro getEstado()
    {
        return estado;
    }

    public String getResultado()
    {
        return resultado;
    }

    public String getCodigoInscripcionGanadora()
    {
        return codigoInscripcionGanadora;
    }

    public boolean isEmpate()
    {
        return empate;
    }
}

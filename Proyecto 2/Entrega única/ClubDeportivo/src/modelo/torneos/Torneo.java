package modelo.torneos;

import java.io.Serializable;
import java.time.LocalDate;
import modelo.deportes.Deporte;
import modelo.tipos.Categoria;
import excepciones.ClubException;

public class Torneo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String codigo;
    private String nombre;

    private Deporte deporte;
    private Categoria categoria;
    private String nivel;

    private TipoTorneo tipo;
    private ModalidadTorneo modalidad;
    private EstadoTorneo estado;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private int cuposTotales;
    private double tarifaInscripcion;
    private HistorialTorneo historial;

    public Torneo(String codigo, String nombre,
            Deporte deporte, Categoria categoria,
            String nivel, TipoTorneo tipo,
            ModalidadTorneo modalidad,
            LocalDate fechaInicio, LocalDate fechaFin,
            int cuposTotales, double tarifaInscripcion)
      throws ClubException
{
  if (codigo == null || codigo.trim().isEmpty())
  {
      throw new ClubException("El código del torneo es obligatorio");
  }

  if (nombre == null || nombre.trim().isEmpty())
  {
      throw new ClubException("El nombre del torneo es obligatorio");
  }

  if (deporte == null || tipo == null || modalidad == null)
  {
      throw new ClubException(
          "Debe indicar deporte, tipo y modalidad");
  }

  if (fechaInicio == null || fechaFin == null
          || fechaFin.isBefore(fechaInicio))
  {
      throw new ClubException("Las fechas del torneo no son válidas");
  }

  if (cuposTotales <= 0)
  {
      throw new ClubException("La cantidad de cupos debe ser positiva");
  }

  if (deporte.isEsDeConjunto())
  {
      if (modalidad != ModalidadTorneo.EQUIPOS)
      {
          throw new ClubException(
              "Los deportes de conjunto requieren equipos");
      }

      if (categoria == null)
      {
          throw new ClubException("Debe indicar la categoría");
      }
  }
  else
  {
      if (modalidad == ModalidadTorneo.EQUIPOS)
      {
          throw new ClubException(
              "La disciplina individual requiere individuales o dobles");
      }

      if (nivel == null
              || !(nivel.trim().equalsIgnoreCase("Principiante")
              || nivel.trim().equalsIgnoreCase("Intermedio")
              || nivel.trim().equalsIgnoreCase("Avanzado")))
      {
          throw new ClubException(
              "Nivel válido: Principiante, Intermedio o Avanzado");
      }
  }

  if (!Double.isFinite(tarifaInscripcion))
  {
      throw new ClubException("La tarifa debe ser un número finito");
  }

  if (tipo == TipoTorneo.RECREATIVO && tarifaInscripcion != 0)
  {
      throw new ClubException(
          "Los torneos recreativos no cobran inscripción");
  }

  if (tipo == TipoTorneo.COMPETITIVO && tarifaInscripcion <= 0)
  {
      throw new ClubException(
          "Los torneos competitivos requieren una tarifa positiva");
  }

  this.codigo = codigo.trim();
  this.nombre = nombre.trim();
  this.deporte = deporte;
  this.categoria = categoria;
  this.nivel = nivel == null ? null : nivel.trim();
  this.tipo = tipo;
  this.modalidad = modalidad;
  this.fechaInicio = fechaInicio;
  this.fechaFin = fechaFin;
  this.cuposTotales = cuposTotales;
  this.tarifaInscripcion = tarifaInscripcion;
  this.estado = EstadoTorneo.INSCRIPCIONES_ABIERTAS;
	}

    public String getCodigo()
    {
        return codigo;
    }
    public String getNombre()
    {
        return nombre;
    }

    public Deporte getDeporte()
    {
        return deporte;
    }

    public Categoria getCategoria()
    {
        return categoria;
    }

    public String getNivel()
    {
        return nivel;
    }

    public TipoTorneo getTipo()
    {
        return tipo;
    }

    public ModalidadTorneo getModalidad()
    {
        return modalidad;
    }

    public EstadoTorneo getEstado()
    {
        return estado;
    }

    public LocalDate getFechaInicio()
    {
        return fechaInicio;
    }

    public LocalDate getFechaFin()
    {
        return fechaFin;
    }

    public int getCuposTotales()
    {
        return cuposTotales;
    }

    public double getTarifaInscripcion()
    {
        return tarifaInscripcion;
    }

    private boolean cuposPreferencialesLiberados = false;

    public int getCuposPreferencialesIniciales()
    {
        return cuposTotales / 5 + (cuposTotales % 5 == 0 ? 0 : 1);
    }

    public boolean isCuposPreferencialesLiberados()
    {
        return cuposPreferencialesLiberados;
    }

    public int getCuposReservadosParaPreferenciales()
    {
        if (cuposPreferencialesLiberados)
        {
            return 0;
        }

        return getCuposPreferencialesIniciales();
    }

    public int getCuposHabilitadosParaRegulares()
    {
        return cuposTotales - getCuposReservadosParaPreferenciales();
    }

    public void liberarCuposPreferenciales() throws ClubException
    {
        if (estado != EstadoTorneo.INSCRIPCIONES_ABIERTAS)
        {
            throw new ClubException(
                "Solo se pueden liberar cupos con inscripciones abiertas");
        }

        cuposPreferencialesLiberados = true;
    }

    public void iniciar() throws ClubException
    {
        if (estado != EstadoTorneo.INSCRIPCIONES_ABIERTAS)
        {
            throw new ClubException(
                "Solo se puede iniciar un torneo con inscripciones abiertas");
        }

        estado = EstadoTorneo.EN_CURSO;
    }

    public void finalizar() throws ClubException
    {
        if (estado != EstadoTorneo.EN_CURSO)
        {
            throw new ClubException(
                "Solo se puede finalizar un torneo en curso");
        }

        if (getHistorial().tieneEncuentrosPendientes())
        {
            throw new ClubException(
                "Debe registrar los resultados de los encuentros pendientes "
                + "antes de finalizar el torneo");
        }

        estado = EstadoTorneo.FINALIZADO;
    }

    public HistorialTorneo getHistorial()
    {
        if (historial == null)
        {
            historial = new HistorialTorneo(this);
        }

        return historial;
    }
}

package logica;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import excepciones.ClubException;
import modelo.torneos.Torneo;

public class GestorTorneos implements Serializable
{
    private static final long serialVersionUID = 1L;

    private List<Torneo> torneos;
    private ClubDeportivo club;
    public void configurarClub(ClubDeportivo club) { this.club = club; }

    public GestorTorneos()
    {
        torneos = new ArrayList<Torneo>();
    }

    public void registrarTorneo(Torneo torneo) throws ClubException
    {
        if (torneo == null)
        {
            throw new ClubException("Debe indicar un torneo");
        }

        for (Torneo existente : torneos)
        {
            if (existente.getCodigo().equalsIgnoreCase(torneo.getCodigo()))
            {
                throw new ClubException(
                    "Ya existe un torneo con ese código");
            }
        }

        torneos.add(torneo);
    }

    public Torneo buscarTorneo(String codigo) throws ClubException
    {
        if (codigo == null || codigo.trim().isEmpty())
        {
            throw new ClubException("Debe indicar el código del torneo");
        }

        for (Torneo torneo : torneos)
        {
            if (torneo.getCodigo().equalsIgnoreCase(codigo.trim()))
            {
                return torneo;
            }
        }

        throw new ClubException("No se encontró el torneo: " + codigo);
    }

    public List<Torneo> getTorneos()
    {
        return new ArrayList<Torneo>(torneos);
    }

    public void liberarCuposPreferenciales(String codigo)
            throws ClubException
    {
        Torneo torneo = buscarTorneo(codigo);
        torneo.liberarCuposPreferenciales();
    }

    public void iniciarTorneo(String codigo) throws ClubException
    {
        Torneo torneo = buscarTorneo(codigo);
        if (club != null)
            for (modelo.torneos.InscripcionTorneo inscripcion : torneo.getHistorial().getInscripciones())
                for (modelo.personas.Persona participante : inscripcion.getParticipantes())
                    for (modelo.personal.Turno turno : club.getGestorPersonal().getTurnos())
                        if (turno.getEmpleado() == participante && !turno.getFecha().isBefore(torneo.getFechaInicio())
                                && !turno.getFecha().isAfter(torneo.getFechaFin()))
                            throw new ClubException("Un empleado inscrito tiene turno durante el torneo");
        torneo.iniciar();
    }

    public void finalizarTorneo(String codigo) throws ClubException
    {
        Torneo torneo = buscarTorneo(codigo);
        torneo.finalizar();
    }
    private List<modelo.comercio.BonoTorneo> bonos = new ArrayList<modelo.comercio.BonoTorneo>();
    private List<String> premiosEntregados = new ArrayList<String>();

    public List<modelo.comercio.BonoTorneo> consultarBonos(modelo.personas.Persona persona)
    {
        if (bonos == null) bonos = new ArrayList<modelo.comercio.BonoTorneo>();
        List<modelo.comercio.BonoTorneo> resultado = new ArrayList<modelo.comercio.BonoTorneo>();
        for (modelo.comercio.BonoTorneo bono : bonos)
            if (bono.getPropietario() == persona) resultado.add(bono);
        return resultado;
    }

    public void entregarBonos(String codigo, double porcentaje) throws ClubException
    {
        Torneo torneo = buscarTorneo(codigo);
        if (premiosEntregados == null) premiosEntregados = new ArrayList<String>();
        if (bonos == null) bonos = new ArrayList<modelo.comercio.BonoTorneo>();
        if (torneo.getEstado() != modelo.torneos.EstadoTorneo.FINALIZADO
                || torneo.getTipo() != modelo.torneos.TipoTorneo.RECREATIVO
                || premiosEntregados.contains(torneo.getCodigo())
                || torneo.getHistorial().getEncuentros().isEmpty())
            throw new ClubException("El torneo no permite entregar bonos");
        List<modelo.comercio.BonoTorneo> nuevos = new ArrayList<modelo.comercio.BonoTorneo>();
        for (modelo.torneos.TablaPosicionesTorneo.Fila fila : modelo.torneos.TablaPosicionesTorneo.calcular(torneo))
        {
            if (fila.getPosicion() == 1)
            {
                for (modelo.personas.Persona persona : torneo.getHistorial().buscarInscripcion(fila.getCodigo()).getParticipantes())
                {
                    nuevos.add(new modelo.comercio.BonoTorneo(torneo.getCodigo() + "-" + persona.getDocumento(), persona, porcentaje));
                }
            }
        }
        if (nuevos.isEmpty()) throw new ClubException("No hay ganadores para premiar");
        bonos.addAll(nuevos);
        premiosEntregados.add(torneo.getCodigo());
    }

    public modelo.torneos.InscripcionTorneo inscribir(ClubDeportivo club, String codigo,
            String id, String nombre, List<modelo.personas.Persona> participantes,
            modelo.deportes.Equipo equipo) throws ClubException
    {
        Torneo torneo = buscarTorneo(codigo);
        if (!java.time.LocalDate.now().isBefore(torneo.getFechaInicio()))
            throw new ClubException("Las inscripciones cierran al comenzar la fecha inicial");
        modelo.torneos.InscripcionTorneo inscripcion = new modelo.torneos.InscripcionTorneo(
                id, codigo, nombre, torneo.getModalidad(), participantes, equipo, java.time.LocalDateTime.now());
        for (modelo.personas.Persona persona : participantes)
        {
            if (!club.getGestorUsuarios().getPersonas().contains(persona))
                throw new ClubException("Participante no registrado");
            if (persona instanceof modelo.personas.Administrador)
                throw new ClubException("El administrador no puede participar");
            if (persona instanceof modelo.personas.Empleado)
            {
                if (!(persona instanceof modelo.personas.Entrenador || persona instanceof modelo.personas.Fisioterapeuta)
                        || torneo.getModalidad() != modelo.torneos.ModalidadTorneo.INDIVIDUAL)
                    throw new ClubException("Los empleados solo participan individualmente");
                for (modelo.personal.Turno turno : club.getGestorPersonal().getTurnos())
                    if (turno.getEmpleado() == persona && !turno.getFecha().isBefore(torneo.getFechaInicio())
                            && !turno.getFecha().isAfter(torneo.getFechaFin()))
                        throw new ClubException("El empleado tiene turno durante el torneo");
            }
            else if (persona instanceof modelo.personas.Socio)
            {
                modelo.personas.Socio socio = (modelo.personas.Socio) persona;
                if (!socio.isActivo() || socio.estaSuspendido(torneo.getDeporte(), torneo.getFechaInicio()))
                    throw new ClubException("Socio inactivo o suspendido");
                if (torneo.getModalidad() != modelo.torneos.ModalidadTorneo.EQUIPOS)
                {
                    int nivel = torneo.getNivel().equalsIgnoreCase("Principiante") ? 1
                            : torneo.getNivel().equalsIgnoreCase("Intermedio") ? 2 : 3;
                    if (socio.getNivel(torneo.getDeporte()) != nivel)
                        throw new ClubException("El nivel del participante no corresponde al torneo");
                }
            }
            else if (persona instanceof modelo.personas.ParticipanteExterno)
            {
                if (torneo.getModalidad() == modelo.torneos.ModalidadTorneo.EQUIPOS) continue;
                int nivel = torneo.getNivel().equalsIgnoreCase("Principiante") ? 1
                        : torneo.getNivel().equalsIgnoreCase("Intermedio") ? 2 : 3;
                if (((modelo.personas.ParticipanteExterno) persona).getNivel(torneo.getDeporte()) != nivel)
                    throw new ClubException("El nivel del participante no corresponde al torneo");
            }
            else throw new ClubException("Tipo de participante no habilitado");
        }
        if (torneo.getModalidad() == modelo.torneos.ModalidadTorneo.EQUIPOS)
        {
            if (!torneo.getDeporte().nominaValida(participantes.size())) throw new ClubException("El equipo no está completo");
            for (modelo.personas.Persona persona : participantes)
                if (modelo.tipos.Categoria.paraEdad(persona.getEdad(torneo.getFechaInicio())) != torneo.getCategoria())
                    throw new ClubException("La edad no corresponde a la categoría del torneo");
        }
        if (equipo != null && (!equipo.isActivo() || equipo.getDeporte() != torneo.getDeporte()
                || equipo.getCategoria() != torneo.getCategoria()
                || !equipo.getDeporte().nominaValida(participantes.size())
                || participantes.size() != equipo.getJugadoresActivos().size()
                || !participantes.containsAll(equipo.getJugadoresActivos())))
            throw new ClubException("Debe inscribir el equipo completo de la categoría y deporte correctos");
        boolean preferencial = esPreferencial(club, torneo, participantes, equipo);
        int regulares = 0;
        for (modelo.torneos.InscripcionTorneo existente : torneo.getHistorial().getInscripciones())
            if (!existente.isPreferencial()) regulares++;
        if (!preferencial && regulares >= torneo.getCuposHabilitadosParaRegulares())
            throw new ClubException("Solo quedan cupos preferenciales; espere su liberación");
        double pago = participantes.get(0) instanceof modelo.personas.Empleado ? 0 : torneo.getTarifaInscripcion();
        inscripcion.registrarPago(pago, preferencial);
        torneo.getHistorial().registrarInscripcionConfirmada(inscripcion);
        return inscripcion;
    }

    private boolean esPreferencial(ClubDeportivo club, Torneo torneo,
            List<modelo.personas.Persona> participantes, modelo.deportes.Equipo equipo) throws ClubException
    {
        int posicion = 0;
        if (equipo == null && torneo.getModalidad() == modelo.torneos.ModalidadTorneo.EQUIPOS) return false;
        if (equipo == null)
        {
            for (modelo.competencias.EntradaRanking entrada : club.getGestorDeportivo().consultarRanking(torneo.getDeporte()))
            {
                int nivel = torneo.getNivel().equalsIgnoreCase("Principiante") ? 1
                        : torneo.getNivel().equalsIgnoreCase("Intermedio") ? 2 : 3;
                if (entrada.getJugador().getNivel(torneo.getDeporte()) == nivel)
                {
                    posicion++;
                    if (participantes.contains(entrada.getJugador()) && posicion <= torneo.getCuposPreferencialesIniciales())
                        return true;
                }
            }
        }
        else
        {
            for (modelo.competencias.Competencia competencia : club.getGestorCompetencias().getCompetencias())
            {
                posicion = 0;
                for (modelo.competencias.InscripcionCompetencia entrada : club.getGestorCompetencias().consultarTabla(competencia))
                    if (entrada.getEquipo() != null && entrada.getEquipo().getDeporte() == torneo.getDeporte()
                            && entrada.getEquipo().getCategoria() == torneo.getCategoria())
                    {
                        posicion++;
                        if (entrada.getEquipo() == equipo && posicion <= torneo.getCuposPreferencialesIniciales()) return true;
                    }
            }
        }
        return false;
    }

    public double consultarRecaudo(String codigo) throws ClubException
    {
        double total = 0;
        for (modelo.torneos.InscripcionTorneo inscripcion : buscarTorneo(codigo).getHistorial().getInscripciones())
            total += inscripcion.getValorPagado();
        return Math.round(total * 100.0) / 100.0;
    }

    private java.util.Map<String, java.util.Map<String, Double>> premiosDinero;

    public java.util.Map<String, Double> entregarPremiosDinero(String codigo) throws ClubException
    {
        Torneo torneo = buscarTorneo(codigo);
        if (premiosDinero == null) premiosDinero = new java.util.LinkedHashMap<String, java.util.Map<String, Double>>();
        if (torneo.getTipo() != modelo.torneos.TipoTorneo.COMPETITIVO
                || torneo.getEstado() != modelo.torneos.EstadoTorneo.FINALIZADO
                || torneo.getHistorial().getEncuentros().isEmpty() || premiosDinero.containsKey(torneo.getCodigo()))
            throw new ClubException("No se pueden entregar premios en este estado o ya fueron entregados");
        List<modelo.personas.Persona> ganadores = new ArrayList<modelo.personas.Persona>();
        for (modelo.torneos.TablaPosicionesTorneo.Fila fila : modelo.torneos.TablaPosicionesTorneo.calcular(torneo))
            if (fila.getPosicion() == 1)
                for (modelo.personas.Persona persona : torneo.getHistorial().buscarInscripcion(fila.getCodigo()).getParticipantes())
                    if (!(persona instanceof modelo.personas.Empleado)) ganadores.add(persona);
        if (ganadores.isEmpty()) throw new ClubException("El primer puesto no tiene participantes habilitados para premio monetario");
        long centavos = Math.round(consultarRecaudo(codigo) * 100);
        java.util.Map<String, Double> resultado = new java.util.LinkedHashMap<String, Double>();
        for (int i = 0; i < ganadores.size(); i++)
            resultado.put(ganadores.get(i).getDocumento(), (centavos / ganadores.size() + (i < centavos % ganadores.size() ? 1 : 0)) / 100.0);
        premiosDinero.put(torneo.getCodigo(), resultado);
        return new java.util.LinkedHashMap<String, Double>(resultado);
    }

    public java.util.Map<String, Double> consultarPremiosDinero(String codigo) throws ClubException
    {
        Torneo torneo = buscarTorneo(codigo);
        if (premiosDinero == null || !premiosDinero.containsKey(torneo.getCodigo())) return new java.util.LinkedHashMap<String, Double>();
        return new java.util.LinkedHashMap<String, Double>(premiosDinero.get(torneo.getCodigo()));
    }
}

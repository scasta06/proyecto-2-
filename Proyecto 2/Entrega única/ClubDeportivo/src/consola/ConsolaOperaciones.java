package consola;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import excepciones.ClubException;
import logica.ClubDeportivo;
import modelo.personas.*;
import modelo.deportes.*;
import modelo.actividades.*;
import modelo.competencias.*;
import modelo.personal.*;
import modelo.comercio.*;
import modelo.tipos.*;

public class ConsolaOperaciones extends ConsolaBase
{
    private Persona persona;
    public ConsolaOperaciones(ClubDeportivo club, Persona persona)
    {
        super(club);
        this.persona = persona;
    }

    public void mostrarMenu()
    {
        while (true)
        {
            if (persona instanceof Socio)
                System.out.println("1. Inscribirse en academia / 2. Reservar / 3. Cancelar reserva / 4. Práctica / 5. Ranking / 6. Entrenamientos y asistencia / 7. Historial / 8. Equipos / 9. Tabla de competencia / 10. Instalaciones y bebida");
            else if (persona instanceof Administrador)
                System.out.println("1. Registrar socio / 2. Crear equipo / 3. Inscribir jugador / 4. Dar de baja equipo / 5. Inventario / 6. Reabastecer / 7. Asignar turno / 8. Resolver cambio / 9. Resolver sugerencia / 10. Mensualidad / 11. Crear competencia / 12. Inscribir equipo en competencia / 13. Programar partido oficial / 14. Reporte de ventas / 15. Retirar jugador / 16. Trasladar inventario / 17. Reporte deportivo / 18. Asignar nivel / 19. Registrar rival de competencia / 20. Historial de cambios / 21. Fases y resultados externos / 22. Crear producto");
            else
                System.out.println("1. Mis turnos / 2. Solicitar cambio / 3. Mis solicitudes / 4. Sugerir producto / 5. Código de descuento"
                    + (persona instanceof Entrenador ? " / 6. Programar entrenamiento / 7. Registrar asistencia / 8. Programar partido interno / 9. Alineación / 10. Resultado / 11. Estadísticas" : ""));
            String opcion = pedirCadena("Operación (0 para volver): ");
            if (opcion.isEmpty() || opcion.equals("0")) return;
            try
            {
                if (persona instanceof Socio) socio(Integer.parseInt(opcion));
                else if (persona instanceof Administrador) administrador(Integer.parseInt(opcion));
                else empleado(Integer.parseInt(opcion));
            }
            catch (ClubException | IllegalArgumentException e)
            {
                System.out.println("No se completó la operación: " + e.getMessage());
            }
        }
    }

    private String nombre(Object objeto)
    {
        if (objeto instanceof Deporte) return ((Deporte)objeto).getNombre();
        if (objeto instanceof Equipo) return ((Equipo)objeto).getNombre();
        if (objeto instanceof Persona) return ((Persona)objeto).getDocumento() + " | " + ((Persona)objeto).getNombre();
        if (objeto instanceof Academia) return ((Academia)objeto).getNombre();
        if (objeto instanceof Instalacion) return ((Instalacion)objeto).getNombre();
        if (objeto instanceof Evento) return ((Evento)objeto).getId() + " | " + ((Evento)objeto).getFecha() + " " + ((Evento)objeto).getHoraInicio();
        if (objeto instanceof Competencia) return ((Competencia)objeto).getCodigo();
        if (objeto instanceof Producto) return ((Producto)objeto).getCodigo() + " | " + ((Producto)objeto).getNombre();
        if (objeto instanceof modelo.inventario.Ubicacion) return ((modelo.inventario.Ubicacion)objeto).getNombre();
        if (objeto instanceof Turno) return ((Turno)objeto).getId() + " | " + ((Turno)objeto).getFecha() + " " + ((Turno)objeto).getHoraInicio() + "-" + ((Turno)objeto).getHoraFin();
        if (objeto instanceof SolicitudCambioTurno) return nombre(((SolicitudCambioTurno)objeto).getSolicitante()) + " | " + ((SolicitudCambioTurno)objeto).getEstado() + " | " + ((SolicitudCambioTurno)objeto).getObservacion();
        if (objeto instanceof SugerenciaProducto) return nombre(((SugerenciaProducto)objeto).getProducto()) + " | " + ((SugerenciaProducto)objeto).getEstado();
        if (objeto instanceof InscripcionCompetencia) return ((InscripcionCompetencia)objeto).getCompetencia().nombreDe((InscripcionCompetencia)objeto);
        return objeto.toString();
    }

    private <T> T elegir(List<T> opciones) throws ClubException
    {
        if (opciones.isEmpty()) throw new ClubException("No hay opciones disponibles");
        for (int i = 0; i < opciones.size(); i++) System.out.println((i + 1) + ". " + nombre(opciones.get(i)));
        int indice = pedirEntero("Seleccione: ") - 1;
        if (indice < 0 || indice >= opciones.size()) throw new ClubException("Selección inválida");
        return opciones.get(indice);
    }

    private Deporte deporte() throws ClubException { return elegir(club.getGestorDeportivo().getDeportes()); }
    private Equipo equipo() throws ClubException { return elegir(club.getGestorDeportivo().getEquipos()); }
    private Socio buscarSocio() throws ClubException { return club.getGestorUsuarios().buscarSocio(pedirCadena("Documento del socio: ")); }
    private LocalTime hora() { return LocalTime.parse(pedirCadena("Hora (HH:MM): ")); }
    private double dinero() { return Double.parseDouble(pedirCadena("Valor en pesos: ")); }
    private List<Socio> socios() throws ClubException
    {
        List<Socio> lista = new ArrayList<Socio>();
        for (String documento : pedirCadena("Documentos separados por coma: ").split(","))
            lista.add(club.getGestorUsuarios().buscarSocio(documento.trim()));
        return lista;
    }

    private void socio(int opcion) throws ClubException
    {
        Socio socio = (Socio)persona;
        switch (opcion)
        {
            case 1:
                club.getGestorDeportivo().inscribirEnAcademia(socio, elegir(club.getGestorDeportivo().getAcademias()));
                break;
            case 2:
                Deporte deporte = deporte();
                LocalDate fecha = pedirFecha("Fecha (AAAA-MM-DD): ");
                for (Instalacion instalacion : club.getGestorInstalaciones().consultarDisponibilidad(deporte, fecha).keySet())
                    System.out.println(instalacion.getNombre() + " | " + club.getGestorInstalaciones().consultarDisponibilidad(deporte, fecha).get(instalacion));
                Reserva reserva = club.getGestorInstalaciones().reservar(socio, deporte, fecha, hora(), pedirEntero("Duración en minutos: "),
                    pedirEntero("Jugadores: "), ModalidadReserva.valueOf(pedirCadena("Modalidad (INDIVIDUAL/DOBLES/GRUPAL): ").toUpperCase()));
                System.out.println("Reserva: " + reserva.getId());
                break;
            case 3:
                List<Reserva> propias = new ArrayList<Reserva>();
                for (Reserva r : club.getGestorInstalaciones().getReservas()) if (r.getSocio() == socio) propias.add(r);
                club.getGestorInstalaciones().cancelarReserva(elegir(propias));
                break;
            case 4:
                Deporte disciplina = deporte();
                if (pedirCadena("¿Dobles? (SI/NO): ").equalsIgnoreCase("SI"))
                {
                    List<Socio> propios = new ArrayList<Socio>(); propios.add(socio); propios.add(buscarSocio());
                    List<Socio> rivales = socios();
                    club.getGestorDeportivo().programarDobles(propios, rivales, disciplina, pedirFecha("Fecha: "), hora());
                }
                else club.getGestorDeportivo().programarPartidoPractica(socio,
                    club.getGestorUsuarios().buscarPersona(pedirCadena("Documento rival: ")), disciplina, pedirFecha("Fecha: "), hora());
                break;
            case 5:
                int posicion = 0;
                for (EntradaRanking entrada : club.getGestorDeportivo().consultarRanking(deporte()))
                    System.out.println(++posicion + ". " + entrada.getJugador().getNombre() + " | ELO: " + entrada.getPuntosRanking() + " | " + entrada.getAcumulados());
                break;
            case 6:
                for (SesionEntrenamiento sesion : club.getGestorDeportivo().getSesiones())
                    if (sesion.estaConvocado(socio))
                    {
                        System.out.println(nombre(sesion));
                        for (RegistroAsistencia registro : sesion.getRegistros())
                            if (registro.getJugador() == socio) System.out.println(registro.getEstado() + " | nota: " + registro.getNotaDesempeno() + " | " + registro.getComentario());
                    }
                break;
            case 7:
                LocalDate inicio = pedirFecha("Desde: "), fin = pedirFecha("Hasta: ");
                if (fin.isBefore(inicio)) throw new ClubException("Rango inválido");
                for (PartidoPractica partido : club.getGestorDeportivo().getPartidosPractica())
                    if (partido.participo(socio) && !partido.getFecha().isBefore(inicio) && !partido.getFecha().isAfter(fin))
                        System.out.println(nombre(partido) + " | " + partido.getEstado() + " | " + partido.getMarcadorPropio() + "-" + partido.getMarcadorRival());
                break;
            case 8:
                for (Equipo equipo : club.getGestorDeportivo().getEquipos())
                    if (equipo.esIntegrante(socio)) System.out.println(equipo.getNombre() + " | " + equipo.getCategoria() + " | entrenador: " + equipo.getEntrenador().getNombre());
                break;
            case 9:
                Competencia competencia = elegir(club.getGestorCompetencias().getCompetencias());
                String grupo = pedirCadena("Grupo (vacío para todos): ");
                for (PosicionCompetencia posicionCompetencia : competencia.getClasificacionCompleta())
                    if (grupo.isEmpty() || grupo.equalsIgnoreCase(posicionCompetencia.getGrupo()))
                        System.out.println(posicionCompetencia.getNombre() + " | grupo: " + posicionCompetencia.getGrupo() + " | puntos: " + posicionCompetencia.getPuntos() + " | partidos: " + posicionCompetencia.getJugados());
                break;
            case 10:
                String accion = pedirCadena("1. Entrar / 2. Salir / 3. Terminar bebida: ");
                if (accion.equals("1")) club.getGestorInstalaciones().ingresar(socio, elegir(club.getGestorInstalaciones().getInstalaciones()));
                else if (accion.equals("2")) club.getGestorInstalaciones().salir(socio);
                else if (accion.equals("3")) socio.terminarBebida();
                else throw new ClubException("Opción inválida");
                break;
            default: throw new ClubException("Opción inválida");
        }
        System.out.println("Operación completada.");
    }

    private Producto productoNuevo() throws ClubException
    {
        String tipo = pedirCadena("1. Artículo deportivo / 2. Bebida: ");
        if (!tipo.equals("1") && !tipo.equals("2")) throw new ClubException("Tipo inválido");
        String codigo = pedirCadena("Código: "), nombre = pedirCadena("Nombre: ");
        double precio = dinero();
        if (tipo.equals("1"))
        {
            ArticuloDeportivo producto = new ArticuloDeportivo(codigo, nombre, precio, pedirCadena("Marca: "), pedirCadena("Talla: "));
            producto.setCategoria(pedirCadena("Categoría: "));
            return producto;
        }
        return new Bebida(codigo, nombre, precio, pedirCadena("Caliente (SI/NO): ").equalsIgnoreCase("SI"));
    }

    private void empleado(int opcion) throws ClubException
    {
        Empleado empleado = (Empleado)persona;
        List<Turno> turnos = new ArrayList<Turno>();
        for (Turno turno : club.getGestorPersonal().getTurnos()) if (turno.getEmpleado() == empleado) turnos.add(turno);
        switch (opcion)
        {
            case 1:
                for (Turno turno : turnos) System.out.println(nombre(turno));
                break;
            case 2:
                Turno actual = elegir(turnos);
                Turno propuesto = new Turno(pedirCadena("Código de turno nuevo: "), pedirFecha("Fecha: "), hora(), hora(), empleado);
                club.getGestorPersonal().solicitarCambio(empleado, actual, propuesto, pedirCadena("Motivo: "));
                break;
            case 3:
                for (SolicitudCambioTurno solicitud : club.getGestorPersonal().getSolicitudes())
                    if (solicitud.getSolicitante() == empleado) System.out.println(nombre(solicitud) + " | propuesto: " + nombre(solicitud.getTurnoPropuesto()));
                break;
            case 4:
                club.getGestorComercio().sugerirProducto(empleado, productoNuevo());
                break;
            case 5:
                System.out.println(empleado.getCodigoDescuento());
                break;
            default:
                if (!(empleado instanceof Entrenador)) throw new ClubException("Operación reservada al entrenador");
                entrenador(opcion, (Entrenador)empleado);
        }
    }

    private List<Partido> partidos()
    {
        List<Partido> lista = new ArrayList<Partido>();
        lista.addAll(club.getGestorDeportivo().getPartidosPractica());
        lista.addAll(club.getGestorDeportivo().getPartidosInternos());
        for (Competencia competencia : club.getGestorCompetencias().getCompetencias()) lista.addAll(competencia.getPartidos());
        return lista;
    }

    private void entrenador(int opcion, Entrenador entrenador) throws ClubException
    {
        if (opcion == 6)
            club.getGestorDeportivo().programarEntrenamiento(entrenador, pedirFecha("Fecha: "), hora(), pedirEntero("Duración: "),
                elegir(club.getGestorInstalaciones().getInstalaciones()), socios());
        else if (opcion == 7)
        {
            List<SesionEntrenamiento> propias = new ArrayList<SesionEntrenamiento>();
            for (SesionEntrenamiento sesion : club.getGestorDeportivo().getSesiones()) if (sesion.getEntrenador() == entrenador) propias.add(sesion);
            SesionEntrenamiento sesion = elegir(propias);
            if (sesion.getFecha().atTime(sesion.getHoraFin()).isAfter(java.time.LocalDateTime.now())) throw new ClubException("La sesión todavía no ha terminado");
            Socio socio = elegir(sesion.getConvocados());
            EstadoAsistencia estado = EstadoAsistencia.valueOf(pedirCadena("ASISTIO/FALTO/LLEGO_TARDE: ").toUpperCase());
            int nota = pedirEntero("Nota 1 a 5 (0 sin nota): ");
            club.getGestorDeportivo().registrarAsistencia(entrenador, sesion, socio, estado, nota == 0 ? null : nota, pedirCadena("Comentario: "));
        }
        else if (opcion == 8)
        {
            Equipo local = equipo(), visitante = equipo();
            if (local.getEntrenador() != entrenador && visitante.getEntrenador() != entrenador) throw new ClubException("Debe dirigir uno de los equipos");
            club.getGestorDeportivo().programarPartidoInterno(local, visitante, pedirFecha("Fecha: "), hora());
        }
        else if (opcion >= 9 && opcion <= 11)
        {
            Partido partido = elegir(partidos());
            if (!entrenador.puedeEntrenar(partido.getDeporte())) throw new ClubException("No dirige esta disciplina");
            if (partido instanceof PartidoInterno && ((PartidoInterno)partido).getLocal().getEntrenador() != entrenador
                    && ((PartidoInterno)partido).getVisitante().getEntrenador() != entrenador)
                throw new ClubException("No es responsable de estos equipos");
            if (partido instanceof PartidoOficial && ((PartidoOficial)partido).getInscripcion().getEquipo() != null
                    && ((PartidoOficial)partido).getInscripcion().getEquipo().getEntrenador() != entrenador)
                throw new ClubException("No es responsable del equipo");
            if (opcion == 9)
            {
                boolean retirar = pedirCadena("Retirar de alineación (SI/NO): ").equalsIgnoreCase("SI");
                Socio jugador = buscarSocio();
                if (retirar) partido.quitarParticipante(jugador);
                else partido.agregarParticipante(jugador);
            }
            else if (opcion == 10)
            {
                if (partido.getFecha().atTime(partido.getHoraFin()).isAfter(java.time.LocalDateTime.now())) throw new ClubException("El partido todavía no ha terminado");
                int propio = pedirEntero("Marcador propio: "), rival = pedirEntero("Marcador rival: ");
                if (partido instanceof PartidoOficial) club.getGestorCompetencias().registrarResultado((PartidoOficial)partido, propio, rival);
                else partido.registrarResultado(propio, rival);
            }
            else
            {
                Socio jugador = elegir(partido.getParticipantes());
                java.util.Map<String, Integer> valores = new java.util.LinkedHashMap<String, Integer>();
                for (String dato : pedirCadena("Estadísticas clave=valor separadas por coma: ").split(","))
                {
                    String[] partes = dato.trim().split("=");
                    if (partes.length != 2) throw new ClubException("Formato de estadística inválido");
                    valores.put(partes[0].trim(), Integer.parseInt(partes[1].trim()));
                }
                club.getGestorDeportivo().registrarEstadistica(entrenador, partido, jugador, valores);
            }
        }
        else throw new ClubException("Opción inválida");
    }

    private void administrador(int opcion) throws ClubException
    {
        Administrador admin = (Administrador)persona;
        switch (opcion)
        {
            case 1:
                club.getGestorUsuarios().registrarSocio(new Socio(pedirCadena("Documento: "), pedirCadena("Nombre: "), pedirFecha("Nacimiento: "),
                    pedirCadena("Login: "), pedirCadena("Contraseña: "), pedirCadena("Número socio: ")));
                break;
            case 2:
                club.getGestorDeportivo().crearEquipo(new Equipo(pedirCadena("Nombre: "), Categoria.valueOf(pedirCadena("Categoría " + java.util.Arrays.toString(Categoria.values()) + ": ").toUpperCase()),
                    pedirEntero("Capacidad: "), deporte(), elegir(club.getGestorUsuarios().getEntrenadores())));
                break;
            case 3:
                club.getGestorDeportivo().inscribirEnEquipo(buscarSocio(), equipo(), pedirCadena("Posición: "), pedirEntero("Camiseta: "));
                break;
            case 4:
                club.getGestorDeportivo().darDeBajaEquipo(equipo());
                break;
            case 5:
                for (Producto producto : club.getGestorComercio().getProductos())
                    System.out.println(nombre(producto) + " | tienda: " + club.getGestorComercio().getInventario().getCantidad(producto, club.getGestorComercio().getTienda().getUbicacion())
                        + " | cafetería: " + club.getGestorComercio().getInventario().getCantidad(producto, club.getGestorComercio().getCafeteria().getUbicacion()));
                break;
            case 6:
                Producto producto = elegir(club.getGestorComercio().getProductos());
                club.getGestorComercio().getInventario().reabastecer(producto,
                    producto instanceof ArticuloDeportivo ? club.getGestorComercio().getTienda().getUbicacion() : club.getGestorComercio().getCafeteria().getUbicacion(), pedirEntero("Cantidad: "), admin);
                break;
            case 7:
                Persona elegida = elegir(club.getGestorUsuarios().getPersonas());
                if (!(elegida instanceof Empleado)) throw new ClubException("Debe elegir empleado");
                club.getGestorPersonal().asignarTurno(new Turno(pedirCadena("Código: "), pedirFecha("Fecha: "), hora(), hora(), (Empleado)elegida));
                break;
            case 8:
                SolicitudCambioTurno solicitud = elegir(club.getGestorPersonal().getSolicitudes());
                if (pedirCadena("Aprobar (SI/NO): ").equalsIgnoreCase("SI")) club.getGestorPersonal().aprobarCambio(solicitud, admin);
                else club.getGestorPersonal().rechazarCambio(solicitud, admin, pedirCadena("Motivo rechazo: "));
                break;
            case 9:
                SugerenciaProducto sugerencia = elegir(club.getGestorComercio().getSugerencias());
                boolean aprobar = pedirCadena("Aprobar (SI/NO): ").equalsIgnoreCase("SI");
                club.getGestorComercio().resolverSugerencia(sugerencia, admin, aprobar, aprobar ? "Aprobado" : pedirCadena("Motivo del rechazo: "));
                break;
            case 10:
                club.getGestorComercio().registrarMensualidad(buscarSocio(), java.time.YearMonth.parse(pedirCadena("Mes AAAA-MM: ")),
                    ConceptoMensualidad.valueOf(pedirCadena("Concepto " + java.util.Arrays.toString(ConceptoMensualidad.values()) + ": ").toUpperCase()), dinero());
                break;
            case 11:
                club.getGestorCompetencias().registrarCompetencia(new Competencia(pedirCadena("Código: "), pedirCadena("Nombre: "), pedirCadena("Organizador: "), deporte(),
                    Categoria.valueOf(pedirCadena("Categoría " + java.util.Arrays.toString(Categoria.values()) + ": ").toUpperCase()),
                    FormatoCompetencia.valueOf(pedirCadena("Formato " + java.util.Arrays.toString(FormatoCompetencia.values()) + ": ").toUpperCase()),
                    pedirFecha("Inicio: "), pedirFecha("Fin: "), pedirFecha("Cierre inscripción: ")));
                break;
            case 12:
                club.getGestorCompetencias().inscribirEquipo(elegir(club.getGestorCompetencias().getCompetencias()), equipo(), socios());
                break;
            case 13:
                Competencia competencia = elegir(club.getGestorCompetencias().getCompetencias());
                club.getGestorCompetencias().programarPartido(competencia, elegir(competencia.getInscripciones()), pedirCadena("Rival: "), pedirFecha("Fecha: "), hora(), pedirCadena("Local (SI/NO): ").equalsIgnoreCase("SI"));
                break;
            case 21:
                Competencia seleccionada = elegir(club.getGestorCompetencias().getCompetencias());
                String accionCompetencia = pedirCadena("1. Resultado externo / 2. Asignar grupo / 3. Iniciar eliminación / 4. Finalizar: ");
                if (accionCompetencia.equals("1")) club.getGestorCompetencias().registrarResultadoExterno(seleccionada,
                    pedirCadena("Código partido: "), pedirCadena("Local: "), pedirCadena("Visitante: "), pedirEntero("Marcador local: "), pedirEntero("Marcador visitante: "));
                else if (accionCompetencia.equals("2")) seleccionada.asignarGrupo(pedirCadena("Participante: "), pedirCadena("Grupo: "));
                else if (accionCompetencia.equals("3")) seleccionada.iniciarEliminacion(pedirEntero("Clasificados por grupo: "));
                else if (accionCompetencia.equals("4")) seleccionada.finalizar();
                else throw new ClubException("Opción inválida");
                break;
            case 22:
                club.getGestorComercio().registrarProducto(productoNuevo());
                break;
            case 15:
                club.getGestorDeportivo().retirarJugador(equipo(), buscarSocio());
                break;
            case 16:
                Producto trasladado = elegir(club.getGestorComercio().getProductos());
                List<modelo.inventario.Ubicacion> ubicaciones = new ArrayList<modelo.inventario.Ubicacion>();
                ubicaciones.add(club.getGestorComercio().getTienda().getUbicacion());
                ubicaciones.add(club.getGestorComercio().getCafeteria().getUbicacion());
                System.out.println("Origen:"); modelo.inventario.Ubicacion origen = elegir(ubicaciones);
                System.out.println("Destino:"); modelo.inventario.Ubicacion destino = elegir(ubicaciones);
                club.getGestorComercio().getInventario().trasladar(trasladado, origen, destino, pedirEntero("Cantidad: "), admin);
                break;
            case 17:
                Socio consultado = buscarSocio();
                LocalDate inicio = pedirFecha("Desde: "), fin = pedirFecha("Hasta: ");
                for (RegistroAsistencia registro : club.getGestorReportes().consultarAsistencia(consultado, inicio, fin))
                    System.out.println(registro.getEstado() + " | " + registro.getNotaDesempeno() + " | " + registro.getComentario());
                for (Partido partido : club.getGestorReportes().consultarResultados(inicio, fin))
                    if (partido.participo(consultado)) System.out.println(nombre(partido) + " | " + partido.getMarcadorPropio() + "-" + partido.getMarcadorRival());
                for (EntradaRanking entrada : club.getGestorDeportivo().consultarRanking(deporte()))
                    if (entrada.getJugador() == consultado) System.out.println("Evolución ELO: " + entrada.getHistorialPuntos());
                break;
            case 18:
                buscarSocio().asignarNivel(deporte(), pedirEntero("Nivel 1 a 3: "));
                break;
            case 19:
                club.getGestorCompetencias().registrarRival(elegir(club.getGestorCompetencias().getCompetencias()), pedirCadena("Institución: "), pedirCadena("Grupo: "));
                break;
            case 20:
                for (SolicitudCambioTurno cambio : club.getGestorPersonal().getSolicitudes())
                    System.out.println(nombre(cambio) + " | " + nombre(cambio.getTurnoActual()) + " -> " + nombre(cambio.getTurnoPropuesto()));
                break;
            case 14:
                logica.ReporteVentas reporte = club.getGestorReportes().generarReporteVentas(pedirFecha("Desde: "), pedirFecha("Hasta: "));
                if (reporte.estaVacio()) System.out.println("No hay movimientos en el período.");
                System.out.println("Total: " + reporte.getTotal() + " | transacciones: " + reporte.getTransacciones());
                System.out.println("Por rubro: " + reporte.getTotalesPorRubro());
                System.out.println("Por mes: " + reporte.getTotalesPorMes());
                break;
            default: throw new ClubException("Opción inválida");
        }
        System.out.println("Operación completada.");
    }
}

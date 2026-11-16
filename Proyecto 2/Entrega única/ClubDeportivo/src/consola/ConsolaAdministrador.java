package consola;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import excepciones.ClubException;
import logica.ClubDeportivo;
import modelo.deportes.Deporte;
import modelo.personas.Administrador;
import modelo.tipos.Categoria;
import modelo.torneos.ModalidadTorneo;
import modelo.torneos.TipoTorneo;
import modelo.torneos.Torneo;

public class ConsolaAdministrador extends ConsolaBase
{
    private Administrador admin;

    public ConsolaAdministrador(ClubDeportivo club, Administrador admin)
    {
        super(club);
        this.admin = admin;
    }

    @Override
    public void mostrarMenu() throws ClubException
    {
        if (admin == null)
        {
            throw new ClubException("Se requiere un administrador");
        }

        while (true)
        {
            System.out.println("\n--- ADMINISTRADOR ---");
            System.out.println("1. Consultar resumen de ventas");
            System.out.println("2. Crear torneo");
            System.out.println("3. Listar torneos");
            System.out.println("4. Consultar detalle de un torneo");
            System.out.println("5. Liberar cupos preferenciales");
            System.out.println("6. Iniciar torneo");
            System.out.println("7. Finalizar torneo");
            System.out.println("8. Gestionar encuentros y resultados");
            System.out.println("9. Entregar premios de torneo");
            System.out.println("10. Registrar participante externo o universitario");
            System.out.println("11. Administración del club");
            System.out.println("0. Guardar y salir");
            System.out.print("Opción: ");

            if (!ENTRADA.hasNextLine())
            {
                return;
            }

            String opcion = ENTRADA.nextLine().trim();

            if (opcion.equals("0"))
            {
                return;
            }

            try
            {
                switch (opcion)
                {
                    case "1":
                        consultarVentas();
                        break;

                    case "2":
                        crearTorneo();
                        break;

                    case "3":
                        listarTorneos();
                        break;

                    case "4":
                        mostrarDetalle(
                            club.getGestorTorneos().buscarTorneo(
                                leerTexto("Código del torneo: "))
                        );
                        break;

                    case "5":
                        club.getGestorTorneos()
                            .liberarCuposPreferenciales(
                                leerTexto("Código del torneo: "));
                        System.out.println(
                            "Reserva preferencial liberada.");
                        break;

                    case "6":
                        club.getGestorTorneos().iniciarTorneo(
                            leerTexto("Código del torneo: "));
                        System.out.println("Torneo iniciado.");
                        break;

                    case "7":
                        club.getGestorTorneos().finalizarTorneo(
                            leerTexto("Código del torneo: "));
                        System.out.println("Torneo finalizado.");
                        break;

                    case "8":
                        new ConsolaEncuentros(club).mostrarMenu();
                        break;

                    case "11":
                        new ConsolaOperaciones(club, admin).mostrarMenu();
                        break;

                    case "10":
                        modelo.personas.ParticipanteExterno externo = new modelo.personas.ParticipanteExterno(
                            leerTexto("Documento: "), leerTexto("Nombre: "), LocalDate.parse(leerTexto("Nacimiento (AAAA-MM-DD): ")),
                            leerTexto("Login: "), leerTexto("Contraseña: "), leerTexto("Universitario (SI/NO): ").equalsIgnoreCase("SI"));
                        List<Deporte> deportes = club.getGestorDeportivo().getDeportes();
                        for (int i = 0; i < deportes.size(); i++) System.out.println((i + 1) + ". " + deportes.get(i).getNombre());
                        Deporte elegido = deportes.get(leerOpcion("Deporte o disciplina: ", deportes.size()) - 1);
                        if (!elegido.isEsDeConjunto()) externo.asignarNivel(elegido, leerOpcion("Nivel (1 a 3): ", 3));
                        club.getGestorUsuarios().registrarExterno(externo);
                        System.out.println("Participante registrado; puede entrar por la aplicación de socios.");
                        break;

                    case "9":
                        Torneo torneo = club.getGestorTorneos().buscarTorneo(leerTexto("Código del torneo: "));
                        if (torneo.getTipo() == TipoTorneo.RECREATIVO)
                            club.getGestorTorneos().entregarBonos(torneo.getCodigo(), Double.parseDouble(leerTexto("Porcentaje del bono: ")));
                        else System.out.println(club.getGestorTorneos().entregarPremiosDinero(torneo.getCodigo()));
                        System.out.println("Premios registrados.");
                        break;

                    default:
                        System.out.println("Opción inválida.");
                }
            }
            catch (ClubException e)
            {
                System.out.println(
                    "No se completó la operación: " + e.getMessage());
            }
            catch (DateTimeParseException e)
            {
                System.out.println(
                    "Fecha inválida. Use el formato AAAA-MM-DD.");
            }
            catch (NumberFormatException e)
            {
                System.out.println(
                    "Número inválido. No use separadores de miles. "
                    + "Para decimales, use punto.");
            }
            catch (IllegalArgumentException e)
            {
                System.out.println(
                    "Entrada inválida: " + e.getMessage());
            }
        }
    }

    private void consultarVentas() throws ClubException
    {
        System.out.println(
            "Ventas del mes: "
            + club.getGestorReportes()
                .generarReporteVentas(
                    LocalDate.now().withDayOfMonth(1),
                    LocalDate.now())
                .getTotal()
        );
    }

    private void crearTorneo() throws ClubException
    {
        List<Deporte> deportes =
            club.getGestorDeportivo().getDeportes();

        if (deportes.isEmpty())
        {
            throw new ClubException("No hay deportes registrados");
        }

        String codigo = leerTexto("Código único: ");
        String nombre = leerTexto("Nombre del torneo: ");

        System.out.println("\nDeportes disponibles:");

        for (int i = 0; i < deportes.size(); i++)
        {
            System.out.println(
                (i + 1) + ". " + deportes.get(i).getNombre());
        }

        Deporte deporte = deportes.get(
            leerOpcion("Número del deporte: ", deportes.size()) - 1);

        Categoria categoria = null;
        String nivel = null;
        ModalidadTorneo modalidad;

        if (deporte.isEsDeConjunto())
        {
            modalidad = ModalidadTorneo.EQUIPOS;
            categoria = elegirEnum("Categoría", Categoria.values());
        }
        else
        {
            System.out.println("\nModalidad:");
            System.out.println("1. Individual");
            System.out.println("2. Dobles");

            modalidad = leerOpcion("Seleccione: ", 2) == 1
                ? ModalidadTorneo.INDIVIDUAL
                : ModalidadTorneo.DOBLES;

            System.out.println("\nNivel:");
            System.out.println("1. Principiante");
            System.out.println("2. Intermedio");
            System.out.println("3. Avanzado");

            String[] niveles = {
                "Principiante", "Intermedio", "Avanzado"
            };

            nivel = niveles[leerOpcion("Seleccione: ", 3) - 1];
        }

        TipoTorneo tipo = elegirEnum(
            "Tipo de torneo", TipoTorneo.values());

        LocalDate inicio = LocalDate.parse(
            leerTexto("Fecha inicial (AAAA-MM-DD): "));

        LocalDate fin = LocalDate.parse(
            leerTexto("Fecha final (AAAA-MM-DD): "));

        int cupos = Integer.parseInt(
            leerTexto(
                "Cantidad de cupos (jugadores, parejas o equipos): "));

        double tarifa = 0;

        if (tipo == TipoTorneo.COMPETITIVO)
        {
            tarifa = Double.parseDouble(
                leerTexto(
                    "Tarifa de inscripción (ejemplo: 25000): "));
        }

        Torneo torneo = new Torneo(
            codigo, nombre, deporte, categoria, nivel,
            tipo, modalidad, inicio, fin, cupos, tarifa
        );

        club.getGestorTorneos().registrarTorneo(torneo);

        System.out.println("\nTorneo registrado correctamente.");
        mostrarDetalle(torneo);
    }

    private void listarTorneos()
    {
        List<Torneo> torneos = club.getGestorTorneos().getTorneos();

        if (torneos.isEmpty())
        {
            System.out.println("No hay torneos registrados.");
            return;
        }

        System.out.println("\n--- TORNEOS REGISTRADOS ---");

        for (Torneo torneo : torneos)
        {
            System.out.println(
                torneo.getCodigo() + " | "
                + torneo.getNombre() + " | "
                + torneo.getTipo() + " | "
                + torneo.getEstado()
            );
        }
    }

    private void mostrarDetalle(Torneo torneo) throws ClubException
    {
        System.out.println("\n--- DATOS DEL TORNEO ---");
        System.out.println("Código: " + torneo.getCodigo());
        System.out.println("Nombre: " + torneo.getNombre());
        System.out.println(
            "Deporte: " + torneo.getDeporte().getNombre());
        System.out.println("Tipo: " + torneo.getTipo());
        System.out.println("Modalidad: " + torneo.getModalidad());
        System.out.println("Estado: " + torneo.getEstado());

        if (torneo.getDeporte().isEsDeConjunto())
        {
            System.out.println("Categoría: " + torneo.getCategoria());
        }
        else
        {
            System.out.println("Nivel: " + torneo.getNivel());
        }

        System.out.println("Inicio: " + torneo.getFechaInicio());
        System.out.println("Fin: " + torneo.getFechaFin());
        System.out.println("Tarifa: " + torneo.getTarifaInscripcion());
        System.out.println(
            "Capacidad total: " + torneo.getCuposTotales());
        System.out.println(
            "Capacidad reservada para preferenciales: "
            + torneo.getCuposReservadosParaPreferenciales());
        System.out.println(
            "Capacidad habilitada para regulares: "
            + torneo.getCuposHabilitadosParaRegulares());
        System.out.println(
            "Estas capacidades no descuentan las inscripciones.");
        VistaHistorialTorneo.mostrar(torneo);
    }

    private String leerTexto(String mensaje)
    {
        String texto = pedirCadena(mensaje);

        if (texto.isEmpty())
        {
            throw new IllegalArgumentException(
                "Debe ingresar un valor");
        }

        return texto;
    }

    private int leerOpcion(String mensaje, int cantidad)
    {
        int opcion = Integer.parseInt(leerTexto(mensaje));

        if (opcion < 1 || opcion > cantidad)
        {
            throw new IllegalArgumentException(
                "Seleccione una opción entre 1 y " + cantidad);
        }

        return opcion;
    }

    private <T extends Enum<T>> T elegirEnum(
            String titulo, T[] opciones)
    {
        System.out.println("\n" + titulo + ":");

        for (int i = 0; i < opciones.length; i++)
        {
            System.out.println((i + 1) + ". " + opciones[i]);
        }

        return opciones[
            leerOpcion("Seleccione: ", opciones.length) - 1];
    }
}

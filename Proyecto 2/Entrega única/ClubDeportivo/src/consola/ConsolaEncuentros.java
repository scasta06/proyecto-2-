package consola;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import excepciones.ClubException;
import logica.ClubDeportivo;
import modelo.torneos.EncuentroTorneo;
import modelo.torneos.HistorialTorneo;
import modelo.torneos.InscripcionTorneo;
import modelo.torneos.Torneo;

public class ConsolaEncuentros extends ConsolaBase
{
    public ConsolaEncuentros(ClubDeportivo club)
    {
        super(club);
    }

    @Override
    public void mostrarMenu() throws ClubException
    {
        while (true)
        {
            System.out.println("\n--- ENCUENTROS DE TORNEOS ---");
            System.out.println("1. Programar encuentro");
            System.out.println("2. Registrar victoria");
            System.out.println("3. Registrar empate");
            System.out.println("4. Consultar historial y posiciones");
            System.out.println("0. Volver");
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

            if (!opcion.equals("1") && !opcion.equals("2")
                    && !opcion.equals("3") && !opcion.equals("4"))
            {
                System.out.println("Opción inválida.");
                continue;
            }

            try
            {
                Torneo torneo = club.getGestorTorneos().buscarTorneo(
                    leerTexto("Código del torneo: "));

                HistorialTorneo historial = torneo.getHistorial();

                switch (opcion)
                {
                    case "1":
                        programarEncuentro(historial);
                        break;

                    case "2":
                        registrarVictoria(historial);
                        break;

                    case "3":
                        registrarEmpate(historial);
                        break;

                    case "4":
                        VistaHistorialTorneo.mostrar(torneo);
                        break;

                    default:
                        break;
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
                    "Fecha u hora inválida. Use AAAA-MM-DD y HH:mm.");
            }
            catch (IllegalArgumentException e)
            {
                System.out.println(
                    "Entrada inválida: " + e.getMessage());
            }
        }
    }

    private void programarEncuentro(HistorialTorneo historial)
            throws ClubException
    {
        if (historial.getInscripciones().size() < 2)
        {
            throw new ClubException(
                "Se requieren al menos dos inscripciones registradas");
        }

        System.out.println("\nInscripciones registradas:");

        for (InscripcionTorneo inscripcion : historial.getInscripciones())
        {
            System.out.println(
                inscripcion.getCodigo() + " | "
                + inscripcion.getNombreParticipacion());
        }

        String codigo = leerTexto("Código único del encuentro: ");
        String codigoA = leerTexto("Código de la primera inscripción: ");
        String codigoB = leerTexto("Código de la segunda inscripción: ");

        LocalDate fecha = LocalDate.parse(
            leerTexto("Fecha (AAAA-MM-DD): "));

        LocalTime hora = LocalTime.parse(
            leerTexto("Hora (HH:mm, ejemplo 14:30): "));

        EncuentroTorneo encuentro = new EncuentroTorneo(
            codigo,
            codigoA,
            codigoB,
            LocalDateTime.of(fecha, hora)
        );

        historial.agregarEncuentro(encuentro);

        System.out.println("Encuentro programado correctamente.");
    }

    private void registrarVictoria(HistorialTorneo historial)
            throws ClubException
    {
        String codigo = leerTexto("Código del encuentro: ");

        EncuentroTorneo encuentro = historial.buscarEncuentro(codigo);

        mostrarParticipantes(historial, encuentro);

        String ganador = leerTexto("Código de la inscripción ganadora: ");
        String resultado = leerTexto(
            "Resultado (ejemplo: 6-4, 6-3 o 2-1): ");

        historial.registrarVictoria(codigo, ganador, resultado);

        System.out.println("Victoria registrada correctamente.");
    }

    private void registrarEmpate(HistorialTorneo historial)
            throws ClubException
    {
        String codigo = leerTexto("Código del encuentro: ");

        EncuentroTorneo encuentro = historial.buscarEncuentro(codigo);

        mostrarParticipantes(historial, encuentro);

        String resultado = leerTexto("Resultado del empate: ");

        historial.registrarEmpate(codigo, resultado);

        System.out.println("Empate registrado correctamente.");
    }

    private void mostrarParticipantes(
            HistorialTorneo historial,
            EncuentroTorneo encuentro) throws ClubException
    {
        InscripcionTorneo a = historial.buscarInscripcion(
            encuentro.getCodigoInscripcionA());

        InscripcionTorneo b = historial.buscarInscripcion(
            encuentro.getCodigoInscripcionB());

        System.out.println(
            a.getCodigo() + ": " + a.getNombreParticipacion());

        System.out.println(
            b.getCodigo() + ": " + b.getNombreParticipacion());
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
}

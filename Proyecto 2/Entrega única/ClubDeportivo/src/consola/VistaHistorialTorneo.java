package consola;

import java.util.List;

import excepciones.ClubException;
import modelo.personas.Persona;
import modelo.torneos.EncuentroTorneo;
import modelo.torneos.EstadoEncuentro;
import modelo.torneos.HistorialTorneo;
import modelo.torneos.InscripcionTorneo;
import modelo.torneos.TablaPosicionesTorneo;
import modelo.torneos.TablaPosicionesTorneo.Fila;
import modelo.torneos.Torneo;

public class VistaHistorialTorneo
{
    public static void mostrar(Torneo torneo) throws ClubException
    {
        HistorialTorneo historial = torneo.getHistorial();

        System.out.println("\n--- HISTORIAL DEL TORNEO ---");
        System.out.println(
            torneo.getCodigo() + " - " + torneo.getNombre());
        System.out.println("Estado: " + torneo.getEstado());

        System.out.println("\nParticipaciones registradas:");

        if (historial.getInscripciones().isEmpty())
        {
            System.out.println("No hay inscripciones registradas.");
        }

        for (InscripcionTorneo inscripcion : historial.getInscripciones())
        {
            System.out.println(
                inscripcion.getCodigo() + " | "
                + inscripcion.getNombreParticipacion() + " | "
                + inscripcion.getModalidad());

            for (Persona persona : inscripcion.getParticipantes())
            {
                System.out.println(
                    "  Participante: " + persona.getNombre());
            }
        }

        System.out.println("\nEncuentros en orden cronológico:");

        if (historial.getEncuentros().isEmpty())
        {
            System.out.println("No hay encuentros registrados.");
        }

        for (EncuentroTorneo encuentro : historial.getEncuentros())
        {
            String nombreA = historial.buscarInscripcion(
                encuentro.getCodigoInscripcionA())
                .getNombreParticipacion();

            String nombreB = historial.buscarInscripcion(
                encuentro.getCodigoInscripcionB())
                .getNombreParticipacion();

            System.out.println(
                encuentro.getCodigo() + " | "
                + encuentro.getFechaHora() + " | "
                + nombreA + " vs. " + nombreB);

            System.out.println("  Estado: " + encuentro.getEstado());

            if (encuentro.getEstado() == EstadoEncuentro.FINALIZADO)
            {
                System.out.println(
                    "  Resultado: " + encuentro.getResultado());

                if (encuentro.isEmpate())
                {
                    System.out.println("  Empate.");
                }
                else
                {
                    String ganador = historial.buscarInscripcion(
                        encuentro.getCodigoInscripcionGanadora())
                        .getNombreParticipacion();

                    System.out.println("  Ganador: " + ganador);
                }
            }
        }

        for (InscripcionTorneo retirada : historial.getInscripcionesRetiradas())
            System.out.println("Retirada: " + retirada.getCodigo() + " | " + retirada.getNombreParticipacion() + " | importe liberado: " + retirada.getValorPagado());
        mostrarPosiciones(torneo);
    }

    private static void mostrarPosiciones(Torneo torneo)
            throws ClubException
    {
        List<Fila> tabla = TablaPosicionesTorneo.calcular(torneo);

        System.out.println("\n--- POSICIONES POR PUNTOS ---");

        if (tabla.isEmpty())
        {
            System.out.println("No hay participantes para clasificar.");
            return;
        }

        System.out.println(
            "Pos. | Participación | PJ | PG | PE | PP | Puntos");

        for (Fila fila : tabla)
        {
            System.out.println(
                fila.getPosicion() + " | "
                + fila.getNombre() + " | "
                + fila.getJugados() + " | "
                + fila.getGanados() + " | "
                + fila.getEmpatados() + " | "
                + fila.getPerdidos() + " | "
                + fila.getPuntos());
        }

        System.out.println(
            "Igualdad de puntos: posición compartida. "
            + "Esta tabla no asigna premios.");
    }
}

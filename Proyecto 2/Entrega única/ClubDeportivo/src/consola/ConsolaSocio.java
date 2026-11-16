package consola;

import logica.ClubDeportivo;
import modelo.actividades.Reserva;
import modelo.personas.Socio;

public class ConsolaSocio extends ConsolaBase
{
    private Socio socio;
    public ConsolaSocio(ClubDeportivo c,Socio p)
    {
        super(c);
        socio=p;
    }
    @Override
    public void mostrarMenu()
    {
        while(true)
        {
            System.out.println("1. Consultar resumen / 2. Consultar torneos / 3. Retirar inscripción / 9. Torneos y compras / 8. Otras operaciones / 0. Salir");
            String opcion=pedirCadena("Opción: ");
            if(opcion.equals("0")||opcion.isEmpty())return;
            if(opcion.equals("1"))
            {
                System.out.println("Puntos: "+socio.getCuenta().getPuntos());
                int cantidad = 0;
                for (Reserva reserva : club.getGestorInstalaciones().getReservas())
                {
                    if (reserva.getSocio() == socio)
                    {
                        cantidad++;
                    }
                }
                System.out.println("Total: " + cantidad);
            }
            else if (opcion.equals("2") || opcion.equals("3"))
            {
                try
                {
                    for (modelo.torneos.Torneo torneo : club.getGestorTorneos().getTorneos())
                    {
                        System.out.println(torneo.getCodigo() + " | " + torneo.getNombre() + " | " + torneo.getEstado());
                    }
                    modelo.torneos.Torneo torneo = club.getGestorTorneos().buscarTorneo(pedirCadena("Código del torneo: "));
                    if (opcion.equals("2")) VistaHistorialTorneo.mostrar(torneo);
                    else
                    {
                        torneo.getHistorial().retirarInscripcion(pedirCadena("Código de inscripción: "), socio);
                        System.out.println("Inscripción retirada. Cupo liberado.");
                    }
                }
                catch (excepciones.ClubException e)
                {
                    System.out.println(e.getMessage());
                }
            }
            else if (opcion.equals("9")) new ConsolaParticipacion(club, socio).mostrarMenu();
            else if (opcion.equals("8")) new ConsolaOperaciones(club, socio).mostrarMenu();
            else System.out.println("Opción inválida");
        }
    }
}


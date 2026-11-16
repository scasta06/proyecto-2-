package consola;

import logica.ClubDeportivo;
import modelo.actividades.SesionEntrenamiento;
import modelo.personas.Entrenador;

public class ConsolaEntrenador extends ConsolaBase
{
    private Entrenador entrenador;
    public ConsolaEntrenador(ClubDeportivo c,Entrenador p)
    {
        super(c);
        entrenador=p;
    }
    @Override
    public void mostrarMenu()
    {
        while(true)
        {
            System.out.println("1. Consultar resumen / 9. Torneos y compras / 8. Otras operaciones / 0. Salir");
            String opcion=pedirCadena("Opción: ");
            if(opcion.equals("0")||opcion.isEmpty())return;
            if(opcion.equals("1"))
            {
                int cantidad = 0;
                for (SesionEntrenamiento sesion : club.getGestorDeportivo().getSesiones())
                {
                    if (sesion.getEntrenador() == entrenador)
                    {
                        cantidad++;
                    }
                }
                System.out.println("Total: " + cantidad);
            }
            else if (opcion.equals("9")) new ConsolaParticipacion(club, entrenador).mostrarMenu();
            else if (opcion.equals("8")) new ConsolaOperaciones(club, entrenador).mostrarMenu();
            else System.out.println("Opción inválida");
        }
    }
}


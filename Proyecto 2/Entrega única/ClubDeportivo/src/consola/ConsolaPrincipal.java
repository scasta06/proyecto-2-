package consola;

import excepciones.ClubException;
import logica.ClubDeportivo;
import modelo.personas.Administrador;
import modelo.personas.Empleado;
import modelo.personas.Entrenador;
import modelo.personas.Fisioterapeuta;
import modelo.personas.Persona;
import modelo.personas.Socio;
import persistencia.IPersistenciaClub;
import persistencia.DatosIniciales;
import persistencia.PersistenciaJson;

public class ConsolaPrincipal
{
    private ClubDeportivo club;

    public static void main(String[] args)
    {
        iniciar("TODOS");
    }

    public static void iniciar(String rol)
    {
        String ruta = "datos/club-ejemplo.json";
        IPersistenciaClub persistencia = new PersistenciaJson();
        ConsolaPrincipal aplicacion = new ConsolaPrincipal();
        try
        {
            if (!new java.io.File(ruta).exists())
            {
                aplicacion.club = DatosIniciales.crear();
                persistencia.salvar(aplicacion.club, ruta);
            }
            else aplicacion.club = persistencia.cargar(ruta);
        }
        catch (ClubException e)
        {
            System.err.println("No se pudieron cargar los datos: " + e.getMessage());
            return;
        }
        try
        {
            aplicacion.iniciarSesion(rol);
        }
        catch (ClubException e)
        {
            System.err.println(e.getMessage());
        }
        catch (IllegalArgumentException e)
        {
            System.err.println(e.getMessage());
        }
        finally
        {
            try
            {
                persistencia.salvar(aplicacion.club, ruta);
            }
            catch (ClubException e)
            {
                System.err.println("No se pudieron guardar los cambios: " + e.getMessage());
            }
        }
    }

    private void iniciarSesion(String rol) throws ClubException
    {
        while (true)
        {
            System.out.print("Login (0 para salir): ");
            if (!ConsolaBase.ENTRADA.hasNextLine()) return;
            String login = ConsolaBase.ENTRADA.nextLine().trim();
            if (login.equals("0")) return;
            System.out.print("Contraseña: ");
            if (!ConsolaBase.ENTRADA.hasNextLine()) return;
            String clave = ConsolaBase.ENTRADA.nextLine();
            Persona persona;
            try
            {
                persona = club.iniciarSesion(login, clave);
            }
            catch (ClubException e)
            {
                System.out.println("Login o contraseña incorrectos.");
                continue;
            }
            if ((rol.equals("SOCIO") && !(persona instanceof Socio) && !(persona instanceof modelo.personas.ParticipanteExterno))
                    || (rol.equals("EMPLEADO") && (!(persona instanceof Empleado) || persona instanceof Administrador))
                    || (rol.equals("ADMINISTRADOR") && !(persona instanceof Administrador)))
            {
                System.out.println("La cuenta no corresponde al rol de esta aplicación.");
                continue;
            }
            abrirConsola(persona);
            return;
        }
    }

    private void abrirConsola(Persona persona) throws ClubException
    {
        ConsolaBase consola;
        if (persona instanceof modelo.personas.ParticipanteExterno)
            consola = new ConsolaParticipacion(club, persona);
        else if (persona instanceof Socio)
            consola = new ConsolaSocio(club, (Socio) persona);
        else if (persona instanceof Entrenador)
            consola = new ConsolaEntrenador(club, (Entrenador) persona);
        else if (persona instanceof Fisioterapeuta)
            consola = new ConsolaFisioterapeuta(club, (Fisioterapeuta) persona);
        else if (persona instanceof Administrador)
            consola = new ConsolaAdministrador(club, (Administrador) persona);
        else
            throw new ClubException("No hay una consola para este tipo de usuario");
        consola.mostrarMenu();
    }
}

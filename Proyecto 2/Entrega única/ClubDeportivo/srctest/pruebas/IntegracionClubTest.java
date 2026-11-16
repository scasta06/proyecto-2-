package pruebas;

import org.junit.jupiter.api.Test;
import excepciones.ClubException;

public class IntegracionClubTest
{
    @Test
    void reservas() throws ClubException
    {
        PruebasRevision.reservas();
    }
    @Test
    void deportivo() throws ClubException
    {
        PruebasRevision.deportivo();
    }
    @Test
    void comercio() throws ClubException
    {
        PruebasRevision.comercio();
    }
    @Test
    void inventario() throws ClubException
    {
        PruebasRevision.inventario();
    }
    @Test
    void personal() throws ClubException
    {
        PruebasRevision.personal();
    }
    @Test
    void competencias() throws ClubException
    {
        PruebasRevision.competencias();
    }
    @Test
    void otrosDeportes() throws ClubException
    {
        PruebasRevision.otrosDeportes();
    }
    @Test
    void persistencia() throws ClubException
    {
        PruebasRevision.persistencia();
    }
}

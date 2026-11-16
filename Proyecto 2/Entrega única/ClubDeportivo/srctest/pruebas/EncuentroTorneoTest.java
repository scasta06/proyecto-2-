package pruebas;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import excepciones.ClubException;
import modelo.torneos.EncuentroTorneo;
import modelo.torneos.EstadoEncuentro;

public class EncuentroTorneoTest
{
    private EncuentroTorneo encuentro;

    @BeforeEach
    void preparar() throws ClubException
    {
        encuentro = new EncuentroTorneo(
            "E01",
            "I01",
            "I02",
            LocalDateTime.of(2027, 3, 10, 10, 0)
        );
    }

    @Test
    void registraVictoriaYConservaLosParticipantes()
            throws ClubException
    {
        encuentro.registrarVictoria("I01", "6-4, 6-3");

        assertEquals(
            EstadoEncuentro.FINALIZADO,
            encuentro.getEstado()
        );
        assertEquals("I01", encuentro.getCodigoInscripcionGanadora());
        assertEquals("6-4, 6-3", encuentro.getResultado());
        assertEquals("I01", encuentro.getCodigoInscripcionA());
        assertEquals("I02", encuentro.getCodigoInscripcionB());
        assertFalse(encuentro.isEmpate());
    }

    @Test
    void rechazaGanadorAjenoSinModificarElEncuentro()
    {
        assertThrows(ClubException.class, () ->
            encuentro.registrarVictoria("I99", "6-4, 6-3")
        );

        assertEquals(
            EstadoEncuentro.PROGRAMADO,
            encuentro.getEstado()
        );
        assertNull(encuentro.getCodigoInscripcionGanadora());
        assertNull(encuentro.getResultado());
        assertFalse(encuentro.isEmpate());
    }

    @Test
    void impideReemplazarUnResultadoRegistrado()
            throws ClubException
    {
        encuentro.registrarVictoria("I01", "6-4, 6-3");

        assertThrows(ClubException.class, () ->
            encuentro.registrarVictoria("I02", "0-6, 0-6")
        );

        assertThrows(ClubException.class, () ->
            encuentro.registrarEmpate("Empate")
        );

        assertEquals("I01", encuentro.getCodigoInscripcionGanadora());
        assertEquals("6-4, 6-3", encuentro.getResultado());
    }

    @Test
    void rechazaEnfrentamientoContraLaMismaInscripcion()
    {
        assertThrows(ClubException.class, () ->
            new EncuentroTorneo(
                "E02",
                "I01",
                " i01 ",
                LocalDateTime.of(2027, 3, 10, 10, 0)
            )
        );
    }

    @Test
    void rechazaResultadoVacioSinCambiarEstado()
    {
        assertThrows(ClubException.class, () ->
            encuentro.registrarVictoria("I01", "   ")
        );

        assertEquals(
            EstadoEncuentro.PROGRAMADO,
            encuentro.getEstado()
        );
        assertNull(encuentro.getResultado());
        assertNull(encuentro.getCodigoInscripcionGanadora());
    }

    @Test
    void representaEmpateSinAsignarGanador() throws ClubException
    {
        encuentro.registrarEmpate("2-2");

        assertEquals(
            EstadoEncuentro.FINALIZADO,
            encuentro.getEstado()
        );
        assertTrue(encuentro.isEmpate());
        assertNull(encuentro.getCodigoInscripcionGanadora());
        assertEquals("2-2", encuentro.getResultado());
    }
}

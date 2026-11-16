package pruebas;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import excepciones.ClubException;
import modelo.deportes.Deporte;
import modelo.personas.Persona;
import modelo.personas.Socio;
import modelo.tipos.TipoEstadistica;
import modelo.torneos.EncuentroTorneo;
import modelo.torneos.HistorialTorneo;
import modelo.torneos.InscripcionTorneo;
import modelo.torneos.ModalidadTorneo;
import modelo.torneos.TablaPosicionesTorneo;
import modelo.torneos.TablaPosicionesTorneo.Fila;
import modelo.torneos.TipoTorneo;
import modelo.torneos.Torneo;

import java.nio.file.Path;
import java.time.LocalTime;

import org.junit.jupiter.api.io.TempDir;

import logica.ClubDeportivo;
import modelo.torneos.EstadoTorneo;
import persistencia.PersistenciaJson;

public class HistorialTorneoTest
{
    private Torneo torneo;
    private HistorialTorneo historial;

    @BeforeEach
    void preparar() throws ClubException
    {
        Deporte tenis = new Deporte(
            "Tenis", false,
            1, 2, 4, 1, 2, 60,
            false, 1,
            Collections.emptyList(),
            TipoEstadistica.INDIVIDUAL
        );

        torneo = new Torneo(
            "T01", "Torneo de prueba",
            tenis, null, "Intermedio",
            TipoTorneo.RECREATIVO,
            ModalidadTorneo.INDIVIDUAL,
            LocalDate.of(2027, 3, 10),
            LocalDate.of(2027, 3, 12),
            16, 0
        );

        historial = torneo.getHistorial();

        Persona ana = new Socio(
            "101", "Ana",
            LocalDate.of(2000, 1, 1),
            "ana", "clave123", "S01"
        );

        Persona luis = new Socio(
            "102", "Luis",
            LocalDate.of(2000, 1, 1),
            "luis", "clave123", "S02"
        );

        historial.registrarInscripcionConfirmada(
            new InscripcionTorneo(
                "I01", "T01", "Ana",
                ModalidadTorneo.INDIVIDUAL,
                Collections.singletonList(ana),
                null,
                LocalDateTime.of(2027, 3, 1, 9, 0)
            )
        );

        historial.registrarInscripcionConfirmada(
            new InscripcionTorneo(
                "I02", "T01", "Luis",
                ModalidadTorneo.INDIVIDUAL,
                Collections.singletonList(luis),
                null,
                LocalDateTime.of(2027, 3, 1, 9, 5)
            )
        );
    }

    private void programarEncuentro() throws ClubException
    {
        historial.agregarEncuentro(
            new EncuentroTorneo(
                "E01", "I01", "I02",
                LocalDateTime.of(2027, 3, 10, 10, 0)
            )
        );
    }

    @Test
    void calculaPosicionesYConservaHistorialAlFinalizar()
            throws ClubException
    {
        programarEncuentro();
        torneo.iniciar();

        historial.registrarVictoria("E01", "I01", "6-4, 6-3");
        torneo.finalizar();

        List<Fila> tabla = TablaPosicionesTorneo.calcular(torneo);

        assertEquals(2, historial.getInscripciones().size());
        assertEquals(1, historial.getEncuentros().size());

        assertEquals("I01", tabla.get(0).getCodigo());
        assertEquals(1, tabla.get(0).getPosicion());
        assertEquals(3, tabla.get(0).getPuntos());
        assertEquals(1, tabla.get(0).getGanados());

        assertEquals("I02", tabla.get(1).getCodigo());
        assertEquals(2, tabla.get(1).getPosicion());
        assertEquals(0, tabla.get(1).getPuntos());
        assertEquals(1, tabla.get(1).getPerdidos());
    }

    @Test
    void consultarDosVecesNoDuplicaPuntos() throws ClubException
    {
        programarEncuentro();
        torneo.iniciar();
        historial.registrarVictoria("E01", "I01", "6-4, 6-3");

        TablaPosicionesTorneo.calcular(torneo);
        List<Fila> segundaConsulta =
            TablaPosicionesTorneo.calcular(torneo);

        assertEquals(3, segundaConsulta.get(0).getPuntos());
        assertEquals(1, segundaConsulta.get(0).getJugados());
    }

    @Test
    void rechazaEncuentroConInscripcionInexistente()
    {
        assertThrows(ClubException.class, () ->
            historial.agregarEncuentro(
                new EncuentroTorneo(
                    "E01", "I01", "I99",
                    LocalDateTime.of(2027, 3, 10, 10, 0)
                )
            )
        );

        assertTrue(historial.getEncuentros().isEmpty());
    }

    @Test
    void rechazaEncuentroFueraDeLasFechas()
    {
        assertThrows(ClubException.class, () ->
            historial.agregarEncuentro(
                new EncuentroTorneo(
                    "E01", "I01", "I02",
                    LocalDateTime.of(2027, 4, 1, 10, 0)
                )
            )
        );

        assertTrue(historial.getEncuentros().isEmpty());
    }

    @Test
    void rechazaEmpateEnTenis() throws ClubException
    {
        programarEncuentro();
        torneo.iniciar();

        assertThrows(ClubException.class, () ->
            historial.registrarEmpate("E01", "Empate")
        );

        assertTrue(historial.tieneEncuentrosPendientes());
        assertNull(historial.buscarEncuentro("E01").getResultado());
    }

    @Test
    void encuentrosPendientesNoSumanPartidosNiPuntos()
            throws ClubException
    {
        programarEncuentro();

        List<Fila> tabla = TablaPosicionesTorneo.calcular(torneo);

        for (Fila fila : tabla)
        {
            assertEquals(0, fila.getJugados());
            assertEquals(0, fila.getPuntos());
            assertEquals(1, fila.getPosicion());
        }
    }

    @Test
    void impideFinalizarConEncuentrosPendientes() throws ClubException
    {
        programarEncuentro();
        torneo.iniciar();

        assertThrows(ClubException.class, () ->
            torneo.finalizar()
        );

        assertEquals(EstadoTorneo.EN_CURSO, torneo.getEstado());
        assertTrue(historial.tieneEncuentrosPendientes());

        historial.registrarVictoria("E01", "I01", "6-4, 6-3");
        torneo.finalizar();

        assertEquals(EstadoTorneo.FINALIZADO, torneo.getEstado());
        assertFalse(historial.tieneEncuentrosPendientes());
    }

    @Test
    void guardaYRecuperaHistorialCompleto(@TempDir Path carpeta)
            throws ClubException
    {
        programarEncuentro();
        torneo.iniciar();
        historial.registrarVictoria("E01", "I01", "6-4, 6-3");
        torneo.finalizar();

        ClubDeportivo club = new ClubDeportivo(
            "Club de prueba",
            "123",
            "Bogotá",
            LocalTime.of(6, 0),
            LocalTime.of(22, 0)
        );

        club.getGestorTorneos().registrarTorneo(torneo);

        String ruta = carpeta.resolve("club-historial.json").toString();
        PersistenciaJson persistencia = new PersistenciaJson();

        persistencia.salvar(club, ruta);

        ClubDeportivo recuperado = persistencia.cargar(ruta);
        Torneo torneoRecuperado =
            recuperado.getGestorTorneos().buscarTorneo("T01");

        HistorialTorneo historialRecuperado =
            torneoRecuperado.getHistorial();

        assertEquals(
            EstadoTorneo.FINALIZADO,
            torneoRecuperado.getEstado()
        );

        assertEquals(2, historialRecuperado.getInscripciones().size());
        assertEquals(1, historialRecuperado.getEncuentros().size());

        assertEquals(
            "Ana",
            historialRecuperado.buscarInscripcion("I01")
                .getParticipantes().get(0).getNombre()
        );

        assertEquals(
            "6-4, 6-3",
            historialRecuperado.buscarEncuentro("E01").getResultado()
        );

        assertEquals(
            "I01",
            historialRecuperado.buscarEncuentro("E01")
                .getCodigoInscripcionGanadora()
        );

        List<Fila> tabla =
            TablaPosicionesTorneo.calcular(torneoRecuperado);

        assertEquals("I01", tabla.get(0).getCodigo());
        assertEquals(3, tabla.get(0).getPuntos());

        assertThrows(ClubException.class, () ->
            historialRecuperado.agregarEncuentro(
                new EncuentroTorneo(
                    "E02", "I01", "I02",
                    LocalDateTime.of(2027, 3, 11, 10, 0)
                )
            )
        );

        assertEquals(1, historialRecuperado.getEncuentros().size());
    }

    @Test
    void rechazaPersonaInscritaConOtroCodigo() throws ClubException
    {
        Persona ana = historial.buscarInscripcion("I01").getParticipantes().get(0);
        InscripcionTorneo repetida = new InscripcionTorneo(
            "OTRA", "T01", "Ana otra vez", ModalidadTorneo.INDIVIDUAL,
            Collections.singletonList(ana), null,
            LocalDateTime.of(2027, 3, 1, 10, 0));
        assertThrows(ClubException.class, () ->
            historial.registrarInscripcionConfirmada(repetida));
        assertEquals(2, historial.getInscripciones().size());
    }

    @Test
    void rechazaInscripcionQueSuperaCapacidad() throws ClubException
    {
        for (int i = 3; i <= torneo.getCuposTotales(); i++)
        {
            historial.registrarInscripcionConfirmada(crearInscripcionAdicional(i));
        }
        InscripcionTorneo excedente = crearInscripcionAdicional(17);
        assertThrows(ClubException.class, () ->
            historial.registrarInscripcionConfirmada(excedente));
        assertEquals(torneo.getCuposTotales(), historial.getInscripciones().size());
    }

    private InscripcionTorneo crearInscripcionAdicional(int numero) throws ClubException
    {
        Persona socio = new Socio("DOC" + numero, "Socio " + numero,
            LocalDate.of(2000, 1, 1), "login" + numero, "clave", "S" + numero);
        return new InscripcionTorneo("I" + numero, "T01", socio.getNombre(),
            ModalidadTorneo.INDIVIDUAL, Collections.singletonList(socio), null,
            LocalDateTime.of(2027, 3, 1, 10, 0));
    }

    @Test
    void retiroLiberaCupoYEliminaEncuentrosProgramados() throws ClubException
    {
        programarEncuentro();
        Persona ana = historial.buscarInscripcion("I01").getParticipantes().get(0);
        historial.retirarInscripcion("I01", ana);
        assertEquals(1, historial.getInscripciones().size());
        assertTrue(historial.getEncuentros().isEmpty());
        historial.registrarInscripcionConfirmada(crearInscripcionAdicional(3));
        assertEquals(2, historial.getInscripciones().size());
    }

    @Test
    void retiroAjenoNoModificaHistorial() throws ClubException
    {
        programarEncuentro();
        Persona luis = historial.buscarInscripcion("I02").getParticipantes().get(0);
        assertThrows(ClubException.class, () -> historial.retirarInscripcion("I01", luis));
        assertEquals(2, historial.getInscripciones().size());
        assertEquals(1, historial.getEncuentros().size());
    }

    @Test
    void retiroEnCursoNoModificaHistorial() throws ClubException
    {
        Persona ana = historial.buscarInscripcion("I01").getParticipantes().get(0);
        torneo.iniciar();
        assertThrows(ClubException.class, () -> historial.retirarInscripcion("I01", ana));
        assertEquals(2, historial.getInscripciones().size());
    }
}

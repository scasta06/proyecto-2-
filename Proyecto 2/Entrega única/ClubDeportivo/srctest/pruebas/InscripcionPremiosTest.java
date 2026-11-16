package pruebas;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Map;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import excepciones.ClubException;
import logica.ClubDeportivo;
import modelo.deportes.Deporte;
import modelo.personas.Socio;
import modelo.personas.Entrenador;
import modelo.personas.ParticipanteExterno;
import modelo.personal.Turno;
import modelo.tipos.TipoEstadistica;
import modelo.torneos.*;
import persistencia.PersistenciaJson;

public class InscripcionPremiosTest
{
    private ClubDeportivo club() throws ClubException
    {
        return new ClubDeportivo("Club", "1", "Bogotá", LocalTime.of(8,0), LocalTime.of(20,0));
    }
    private Torneo torneo(ClubDeportivo club, int cupos, TipoTorneo tipo) throws ClubException
    {
        Deporte deporte = new Deporte("Tenis", false, 1, 2, 4, 1, 2, 60, false, 1, Collections.emptyList(), TipoEstadistica.INDIVIDUAL);
        club.getGestorDeportivo().registrarDeporte(deporte);
        Torneo torneo = new Torneo("T", "Torneo", deporte, null, "Intermedio", tipo,
            ModalidadTorneo.INDIVIDUAL, LocalDate.now().plusDays(10), LocalDate.now().plusDays(11), cupos,
            tipo == TipoTorneo.COMPETITIVO ? 100 : 0);
        club.getGestorTorneos().registrarTorneo(torneo);
        return torneo;
    }
    private Socio socio(ClubDeportivo club, Torneo torneo, String id, int nivel) throws ClubException
    {
        Socio socio = new Socio(id, id, LocalDate.of(2000,1,1), id, "clave", id);
        socio.asignarNivel(torneo.getDeporte(), nivel);
        club.getGestorUsuarios().registrarSocio(socio);
        return socio;
    }
    private void inscribir(ClubDeportivo club, String id, modelo.personas.Persona persona) throws ClubException
    {
        club.getGestorTorneos().inscribir(club, "T", id, id, Collections.singletonList(persona), null);
    }
    private void terminar(Torneo torneo) throws ClubException
    {
        torneo.getHistorial().agregarEncuentro(new EncuentroTorneo("E", "A", "B", torneo.getFechaInicio().atTime(10,0)));
        torneo.iniciar();
        torneo.getHistorial().registrarVictoria("E", "A", "2-0");
        torneo.finalizar();
    }
    @Test
    void respetaReservaYLiberaCupoTrasRetiro() throws ClubException
    {
        ClubDeportivo club = club(); Torneo torneo = torneo(club, 2, TipoTorneo.COMPETITIVO);
        Socio a = socio(club, torneo, "1", 2); Socio b = socio(club, torneo, "2", 2);
        inscribir(club, "A", a);
        assertThrows(ClubException.class, () -> inscribir(club, "B", b));
        assertEquals(100, club.getGestorTorneos().consultarRecaudo("T"));
        torneo.getHistorial().retirarInscripcion("A", a);
        assertEquals(0, club.getGestorTorneos().consultarRecaudo("T"));
        inscribir(club, "B", b);
        torneo.liberarCuposPreferenciales();
        inscribir(club, "A", a);
        assertEquals(2, torneo.getHistorial().getInscripciones().size());
    }
    @Test
    void rechazaNivelDistintoSinCobrar() throws ClubException
    {
        ClubDeportivo club = club(); Torneo torneo = torneo(club, 5, TipoTorneo.COMPETITIVO);
        Socio socio = socio(club, torneo, "1", 1);
        assertThrows(ClubException.class, () -> inscribir(club, "A", socio));
        assertTrue(torneo.getHistorial().getInscripciones().isEmpty());
        assertEquals(0, club.getGestorTorneos().consultarRecaudo("T"));
    }
    @Test
    void empleadoSinTurnoNoPagaYConTurnoNoSeInscribe() throws ClubException
    {
        ClubDeportivo club = club(); Torneo torneo = torneo(club, 5, TipoTorneo.COMPETITIVO);
        Entrenador empleado = new Entrenador("E", "E", LocalDate.of(1990,1,1), "e", "clave", "E", "licencia");
        club.getGestorUsuarios().registrarEmpleado(empleado);
        inscribir(club, "A", empleado);
        assertEquals(0, club.getGestorTorneos().consultarRecaudo("T"));
        torneo.getHistorial().retirarInscripcion("A", empleado);
        club.getGestorPersonal().asignarTurno(new Turno("TU", torneo.getFechaInicio(), LocalTime.of(8,0), LocalTime.of(12,0), empleado));
        assertThrows(ClubException.class, () -> inscribir(club, "A", empleado));
    }
    @Test
    void externoRegistradoPuedeParticipar() throws ClubException
    {
        ClubDeportivo club = club(); Torneo torneo = torneo(club, 5, TipoTorneo.RECREATIVO);
        ParticipanteExterno externo = new ParticipanteExterno("X", "X", LocalDate.of(2000,1,1), "x", "clave", true);
        externo.asignarNivel(torneo.getDeporte(), 2);
        club.getGestorUsuarios().registrarExterno(externo);
        inscribir(club, "A", externo);
        assertEquals(externo, torneo.getHistorial().buscarInscripcion("A").getParticipantes().get(0));
    }
    @Test
    void premioNoDuplicaYSeConserva(@TempDir Path carpeta) throws ClubException
    {
        ClubDeportivo club = club(); Torneo torneo = torneo(club, 5, TipoTorneo.COMPETITIVO);
        inscribir(club, "A", socio(club, torneo, "1", 2));
        inscribir(club, "B", socio(club, torneo, "2", 2));
        terminar(torneo);
        Map<String, Double> premios = club.getGestorTorneos().entregarPremiosDinero("T");
        assertEquals(200.0, premios.get("1"));
        assertThrows(ClubException.class, () -> club.getGestorTorneos().entregarPremiosDinero("T"));
        PersistenciaJson persistencia = new PersistenciaJson();
        String ruta = carpeta.resolve("club.json").toString();
        persistencia.salvar(club, ruta);
        ClubDeportivo copia = persistencia.cargar(ruta);
        assertEquals(premios, copia.getGestorTorneos().consultarPremiosDinero("T"));
    }
}

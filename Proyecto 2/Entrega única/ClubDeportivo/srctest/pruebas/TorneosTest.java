package pruebas;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import excepciones.ClubException;
import logica.ClubDeportivo;
import logica.GestorTorneos;
import modelo.deportes.Deporte;
import modelo.tipos.TipoEstadistica;
import modelo.torneos.EstadoTorneo;
import modelo.torneos.ModalidadTorneo;
import modelo.torneos.TipoTorneo;
import modelo.torneos.Torneo;
import persistencia.PersistenciaJson;

public class TorneosTest
{
    private Deporte tenis;
    private GestorTorneos gestor;

    @BeforeEach
    void preparar() throws ClubException
    {
        tenis = new Deporte(
            "Tenis", false,
            1, 2, 4, 1, 2, 60,
            false, 1,
            Collections.emptyList(),
            TipoEstadistica.INDIVIDUAL
        );

        gestor = new GestorTorneos();
    }

    private Torneo crear(String codigo, TipoTorneo tipo,
                         int cupos, double tarifa)
            throws ClubException
    {
        return new Torneo(
            codigo,
            "Torneo de tenis",
            tenis,
            null,
            "Intermedio",
            tipo,
            ModalidadTorneo.INDIVIDUAL,
            LocalDate.of(2027, 3, 10),
            LocalDate.of(2027, 3, 12),
            cupos,
            tarifa
        );
    }

    @Test
    void registraYConsultaRecreativo() throws ClubException
    {
        Torneo torneo = crear("T01", TipoTorneo.RECREATIVO, 16, 0);
        gestor.registrarTorneo(torneo);

        assertSame(torneo, gestor.buscarTorneo("T01"));
        assertEquals(1, gestor.getTorneos().size());
        assertEquals(0.0, torneo.getTarifaInscripcion(), 0.001);
        assertEquals(
            EstadoTorneo.INSCRIPCIONES_ABIERTAS,
            torneo.getEstado()
        );
    }

    @Test
    void registraCompetitivoConTarifa() throws ClubException
    {
        Torneo torneo = crear(
            "T02", TipoTorneo.COMPETITIVO, 16, 25000);

        gestor.registrarTorneo(torneo);

        assertEquals(
            TipoTorneo.COMPETITIVO,
            gestor.buscarTorneo("T02").getTipo()
        );
        assertEquals(
            25000.0,
            gestor.buscarTorneo("T02").getTarifaInscripcion(),
            0.001
        );
    }

    @Test
    void rechazaCobroEnRecreativo()
    {
        assertThrows(ClubException.class, () ->
            crear("T01", TipoTorneo.RECREATIVO, 16, 10000)
        );
    }

    @Test
    void rechazaCompetitivoSinTarifaPositiva()
    {
        assertThrows(ClubException.class, () ->
            crear("T01", TipoTorneo.COMPETITIVO, 16, 0)
        );

        assertThrows(ClubException.class, () ->
            crear("T02", TipoTorneo.COMPETITIVO, 16, -1000)
        );
    }

    @Test
    void rechazaCuposNoPositivos()
    {
        assertThrows(ClubException.class, () ->
            crear("T01", TipoTorneo.RECREATIVO, 0, 0)
        );

        assertThrows(ClubException.class, () ->
            crear("T02", TipoTorneo.RECREATIVO, -5, 0)
        );
    }

    @Test
    void rechazaCodigoDuplicadoSinModificarLista()
            throws ClubException
    {
        Torneo original = crear(
            "T01", TipoTorneo.RECREATIVO, 16, 0);
        Torneo duplicado = crear(
            "t01", TipoTorneo.COMPETITIVO, 8, 20000);

        gestor.registrarTorneo(original);

        assertThrows(ClubException.class, () ->
            gestor.registrarTorneo(duplicado)
        );

        assertEquals(1, gestor.getTorneos().size());
        assertSame(original, gestor.buscarTorneo("T01"));
    }

    @Test
    void permiteTorneosEnLasMismasFechas() throws ClubException
    {
        Torneo primero = crear(
            "T01", TipoTorneo.RECREATIVO, 16, 0);
        Torneo segundo = crear(
            "T02", TipoTorneo.COMPETITIVO, 8, 20000);

        gestor.registrarTorneo(primero);
        gestor.registrarTorneo(segundo);

        assertEquals(2, gestor.getTorneos().size());
        assertSame(primero, gestor.buscarTorneo("T01"));
        assertSame(segundo, gestor.buscarTorneo("T02"));
    }

    @Test
    void informaCuandoNoExisteElTorneo()
    {
        assertThrows(ClubException.class, () ->
            gestor.buscarTorneo("NO-EXISTE")
        );
    }

    @Test
    void rechazaFechasInvertidas()
    {
        assertThrows(ClubException.class, () ->
            new Torneo(
                "T03", "Torneo con fechas inválidas",
                tenis, null, "Intermedio",
                TipoTorneo.RECREATIVO,
                ModalidadTorneo.INDIVIDUAL,
                LocalDate.of(2027, 3, 12),
                LocalDate.of(2027, 3, 10),
                16, 0
            )
        );
    }

    @Test
    void rechazaEquiposEnDisciplinaIndividual()
    {
        assertThrows(ClubException.class, () ->
            new Torneo(
                "T04", "Torneo con modalidad inválida",
                tenis, null, "Intermedio",
                TipoTorneo.RECREATIVO,
                ModalidadTorneo.EQUIPOS,
                LocalDate.of(2027, 3, 10),
                LocalDate.of(2027, 3, 12),
                16, 0
            )
        );
    }

    @Test
    void reservaVeintePorCientoRedondeadoHaciaArriba()
            throws ClubException
    {
        Torneo torneo = crear("T01", TipoTorneo.RECREATIVO, 16, 0);

        assertEquals(4, torneo.getCuposPreferencialesIniciales());
        assertEquals(4, torneo.getCuposReservadosParaPreferenciales());
        assertEquals(12, torneo.getCuposHabilitadosParaRegulares());
        assertFalse(torneo.isCuposPreferencialesLiberados());
    }

    @Test
    void calculaReservaEnCapacidadesPequenasYMultiplosDeCinco()
            throws ClubException
    {
        Torneo pequeno = crear("T01", TipoTorneo.RECREATIVO, 3, 0);
        Torneo exacto = crear("T02", TipoTorneo.RECREATIVO, 20, 0);

        assertEquals(1, pequeno.getCuposReservadosParaPreferenciales());
        assertEquals(2, pequeno.getCuposHabilitadosParaRegulares());

        assertEquals(4, exacto.getCuposReservadosParaPreferenciales());
        assertEquals(16, exacto.getCuposHabilitadosParaRegulares());
    }

    @Test
    void liberaReservaSinAumentarCapacidadTotal()
            throws ClubException
    {
        Torneo torneo = crear("T01", TipoTorneo.RECREATIVO, 16, 0);
        gestor.registrarTorneo(torneo);

        gestor.liberarCuposPreferenciales("T01");

        assertTrue(torneo.isCuposPreferencialesLiberados());
        assertEquals(0, torneo.getCuposReservadosParaPreferenciales());
        assertEquals(16, torneo.getCuposHabilitadosParaRegulares());
        assertEquals(16, torneo.getCuposTotales());
        assertEquals(4, torneo.getCuposPreferencialesIniciales());
    }

    @Test
    void repetirLiberacionNoDuplicaCupos() throws ClubException
    {
        Torneo torneo = crear("T01", TipoTorneo.RECREATIVO, 16, 0);
        gestor.registrarTorneo(torneo);

        gestor.liberarCuposPreferenciales("T01");
        gestor.liberarCuposPreferenciales("T01");

        assertEquals(16, torneo.getCuposHabilitadosParaRegulares());
        assertEquals(16, torneo.getCuposTotales());
    }

    @Test
    void iniciaYFinalizaSinEliminarElTorneo() throws ClubException
    {
        Torneo torneo = crear("T01", TipoTorneo.RECREATIVO, 16, 0);
        gestor.registrarTorneo(torneo);

        gestor.iniciarTorneo("T01");

        assertEquals(EstadoTorneo.EN_CURSO, torneo.getEstado());

        gestor.finalizarTorneo("T01");

        assertEquals(EstadoTorneo.FINALIZADO, torneo.getEstado());
        assertSame(torneo, gestor.buscarTorneo("T01"));
        assertEquals(1, gestor.getTorneos().size());
    }

    @Test
    void rechazaFinalizacionAntesDelInicio() throws ClubException
    {
        Torneo torneo = crear("T01", TipoTorneo.RECREATIVO, 16, 0);
        gestor.registrarTorneo(torneo);

        assertThrows(ClubException.class, () ->
            gestor.finalizarTorneo("T01")
        );

        assertEquals(
            EstadoTorneo.INSCRIPCIONES_ABIERTAS,
            torneo.getEstado()
        );
    }

    @Test
    void rechazaReinicioDeTorneoFinalizado() throws ClubException
    {
        Torneo torneo = crear("T01", TipoTorneo.RECREATIVO, 16, 0);
        gestor.registrarTorneo(torneo);

        gestor.iniciarTorneo("T01");
        gestor.finalizarTorneo("T01");

        assertThrows(ClubException.class, () ->
            gestor.iniciarTorneo("T01")
        );

        assertEquals(EstadoTorneo.FINALIZADO, torneo.getEstado());
    }

    @Test
    void rechazaLiberacionDeCuposCuandoEstaEnCurso()
            throws ClubException
    {
        Torneo torneo = crear("T01", TipoTorneo.RECREATIVO, 16, 0);
        gestor.registrarTorneo(torneo);
        gestor.iniciarTorneo("T01");

        assertThrows(ClubException.class, () ->
            gestor.liberarCuposPreferenciales("T01")
        );

        assertFalse(torneo.isCuposPreferencialesLiberados());
        assertEquals(4, torneo.getCuposReservadosParaPreferenciales());
    }

    @Test
    void guardaYRecuperaTorneos(@TempDir Path carpeta)
            throws ClubException
    {
        ClubDeportivo club = new ClubDeportivo(
            "Club de prueba",
            "123",
            "Bogotá",
            LocalTime.of(6, 0),
            LocalTime.of(22, 0)
        );

        Torneo torneo = crear(
            "T01", TipoTorneo.COMPETITIVO, 16, 25000);

        club.getGestorTorneos().registrarTorneo(torneo);
        club.getGestorTorneos().liberarCuposPreferenciales("T01");
        club.getGestorTorneos().iniciarTorneo("T01");
        club.getGestorTorneos().finalizarTorneo("T01");

        String ruta = carpeta.resolve("club-prueba.json").toString();
        PersistenciaJson persistencia = new PersistenciaJson();

        persistencia.salvar(club, ruta);

        ClubDeportivo recuperado = persistencia.cargar(ruta);
        Torneo torneoRecuperado =
            recuperado.getGestorTorneos().buscarTorneo("T01");

        assertEquals(1, recuperado.getGestorTorneos().getTorneos().size());
        assertEquals("Torneo de tenis", torneoRecuperado.getNombre());
        assertEquals("Tenis", torneoRecuperado.getDeporte().getNombre());
        assertEquals(TipoTorneo.COMPETITIVO, torneoRecuperado.getTipo());
        assertEquals(EstadoTorneo.FINALIZADO, torneoRecuperado.getEstado());
        assertEquals(16, torneoRecuperado.getCuposTotales());
        assertEquals(
            25000.0,
            torneoRecuperado.getTarifaInscripcion(),
            0.001
        );
        assertTrue(torneoRecuperado.isCuposPreferencialesLiberados());
    }

    @Test
    void rechazaCodigoVacio()
    {
        assertThrows(ClubException.class, () ->
            crear("   ", TipoTorneo.RECREATIVO, 16, 0)
        );
    }

    @Test
    void rechazaTarifasNoFinitas()
    {
        assertThrows(ClubException.class, () ->
            crear("T01", TipoTorneo.COMPETITIVO, 16, Double.NaN)
        );

        assertThrows(ClubException.class, () ->
            crear(
                "T02", TipoTorneo.COMPETITIVO,
                16, Double.POSITIVE_INFINITY)
        );
    }

    @Test
    void rechazaRegistroNuloSinModificarColeccion()
    {
        assertThrows(ClubException.class, () ->
            gestor.registrarTorneo(null)
        );

        assertTrue(gestor.getTorneos().isEmpty());
    }

    @Test
    void listaDevueltaNoPermiteBorrarTorneosDelGestor()
            throws ClubException
    {
        Torneo torneo = crear("T01", TipoTorneo.RECREATIVO, 16, 0);
        gestor.registrarTorneo(torneo);

        gestor.getTorneos().clear();

        assertEquals(1, gestor.getTorneos().size());
        assertSame(torneo, gestor.buscarTorneo("T01"));
    }

    @Test
    void buscaCodigoSinDistinguirMayusculasNiEspaciosExternos()
            throws ClubException
    {
        Torneo torneo = crear(" T01 ", TipoTorneo.RECREATIVO, 16, 0);
        gestor.registrarTorneo(torneo);

        assertSame(torneo, gestor.buscarTorneo(" t01 "));
    }

    @Test
    void rechazaSegundaFinalizacionSinCambiarEstado()
            throws ClubException
    {
        Torneo torneo = crear("T01", TipoTorneo.RECREATIVO, 16, 0);
        gestor.registrarTorneo(torneo);
        gestor.iniciarTorneo("T01");
        gestor.finalizarTorneo("T01");

        assertThrows(ClubException.class, () ->
            gestor.finalizarTorneo("T01")
        );

        assertEquals(EstadoTorneo.FINALIZADO, torneo.getEstado());
    }
}

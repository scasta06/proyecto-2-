package pruebas;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import excepciones.ClubException;
import modelo.comercio.BonoTorneo;
import modelo.comercio.Tienda;
import modelo.comercio.Venta;
import modelo.inventario.Ubicacion;
import modelo.personas.Socio;
import modelo.tipos.TipoUbicacion;

public class BonoTorneoTest
{
    private Socio socio(String id) throws ClubException
    {
        return new Socio(id, id, LocalDate.of(2000,1,1), id, "clave", id);
    }

    private Venta venta(Socio socio) throws ClubException
    {
        return new Venta("V1", socio, new Tienda(new Ubicacion("T", "Tienda", TipoUbicacion.TIENDA)));
    }

    @Test
    void rechazaPorcentajeInvalido() throws ClubException
    {
        Socio s = socio("1");
        assertThrows(ClubException.class, () -> new BonoTorneo("B", s, Double.NaN));
        assertThrows(ClubException.class, () -> new BonoTorneo("B", s, 101));
        assertThrows(ClubException.class, () -> new BonoTorneo("B", s, 0));
    }

    @Test
    void rechazaBonoAjenoSinCambiarVenta() throws ClubException
    {
        BonoTorneo bono = new BonoTorneo("B", socio("1"), 10);
        Venta venta = venta(socio("2"));
        assertThrows(ClubException.class, () -> venta.aplicarBono(bono));
        assertNull(venta.getBono());
        assertFalse(bono.isUsado());
    }

    @Test
    void noAcumulaCodigoNiPuntos() throws ClubException
    {
        Socio s = socio("1");
        Venta venta = venta(s);
        venta.aplicarBono(new BonoTorneo("B", s, 10));
        assertThrows(ClubException.class, () -> venta.aplicarCodigoCompartido());
        assertThrows(ClubException.class, () -> venta.redimirPuntos(1, 1));
    }

    @Test
    void ventaAbiertaNoConsumeBono() throws ClubException
    {
        Socio s = socio("1");
        BonoTorneo bono = new BonoTorneo("B", s, 10);
        Venta venta = venta(s);
        venta.aplicarBono(bono);
        assertThrows(ClubException.class, () -> bono.consumir(venta));
        assertFalse(bono.isUsado());
    }

    @Test
    void compraConsumeBonoUnaVezYDescuentaStock() throws ClubException
    {
        logica.ClubDeportivo club = DatosPrueba.crear();
        logica.GestorComercio comercio = club.getGestorComercio();
        Socio socio = club.getGestorUsuarios().buscarSocio("S1");
        modelo.comercio.Producto producto = comercio.getProductos().get(0);
        BonoTorneo bono = new BonoTorneo("B", socio, 10);
        Venta venta = comercio.iniciarVenta(comercio.getTienda(), socio);
        comercio.agregarProducto(venta, producto, 1);
        venta.aplicarBono(bono);
        int stock = comercio.getInventario().getCantidad(producto, comercio.getTienda().getUbicacion());
        comercio.finalizarVenta(venta, 0, 0);
        assertEquals(107100, venta.getTotalPagado());
        assertTrue(bono.isUsado());
        assertEquals(stock - 1, comercio.getInventario().getCantidad(producto, comercio.getTienda().getUbicacion()));
        Venta otra = comercio.iniciarVenta(comercio.getTienda(), socio);
        assertThrows(ClubException.class, () -> otra.aplicarBono(bono));
    }

    @Test
    void fallaDeStockNoConsumeBono() throws ClubException
    {
        logica.ClubDeportivo club = DatosPrueba.crear();
        logica.GestorComercio comercio = club.getGestorComercio();
        Socio socio = club.getGestorUsuarios().buscarSocio("S1");
        modelo.comercio.Producto producto = comercio.getProductos().get(0);
        Venta venta = comercio.iniciarVenta(comercio.getTienda(), socio);
        comercio.agregarProducto(venta, producto, 1);
        BonoTorneo bono = new BonoTorneo("B", socio, 10);
        venta.aplicarBono(bono);
        comercio.getInventario().descontar(producto, comercio.getTienda().getUbicacion(), 10);
        assertThrows(ClubException.class, () -> comercio.finalizarVenta(venta, 0, 0));
        assertFalse(venta.isCerrada());
        assertFalse(bono.isUsado());
        assertEquals(0, socio.getCuenta().getPuntos());
    }

    @Test
    void fidelidadExcluyePropinaYEmpleadoDescuentaEnCafe() throws ClubException
    {
        logica.ClubDeportivo club = DatosPrueba.crear();
        logica.GestorComercio comercio = club.getGestorComercio();
        modelo.personas.Entrenador empleado = club.getGestorUsuarios().getEntrenadores().get(0);
        modelo.comercio.Producto agua = null;
        for (modelo.comercio.Producto producto : comercio.getProductos())
            if (producto.getCodigo().equals("AGU")) agua = producto;
        Venta venta = comercio.iniciarVenta(comercio.getCafeteria(), empleado);
        comercio.agregarProducto(venta, agua, 1);
        comercio.finalizarVenta(venta, 1000, 0);
        assertEquals(300, venta.getDescuento());
        assertEquals(1836, venta.getBaseFidelizacion());
        assertEquals(36, venta.getPuntosGanados());
        assertEquals(2836, venta.getTotalPagado());
    }
}

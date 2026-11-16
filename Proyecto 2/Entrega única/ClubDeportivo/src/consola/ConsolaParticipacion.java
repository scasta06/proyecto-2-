package consola;

import java.util.ArrayList;
import java.util.List;
import excepciones.ClubException;
import logica.ClubDeportivo;
import modelo.personas.Persona;
import modelo.personas.Socio;
import modelo.torneos.Torneo;
import modelo.torneos.ModalidadTorneo;
import modelo.deportes.Equipo;
import modelo.comercio.BonoTorneo;
import modelo.comercio.Producto;
import modelo.comercio.Venta;
import modelo.comercio.PuntoVenta;

public class ConsolaParticipacion extends ConsolaBase
{
    private Persona persona;

    public ConsolaParticipacion(ClubDeportivo club, Persona persona)
    {
        super(club);
        this.persona = persona;
    }

    public void mostrarMenu()
    {
        while (true)
        {
            String opcion = pedirCadena("1. Torneos / 2. Inscribirse / 3. Retirarse / 4. Bonos y premios / 5. Comprar / 0. Volver: ");
            if (opcion.isEmpty() || opcion.equals("0")) return;
            try
            {
                if (opcion.equals("1")) VistaHistorialTorneo.mostrar(elegirTorneo());
                else if (opcion.equals("2")) inscribir();
                else if (opcion.equals("3"))
                {
                    Torneo torneo = elegirTorneo();
                    torneo.getHistorial().retirarInscripcion(pedirCadena("Código de inscripción: "), persona);
                    System.out.println("Retiro confirmado; cupo liberado.");
                }
                else if (opcion.equals("4"))
                {
                    for (BonoTorneo bono : club.getGestorTorneos().consultarBonos(persona))
                        System.out.println(bono.getCodigo() + " | " + bono.getPorcentaje() + "% | usado: " + bono.isUsado());
                    for (Torneo torneo : club.getGestorTorneos().getTorneos())
                    {
                        Double premio = club.getGestorTorneos().consultarPremiosDinero(torneo.getCodigo()).get(persona.getDocumento());
                        if (premio != null) System.out.println(torneo.getNombre() + " | premio: " + premio);
                    }
                }
                else if (opcion.equals("5")) comprar();
                else System.out.println("Opción inválida");
            }
            catch (ClubException | IllegalArgumentException e)
            {
                System.out.println("No se completó la operación: " + e.getMessage());
            }
        }
    }

    private Torneo elegirTorneo() throws ClubException
    {
        for (Torneo torneo : club.getGestorTorneos().getTorneos())
            System.out.println(torneo.getCodigo() + " | " + torneo.getNombre() + " | " + torneo.getEstado()
                    + " | inscritos: " + torneo.getHistorial().getInscripciones().size() + "/" + torneo.getCuposTotales());
        return club.getGestorTorneos().buscarTorneo(pedirCadena("Código del torneo: "));
    }

    private void inscribir() throws ClubException
    {
        Torneo torneo = elegirTorneo();
        List<Persona> participantes = new ArrayList<Persona>();
        participantes.add(persona);
        Equipo equipo = null;
        if (torneo.getModalidad() == ModalidadTorneo.EQUIPOS)
        {
            for (Equipo candidato : club.getGestorDeportivo().getEquipos())
                if (candidato.getJugadoresActivos().contains(persona)) System.out.println(candidato.getNombre());
            String nombre = pedirCadena("Nombre del equipo: ");
            for (Equipo candidato : club.getGestorDeportivo().getEquipos())
                if (candidato.getNombre().equalsIgnoreCase(nombre) && candidato.getJugadoresActivos().contains(persona)) equipo = candidato;
            if (equipo == null)
            {
                if (!(persona instanceof modelo.personas.ParticipanteExterno)) throw new ClubException("Debe pertenecer al equipo");
                for (String documento : pedirCadena("Documentos de los demás integrantes separados por coma: ").split(","))
                    participantes.add(club.getGestorUsuarios().buscarPersona(documento.trim()));
            }
            else
            {
                participantes.clear();
                participantes.addAll(equipo.getJugadoresActivos());
            }
        }
        else if (torneo.getModalidad() == ModalidadTorneo.DOBLES)
            participantes.add(club.getGestorUsuarios().buscarPersona(pedirCadena("Documento de la pareja: ")));
        String codigo = pedirCadena("Código único de inscripción: ");
        String nombre = pedirCadena("Nombre de la participación: ");
        System.out.println("Tarifa por inscripción: " + (persona instanceof modelo.personas.Empleado ? 0 : torneo.getTarifaInscripcion()));
        if (!pedirCadena("Confirmar inscripción y pago (SI): ").equalsIgnoreCase("SI")) return;
        club.getGestorTorneos().inscribir(club, torneo.getCodigo(), codigo, nombre, participantes, equipo);
        System.out.println("Inscripción confirmada.");
    }

    private void comprar() throws ClubException
    {
        String local = pedirCadena("1. Tienda / 2. Cafetería: ");
        if (!local.equals("1") && !local.equals("2")) throw new ClubException("Punto de venta inválido");
        PuntoVenta punto = local.equals("1") ? club.getGestorComercio().getTienda() : club.getGestorComercio().getCafeteria();
        Venta venta = club.getGestorComercio().iniciarVenta(punto, persona);
        for (Producto producto : club.getGestorComercio().getProductos())
            if (punto.admiteProducto(producto)) System.out.println(producto.getCodigo() + " | " + producto.getNombre()
                    + " | precio: " + producto.getPrecio() + " | stock: "
                    + club.getGestorComercio().getInventario().getCantidad(producto, punto.getUbicacion()));
        while (true)
        {
            String codigo = pedirCadena("Producto (0 para terminar): ");
            if (codigo.equals("0") || codigo.isEmpty()) break;
            Producto elegido = null;
            for (Producto producto : club.getGestorComercio().getProductos())
                if (producto.getCodigo().equals(codigo)) elegido = producto;
            if (elegido == null) throw new ClubException("Producto desconocido");
            if (elegido instanceof modelo.comercio.Snack)
                for (modelo.comercio.Ingrediente ingrediente : club.getGestorComercio().consultarIngredientes((modelo.comercio.Snack)elegido))
                {
                    System.out.println(ingrediente.getNombre());
                    for (modelo.comercio.Alergeno alergeno : ingrediente.getAlergenos())
                        System.out.println("Alérgeno: " + alergeno.getNombre() + " | " + alergeno.getDescripcion());
                }
            club.getGestorComercio().agregarProducto(venta, elegido, pedirEntero("Cantidad: "));
        }
        if (venta.requiereInfoAlergenos())
        {
            if (!pedirCadena("¿Confirma haber consultado ingredientes y alérgenos? (SI): ").equalsIgnoreCase("SI")) return;
            venta.marcarAlergenosInformados();
        }
        String bonoCodigo = pedirCadena("Código de bono (vacío para ninguno): ");
        if (!bonoCodigo.isEmpty())
        {
            BonoTorneo elegido = null;
            for (BonoTorneo bono : club.getGestorTorneos().consultarBonos(persona))
                if (bono.getCodigo().equals(bonoCodigo)) elegido = bono;
            if (elegido == null) throw new ClubException("Bono desconocido");
            venta.aplicarBono(elegido);
        }
        else
        {
            String codigo = pedirCadena("Código de descuento (vacío para ninguno): ");
            if (!codigo.isEmpty()) club.getGestorComercio().aplicarCodigoDescuento(venta, codigo);
        }
        venta.calcularTotales(punto);
        System.out.println("Subtotal: " + venta.getSubtotal() + " | descuento: " + venta.getDescuento() + " | impuesto: " + venta.getImpuesto());
        double propina = punto.admitePropina() ? Double.parseDouble(pedirCadena("Propina voluntaria en pesos (0 para ninguna): ")) : 0;
        int puntos = bonoCodigo.isEmpty() ? pedirEntero("Puntos a redimir (0 para ninguno): ") : 0;
        if (!pedirCadena("Confirmar compra (SI): ").equalsIgnoreCase("SI")) return;
        club.getGestorComercio().finalizarVenta(venta, propina, puntos);
        System.out.println("Compra " + venta.getNumero() + " | total: " + venta.getTotalPagado() + " | puntos ganados: " + venta.getPuntosGanados());
    }
}

package modelo.comercio;

import excepciones.ClubException;
import excepciones.VentaException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import modelo.personas.Persona;
import modelo.personas.Socio;

public class Venta implements Serializable
{
    private static final long serialVersionUID = 1L;
    private BonoTorneo bono;
    private String numero;
    private LocalDateTime fechaHora=LocalDateTime.now();
    private double subtotal;
    private double descuento;
    private double impuesto;
    private double propina;
    private double valorRedimido;
    private double totalPagado;
    private int puntosRedimidos;
    private int puntosGanados;
    private boolean alergenosInformados;
    private boolean cerrada;
    private boolean codigoCompartido;
    private double porcentajeCodigo;
    private Persona comprador;
    private PuntoVenta puntoVenta;
    private List<ItemVenta> items=new ArrayList<>();
    public Venta(String n,Persona c,PuntoVenta pv)
    {
        numero=n;
        comprador=c;
        puntoVenta=pv;
    }
    private void abierta() throws ClubException
    {
        validar(!cerrada,"Venta cerrada");
    }
    public void agregarItem(Producto p,int cantidad) throws ClubException
    {
        abierta();
        validar(puntoVenta.admiteProducto(p),"Producto no admitido");
        items.add(new ItemVenta(p,cantidad));
        alergenosInformados=false;
    }
    public boolean requiereInfoAlergenos()
    {
        for (ItemVenta item : items)
        {
            if (item.getProducto() instanceof Snack)
            {
                return true;
            }
        }
        return false;
    }
    public void marcarAlergenosInformados() throws ClubException
    {
        abierta();
        alergenosInformados=true;
    }
    public void calcularTotales(PuntoVenta pv) throws ClubException
    {
        abierta();
        validar(pv==puntoVenta,"Punto de venta incorrecto");
        double suma = 0;
        for (ItemVenta item : items)
        {
            suma += item.getSubtotal();
        }
        subtotal = redondearDinero(suma);
        descuento=pv.calcularDescuento(comprador,subtotal);
        if (codigoCompartido)
            descuento = redondearDinero(subtotal * (porcentajeCodigo == 0 ? 0.08 : porcentajeCodigo));
        if (bono != null)
        {
            bono.validarUso(comprador);
            descuento = redondearDinero(subtotal * bono.getPorcentaje() / 100.0);
        }
        impuesto=pv.calcularImpuesto(subtotal-descuento);
        totalPagado=redondearDinero(subtotal-descuento+impuesto+propina-valorRedimido);
    }
    public void aplicarPropina(double v) throws ClubException
    {
        abierta();
        validar(puntoVenta.admitePropina()||v==0,"La tienda no admite propina");
        propina=redondearDinero(v);
    }
    public void redimirPuntos(int p,double valor) throws ClubException
    {
        abierta();
        validar(bono == null || p == 0, "El bono no se acumula con puntos");
        validar(p>=0&&valor==(double)p*CuentaFidelizacion.VALOR_PUNTO&&valor<=subtotal-descuento+impuesto,"Redención inválida");
        puntosRedimidos=p;
        valorRedimido=valor;
    }
    public double getBaseFidelizacion()
    {
        return totalPagado - propina;
    }
    public void cerrar() throws ClubException
    {
        abierta();
        validar(!items.isEmpty()&&(!requiereInfoAlergenos()||alergenosInformados),"Venta vacía o alérgenos no informados");
        cerrada=true;
    }
    public boolean isAlergenosInformados()
    {
        return alergenosInformados;
    }
    public boolean isCerrada()
    {
        return cerrada;
    }
    public Persona getComprador()
    {
        return comprador;
    }
    public PuntoVenta getPuntoVenta()
    {
        return puntoVenta;
    }
    public List<ItemVenta> getItems()
    {
        return copiaLectura(items);
    }
    public LocalDateTime getFechaHora()
    {
        return fechaHora;
    }
    public double getTotalPagado()
    {
        return totalPagado;
    }
    public double getSubtotal()
    {
        return subtotal;
    }
    public double getDescuento()
    {
        return descuento;
    }
    public double getImpuesto()
    {
        return impuesto;
    }
    public double getPropina()
    {
        return propina;
    }
    public int getPuntosGanados()
    {
        return puntosGanados;
    }
    public void setPuntosGanados(int n)
    {
        puntosGanados=n;
    }
    public String getNumero()
    {
        return numero;
    }
    private static void validar(boolean condicion,String mensaje) throws ClubException
    {
        if(!condicion)throw new VentaException(mensaje);
    }
    private static double redondearDinero(double valor) throws ClubException
    {
        if(!Double.isFinite(valor)||valor<0)throw new ClubException("Valor monetario inválido");
        return Math.round(valor*100.0)/100.0;
    }
    private static <T> List<T> copiaLectura(Collection<T> valores)
    {
        return new ArrayList<T>(valores);
    }
    public void aplicarCodigoCompartido() throws ClubException
    {
        abierta();
        validar(comprador instanceof Socio || comprador instanceof modelo.personas.Empleado, "Comprador no habilitado");
        validar(bono == null, "El bono no se acumula con códigos");
        codigoCompartido = true;
    }
    public void aplicarCodigoEmpleado(modelo.personas.Empleado titular) throws ClubException
    {
        aplicarCodigoCompartido();
        porcentajeCodigo = titular == comprador ? 0.15 : 0.08;
    }
    public int getPuntosRedimidos() { return puntosRedimidos; }
    public double getValorRedimido() { return valorRedimido; }

    public BonoTorneo getBono() { return bono; }

    public void aplicarBono(BonoTorneo bono) throws ClubException
    {
        abierta();
        validar(bono != null && this.bono == null && !codigoCompartido
            && puntosRedimidos == 0, "El bono no se acumula con otros descuentos");
        bono.validarUso(comprador);
        this.bono = bono;
    }
}

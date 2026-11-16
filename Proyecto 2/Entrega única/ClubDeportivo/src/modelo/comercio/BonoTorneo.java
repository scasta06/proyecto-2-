package modelo.comercio;

import java.io.Serializable;
import excepciones.ClubException;
import modelo.personas.Persona;

public class BonoTorneo implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String codigo;
    private Persona propietario;
    private double porcentaje;
    private String venta;

    public BonoTorneo(String codigo, Persona propietario, double porcentaje) throws ClubException
    {
        if (codigo == null || codigo.trim().isEmpty() || propietario == null
                || !Double.isFinite(porcentaje) || porcentaje <= 0 || porcentaje > 100)
            throw new ClubException("Datos de bono inválidos");
        this.codigo = codigo;
        this.propietario = propietario;
        this.porcentaje = porcentaje;
    }

    public String getCodigo() { return codigo; }
    public Persona getPropietario() { return propietario; }
    public double getPorcentaje() { return porcentaje; }
    public boolean isUsado() { return venta != null; }
    public String getVenta() { return venta; }

    public void validarUso(Persona comprador) throws ClubException
    {
        if (comprador != propietario || isUsado())
            throw new ClubException("Bono ajeno o ya utilizado");
    }

    public void consumir(Venta compra) throws ClubException
    {
        validarUso(compra.getComprador());
        if (!compra.isCerrada() || compra.getBono() != this)
            throw new ClubException("El bono requiere una compra confirmada");
        venta = compra.getNumero();
    }
}

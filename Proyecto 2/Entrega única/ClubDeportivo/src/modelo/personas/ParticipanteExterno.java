package modelo.personas;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import excepciones.ClubException;
import modelo.deportes.Deporte;
import modelo.comercio.CuentaFidelizacion;

public class ParticipanteExterno extends Persona
{
    private static final long serialVersionUID = 1L;
    private boolean universitario;
    private Map<Deporte, Integer> niveles = new HashMap<Deporte, Integer>();
    private CuentaFidelizacion cuenta = new CuentaFidelizacion();

    public ParticipanteExterno(String documento, String nombre, LocalDate nacimiento,
            String login, String clave, boolean universitario) throws ClubException
    {
        super(documento, nombre, nacimiento, login, clave);
        this.universitario = universitario;
    }

    public String getTipo() { return universitario ? "UNIVERSITARIO" : "EXTERNO"; }
    public CuentaFidelizacion getCuenta() { return cuenta; }

    public void asignarNivel(Deporte deporte, int nivel) throws ClubException
    {
        if (deporte == null || deporte.isEsDeConjunto() || nivel < 1 || nivel > 3)
            throw new ClubException("Disciplina o nivel inválidos");
        niveles.put(deporte, nivel);
    }

    public int getNivel(Deporte deporte) throws ClubException
    {
        if (!niveles.containsKey(deporte)) throw new ClubException("Debe registrar el nivel del participante");
        return niveles.get(deporte);
    }
}

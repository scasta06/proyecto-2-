package excepciones;


public class ClubException extends Exception
{
    private static final long serialVersionUID=1L;
    public ClubException(String msg)
    {
        super(msg);
    }
    public ClubException(String mensaje, Throwable causa)
    {
        super(mensaje, causa);
    }
}


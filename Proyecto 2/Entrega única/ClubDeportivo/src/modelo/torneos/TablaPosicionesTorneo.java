package modelo.torneos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import excepciones.ClubException;
import modelo.tipos.TipoEstadistica;

public class TablaPosicionesTorneo
{
    public static List<Fila> calcular(Torneo torneo)
            throws ClubException
    {
        HistorialTorneo historial = torneo.getHistorial();
        Map<String, Fila> filas = new LinkedHashMap<String, Fila>();

        for (InscripcionTorneo inscripcion : historial.getInscripciones())
        {
            filas.put(
                inscripcion.getCodigo(),
                new Fila(
                    inscripcion.getCodigo(),
                    inscripcion.getNombreParticipacion())
            );
        }

        boolean baloncesto =
            torneo.getDeporte().getTipoEstadistica()
                == TipoEstadistica.BALONCESTO;

        int puntosVictoria = 3;
        int puntosDerrota = 0;

        for (EncuentroTorneo encuentro : historial.getEncuentros())
        {
            if (encuentro.getEstado() != EstadoEncuentro.FINALIZADO)
            {
                continue;
            }

            String codigoA = historial.buscarInscripcion(
                encuentro.getCodigoInscripcionA()).getCodigo();

            String codigoB = historial.buscarInscripcion(
                encuentro.getCodigoInscripcionB()).getCodigo();

            Fila a = filas.get(codigoA);
            Fila b = filas.get(codigoB);

            a.jugados++;
            b.jugados++;

            if (encuentro.isEmpate())
            {
                if (!torneo.getDeporte().isPermiteEmpate() || baloncesto)
                {
                    throw new ClubException(
                        "El historial contiene un empate no permitido");
                }

                a.empatados++;
                b.empatados++;
                a.puntos++;
                b.puntos++;
            }
            else
            {
                boolean ganoA = codigoA.equalsIgnoreCase(
                    encuentro.getCodigoInscripcionGanadora());

                Fila ganador = ganoA ? a : b;
                Fila perdedor = ganoA ? b : a;

                ganador.ganados++;
                ganador.puntos += puntosVictoria;

                perdedor.perdidos++;
                perdedor.puntos += puntosDerrota;
            }
        }

        List<Fila> tabla = new ArrayList<Fila>(filas.values());

        tabla.sort(
            Comparator.comparingInt(Fila::getPuntos)
                .reversed()
                .thenComparing(Fila::getCodigo)
        );

        int posicion = 0;

        for (int i = 0; i < tabla.size(); i++)
        {
            if (i == 0
                    || tabla.get(i).getPuntos()
                        != tabla.get(i - 1).getPuntos())
            {
                posicion = i + 1;
            }

            tabla.get(i).posicion = posicion;
        }

        return tabla;
    }

    public static class Fila
    {
        private String codigo;
        private String nombre;
        private int posicion;
        private int jugados;
        private int ganados;
        private int empatados;
        private int perdidos;
        private int puntos;

        private Fila(String codigo, String nombre)
        {
            this.codigo = codigo;
            this.nombre = nombre;
        }

        public String getCodigo()
        {
            return codigo;
        }

        public String getNombre()
        {
            return nombre;
        }

        public int getPosicion()
        {
            return posicion;
        }

        public int getJugados()
        {
            return jugados;
        }

        public int getGanados()
        {
            return ganados;
        }

        public int getEmpatados()
        {
            return empatados;
        }

        public int getPerdidos()
        {
            return perdidos;
        }

        public int getPuntos()
        {
            return puntos;
        }
    }
}

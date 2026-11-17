package ec.edu.epn.eda2.juez.core.ejecucion;

import java.util.Arrays;

//Resultado de medir una tarea: valor devuelto y tiempos, o el motivo
//por el que no se pudo medir (excepcion o tiempo agotado).
public final class Medicion<T> {

    private final T resultado;
    private final long[] tiemposNs;
    private final long[] memoriaBytes;
    private final Throwable error;
    private final boolean agotoTiempo;

    //Uso interno: se crea con las fabricas exito, fallo y tiempoAgotado.
    private Medicion(
            T resultado,
            long[] tiemposNs,
            long[] memoriaBytes,
            Throwable error,
            boolean agotoTiempo) {

        this.resultado = resultado;
        this.tiemposNs = tiemposNs;
        this.memoriaBytes = memoriaBytes;
        this.error = error;
        this.agotoTiempo = agotoTiempo;
    }

    //Todas las repeticiones terminaron bien.
    static <T> Medicion<T> exito(
            T resultado, long[] tiemposNs, long[] memoriaBytes) {
        return new Medicion<>(resultado, tiemposNs.clone(),
            memoriaBytes.clone(), null, false);
    }

    //La tarea lanzo una excepcion o un error.
    static <T> Medicion<T> fallo(Throwable error) {
        return new Medicion<>(null, new long[0], new long[0], error, false);
    }

    //La tarea supero el limite de tiempo.
    static <T> Medicion<T> tiempoAgotado() {
        return new Medicion<>(null, new long[0], new long[0], null, true);
    }

    //True si todas las repeticiones terminaron bien.
    public boolean exitosa() {
        return error == null && !agotoTiempo;
    }

    //Valor devuelto por la primera repeticion medida.
    public T resultado() {
        return resultado;
    }

    //Excepcion lanzada por la tarea (null si no hubo).
    public Throwable error() {
        return error;
    }

    //True si se supero el limite de tiempo.
    public boolean agotoTiempo() {
        return agotoTiempo;
    }

    //Tiempo de cada repeticion medida, en nanosegundos.
    public long[] tiemposNs() {
        return tiemposNs.clone();
    }

    //Bytes asignados en cada repeticion (-1 si no se pudo medir).
    public long[] memoriaBytes() {
        return memoriaBytes.clone();
    }

    //Mediana de los bytes asignados por ejecucion (-1 si no hay dato).
    public long memoriaMedianaBytes() {

        if (memoriaBytes.length == 0) {
            return -1;
        }

        long[] ordenados = memoriaBytes.clone();
        Arrays.sort(ordenados);

        return ordenados[(ordenados.length - 1) / 2];
    }

    //Menor tiempo medido (-1 si no hubo mediciones).
    public long minimoNs() {

        if (tiemposNs.length == 0) {
            return -1;
        }

        return Arrays.stream(tiemposNs).min().getAsLong();
    }

    //Mediana de los tiempos medidos (-1 si no hubo mediciones).
    public long medianaNs() {

        if (tiemposNs.length == 0) {
            return -1;
        }

        long[] ordenados = tiemposNs.clone();
        Arrays.sort(ordenados);

        return ordenados[(ordenados.length - 1) / 2];
    }

    //Promedio de los tiempos medidos (-1 si no hubo mediciones).
    public long promedioNs() {

        if (tiemposNs.length == 0) {
            return -1;
        }

        return (long) Arrays.stream(tiemposNs).average().getAsDouble();
    }
}

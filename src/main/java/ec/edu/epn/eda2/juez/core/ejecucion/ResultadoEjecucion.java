package ec.edu.epn.eda2.juez.core.ejecucion;

//Una fila del reporte: un algoritmo evaluado sobre un caso.
//
//algoritmo es null cuando el error afecta al caso completo
//(por ejemplo, un TXT mal formado). Los tiempos son -1 si no hubo
//medicion; medianaNs es el tiempo que se reporta.
//memoriaBytes: bytes asignados por la solucion en una ejecucion
//(mediana de las repeticiones); -1 si no se pudo medir.
public record ResultadoEjecucion(
        String caso,
        int vertices,
        int aristas,
        Algoritmo algoritmo,
        EstadoEjecucion estado,
        long medianaNs,
        long minimoNs,
        String detalle,
        long memoriaBytes) {

    //Fila sin medicion de memoria (-1 = no medida).
    public ResultadoEjecucion(
            String caso, int vertices, int aristas, Algoritmo algoritmo,
            EstadoEjecucion estado, long medianaNs, long minimoNs,
            String detalle) {

        this(caso, vertices, aristas, algoritmo, estado,
            medianaNs, minimoNs, detalle, -1);
    }
}

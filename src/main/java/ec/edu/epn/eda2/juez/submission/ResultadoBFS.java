package ec.edu.epn.eda2.juez.submission;

import java.util.Objects;

//Resultado de BFS: orden de visita y distancias desde el inicio.
//Distancia -1 indica un vertice inalcanzable.
public record ResultadoBFS(int[] orden, int[] distancias) {

    //Rechaza nulos y copia los arreglos para que no cambien despues.
    public ResultadoBFS {
        Objects.requireNonNull(orden, "orden no puede ser null.");
        Objects.requireNonNull(
            distancias, "distancias no puede ser null."
        );

        orden = orden.clone();
        distancias = distancias.clone();
    }
}

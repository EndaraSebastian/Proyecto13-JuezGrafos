package ec.edu.epn.eda2.juez.submission;

import ec.edu.epn.eda2.juez.core.modelo.Arista;

import java.util.List;
import java.util.Objects;

//Resultado del MST: aristas elegidas (arbol o bosque minimo).
//Cada arista puede escribirse en cualquier sentido (a-b o b-a).
public record ResultadoMST(List<Arista> aristas) {

    //Rechaza nulos y copia la lista para que no cambie despues.
    public ResultadoMST {
        Objects.requireNonNull(aristas, "aristas no puede ser null.");
        aristas = List.copyOf(aristas);
    }

    //Suma de los pesos de las aristas entregadas por la solucion.
    //El validador debe comprobar ademas que existan en el grafo.
    public long pesoTotal() {

        long total = 0;

        for (Arista arista : aristas) {
            total += arista.peso();
        }

        return total;
    }
}

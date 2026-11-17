package ec.edu.epn.eda2.juez.submission;

import java.util.Objects;

//Resultado del orden topologico: orden valido o aviso de ciclo.
//Use las fabricas aciclico(...) y conCiclo().
public record ResultadoTopologico(boolean hayCiclo, int[] orden) {

    //Rechaza nulos y copia el arreglo para que no cambie despues.
    public ResultadoTopologico {
        Objects.requireNonNull(orden, "orden no puede ser null.");
        orden = orden.clone();
    }

    //El grafo es aciclico y este es un orden topologico suyo.
    public static ResultadoTopologico aciclico(int[] orden) {
        return new ResultadoTopologico(false, orden);
    }

    //El grafo tiene al menos un ciclo: no existe orden topologico.
    public static ResultadoTopologico conCiclo() {
        return new ResultadoTopologico(true, new int[0]);
    }
}

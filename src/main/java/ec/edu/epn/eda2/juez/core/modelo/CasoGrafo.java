package ec.edu.epn.eda2.juez.core.modelo;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

//Representa un grafo utilizado como caso de prueba.
public final class CasoGrafo {

    private final String nombre;
    private final TipoGrafo tipo;
    private final int vertices;
    private final List<Arista> aristas;

    //Inicializa el grafo y valida sus datos.
    public CasoGrafo(String nombre, TipoGrafo tipo,
                     int vertices, List<Arista> aristas) {

        if (nombre == null ||
            !nombre.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException(
                "Nombre de caso no valido."
            );
        }

        this.tipo = Objects.requireNonNull(tipo);

        if (vertices < 1) {
            throw new IllegalArgumentException(
                "Debe existir al menos un vertice."
            );
        }

        Objects.requireNonNull(aristas);
        Set<String> conexiones = new HashSet<>();

        //Comprueba que las aristas sean validas.
        for (Arista arista : aristas) {
            Objects.requireNonNull(arista);

            int a = arista.origen();
            int b = arista.destino();

            //Verifica que los vertices existan.
            if (a >= vertices || b >= vertices) {
                throw new IllegalArgumentException(
                    "Vertice fuera de rango."
                );
            }

            //Impide bucles propios.
            if (a == b) {
                throw new IllegalArgumentException(
                    "No se admiten bucles propios."
                );
            }

            //Normaliza conexiones no dirigidas.
            if (tipo == TipoGrafo.NO_DIRIGIDO && a > b) {
                int auxiliar = a;
                a = b;
                b = auxiliar;
            }

            //Detecta aristas duplicadas.
            if (!conexiones.add(a + ":" + b)) {
                throw new IllegalArgumentException(
                    "Arista duplicada."
                );
            }
        }

        this.nombre = nombre;
        this.vertices = vertices;
        this.aristas = List.copyOf(aristas);
    }

    //Devuelve el nombre del caso.
    public String nombre() {
        return nombre;
    }

    //Devuelve el tipo de grafo.
    public TipoGrafo tipo() {
        return tipo;
    }

    //Devuelve la cantidad de vertices.
    public int vertices() {
        return vertices;
    }

    //Devuelve las aristas del grafo.
    public List<Arista> aristas() {
        return aristas;
    }
}
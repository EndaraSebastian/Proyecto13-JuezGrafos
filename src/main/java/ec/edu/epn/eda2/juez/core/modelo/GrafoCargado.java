package ec.edu.epn.eda2.juez.core.modelo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

//Grafo ya cargado en memoria, listo para entregarse a las soluciones.
//Envuelve al CasoGrafo del modelo compartido y agrega listas de
//adyacencia inmutables ordenadas por vertice destino ascendente.
public final class GrafoCargado {

    private final CasoGrafo caso;
    private final List<List<Vecino>> adyacencia;

    //Construye las listas de adyacencia a partir del caso.
    public GrafoCargado(CasoGrafo caso) {

        this.caso = Objects.requireNonNull(
            caso, "El caso es obligatorio."
        );

        int n = caso.vertices();
        List<List<Vecino>> listas = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            listas.add(new ArrayList<>());
        }

        //En grafos no dirigidos cada arista se guarda en ambos sentidos.
        for (Arista arista : caso.aristas()) {

            listas.get(arista.origen()).add(
                new Vecino(arista.destino(), arista.peso())
            );

            if (caso.tipo() == TipoGrafo.NO_DIRIGIDO) {
                listas.get(arista.destino()).add(
                    new Vecino(arista.origen(), arista.peso())
                );
            }
        }

        //Orden ascendente: el contrato exige visitar vecinos asi.
        List<List<Vecino>> inmutables = new ArrayList<>(n);

        for (List<Vecino> lista : listas) {
            lista.sort(Comparator.comparingInt(Vecino::destino));
            inmutables.add(List.copyOf(lista));
        }

        this.adyacencia = List.copyOf(inmutables);
    }

    //Devuelve el caso original del modelo compartido.
    public CasoGrafo caso() {
        return caso;
    }

    //Devuelve el nombre del caso.
    public String nombre() {
        return caso.nombre();
    }

    //Devuelve el tipo de grafo.
    public TipoGrafo tipo() {
        return caso.tipo();
    }

    //Indica si el grafo es dirigido.
    public boolean esDirigido() {
        return caso.tipo() == TipoGrafo.DIRIGIDO;
    }

    //Devuelve la cantidad de vertices (numerados de 0 a V-1).
    public int numVertices() {
        return caso.vertices();
    }

    //Devuelve la cantidad de aristas del archivo de entrada.
    public int numAristas() {
        return caso.aristas().size();
    }

    //Devuelve las aristas tal como aparecen en el archivo.
    //En grafos no dirigidos cada arista aparece una sola vez.
    public List<Arista> aristas() {
        return caso.aristas();
    }

    //Devuelve los vecinos de un vertice en orden ascendente.
    //En grafos dirigidos son los sucesores del vertice.
    public List<Vecino> vecinos(int vertice) {
        return adyacencia.get(vertice);
    }
}

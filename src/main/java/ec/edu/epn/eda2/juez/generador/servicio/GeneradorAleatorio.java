package ec.edu.epn.eda2.juez.generador.servicio;

import ec.edu.epn.eda2.juez.core.modelo.Arista;
import ec.edu.epn.eda2.juez.core.modelo.CasoGrafo;
import ec.edu.epn.eda2.juez.core.modelo.TipoGrafo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

//Genera grafos aleatorios para las pruebas del framework.
public final class GeneradorAleatorio {

    private final Random random;

    //Inicializa el generador con una semilla reproducible.
    public GeneradorAleatorio(long semilla) {
        this.random = new Random(semilla);
    }

    //Genera un grafo no dirigido con la cantidad de aristas indicada.
    public CasoGrafo generarNoDirigido(
            String nombre,
            int vertices,
            int cantidadAristas,
            int pesoMaximo,
            boolean asegurarConexo) {

        //Verifica los parametros recibidos.
        if (vertices < 1 || pesoMaximo < 1) {
            throw new IllegalArgumentException(
                "Los vertices y el peso maximo deben ser positivos."
            );
        }

        //Calcula la cantidad maxima de aristas sin duplicados.
        long maximo = (long) vertices * (vertices - 1) / 2;

        if (cantidadAristas < 0 || cantidadAristas > maximo) {
            throw new IllegalArgumentException(
                "Cantidad de aristas fuera de rango."
            );
        }

        //Un grafo conexo necesita al menos V-1 aristas.
        if (asegurarConexo && cantidadAristas < vertices - 1) {
            throw new IllegalArgumentException(
                "Un grafo conexo requiere al menos V-1 aristas."
            );
        }

        List<Arista> aristas = new ArrayList<>();
        Set<Long> conexiones = new HashSet<>();

        //Construye primero un arbol para garantizar conectividad.
        if (asegurarConexo) {

            List<Integer> orden = new ArrayList<>();

            for (int i = 0; i < vertices; i++) {
                orden.add(i);
            }

            //Mezcla los vertices para obtener conexiones aleatorias.
            java.util.Collections.shuffle(orden, random);

            for (int i = 1; i < vertices; i++) {

                int origen = orden.get(i);
                int destino = orden.get(random.nextInt(i));

                agregarArista(
                    origen, destino, vertices, pesoMaximo,
                    conexiones, aristas
                );
            }
        }

        //Agrega aristas aleatorias hasta alcanzar la cantidad solicitada.
        int intentosSinExito = 0;

        while (aristas.size() < cantidadAristas
                && intentosSinExito < 10000) {

            int origen = random.nextInt(vertices);
            int destino = random.nextInt(vertices);

            if (origen != destino && agregarArista(
                    origen, destino, vertices, pesoMaximo,
                    conexiones, aristas)) {

                intentosSinExito = 0;
            } else {
                intentosSinExito++;
            }
        }

        //Completa las aristas restantes sin repetir conexiones.
        //Evita ciclos de intentos excesivos en grafos muy densos.
        for (int i = 0;
             i < vertices && aristas.size() < cantidadAristas;
             i++) {

            for (int j = i + 1;
                 j < vertices && aristas.size() < cantidadAristas;
                 j++) {

                agregarArista(
                    i, j, vertices, pesoMaximo,
                    conexiones, aristas
                );
            }
        }

        //Devuelve el grafo completo.
        return new CasoGrafo(
            nombre, TipoGrafo.NO_DIRIGIDO, vertices, aristas
        );
    }

    //Genera un grafo disperso y conexo con V-1 aristas.
    public CasoGrafo generarDisperso(
            String nombre, int vertices, int pesoMaximo) {

        return generarNoDirigido(
            nombre, vertices, vertices - 1, pesoMaximo, true
        );
    }

    //Genera un grafo denso con aproximadamente 80% de aristas.
    public CasoGrafo generarDenso(
            String nombre, int vertices, int pesoMaximo) {

        if (vertices < 1) {
            throw new IllegalArgumentException(
                "La cantidad de vertices debe ser positiva."
            );
        }

        long maximo = (long) vertices * (vertices - 1) / 2;

        int cantidad = (int) Math.max(
            vertices - 1, Math.round(maximo * 0.80)
        );

        return generarNoDirigido(
            nombre, vertices, cantidad, pesoMaximo, true
        );
    }

    //Agrega una arista si no existe previamente.
    private boolean agregarArista(
            int origen,
            int destino,
            int vertices,
            int pesoMaximo,
            Set<Long> conexiones,
            List<Arista> aristas) {

        //Ordena los extremos para evitar duplicados invertidos.
        int a = Math.min(origen, destino);
        int b = Math.max(origen, destino);

        //Crea una clave unica para cada conexion.
        long clave = (long) a * vertices + b;

        if (!conexiones.add(clave)) {
            return false;
        }

        //Asigna un peso aleatorio entre 1 y pesoMaximo.
        int peso = random.nextInt(pesoMaximo) + 1;

        aristas.add(new Arista(a, b, peso));
        return true;
    }
}
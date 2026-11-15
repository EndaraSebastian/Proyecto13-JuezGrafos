package ec.edu.epn.eda2.juez.generador;

import ec.edu.epn.eda2.juez.core.modelo.Arista;
import ec.edu.epn.eda2.juez.core.modelo.CasoGrafo;
import ec.edu.epn.eda2.juez.core.modelo.TipoGrafo;

import java.util.List;

//Verifica que el modelo rechace grafos y aristas invalidos.
public final class PruebaCasosNegativos {

    private static int correctas = 0;
    private static int fallidas = 0;

    //Evita crear instancias de esta clase de prueba.
    private PruebaCasosNegativos() {
    }

    //Ejecuta los casos invalidos y los casos de control.
    public static void main(String[] args) {

        System.out.println("PRUEBAS NEGATIVAS DEL MODELO");
        System.out.println("----------------------------");

        //Comprueba indices invalidos en una arista.
        rechazar("Origen negativo",
                () -> new Arista(-1, 1, 3));

        rechazar("Destino negativo",
                () -> new Arista(0, -1, 3));

        //Comprueba pesos que no estan permitidos.
        rechazar("Peso cero",
                () -> new Arista(0, 1, 0));

        rechazar("Peso negativo",
                () -> new Arista(0, 1, -5));

        //Comprueba un bucle propio.
        rechazar("Bucle propio",
                () -> new CasoGrafo(
                        "bucle",
                        TipoGrafo.NO_DIRIGIDO,
                        3,
                        List.of(new Arista(2, 2, 4))
                ));

        //Comprueba un destino fuera del rango 0 a 2.
        rechazar("Vertice fuera de rango",
                () -> new CasoGrafo(
                        "fuera_rango",
                        TipoGrafo.NO_DIRIGIDO,
                        3,
                        List.of(new Arista(0, 3, 4))
                ));

        //Comprueba una conexion no dirigida duplicada.
        rechazar("Aristas duplicadas no dirigidas",
                () -> new CasoGrafo(
                        "duplicado",
                        TipoGrafo.NO_DIRIGIDO,
                        3,
                        List.of(
                                new Arista(0, 1, 2),
                                new Arista(1, 0, 5)
                        )
                ));

        //Comprueba una conexion dirigida duplicada.
        rechazar("Aristas duplicadas dirigidas",
                () -> new CasoGrafo(
                        "duplicado_dirigido",
                        TipoGrafo.DIRIGIDO,
                        3,
                        List.of(
                                new Arista(0, 1, 2),
                                new Arista(0, 1, 7)
                        )
                ));

        //Comprueba que un grafo tenga vertices.
        rechazar("Grafo sin vertices",
                () -> new CasoGrafo(
                        "sin_vertices",
                        TipoGrafo.NO_DIRIGIDO,
                        0,
                        List.of()
                ));

        //Un grafo desconectado es valido.
        aceptar("Grafo desconectado valido",
                () -> new CasoGrafo(
                        "desconectado",
                        TipoGrafo.NO_DIRIGIDO,
                        4,
                        List.of(new Arista(0, 1, 3))
                ));

        //Un grafo dirigido permite conexiones opuestas.
        aceptar("Aristas dirigidas opuestas validas",
                () -> new CasoGrafo(
                        "opuestas",
                        TipoGrafo.DIRIGIDO,
                        2,
                        List.of(
                                new Arista(0, 1, 2),
                                new Arista(1, 0, 4)
                        )
                ));

        //Un unico vertice sin aristas es un grafo valido.
        aceptar("Grafo de un vertice valido",
                () -> new CasoGrafo(
                        "un_vertice",
                        TipoGrafo.NO_DIRIGIDO,
                        1,
                        List.of()
                ));

        System.out.println("----------------------------");
        System.out.println("Correctas: " + correctas);
        System.out.println("Fallidas: " + fallidas);

        if (fallidas > 0) {
            System.exit(1);
        }
    }

    //La prueba pasa solamente si se rechaza el dato invalido.
    private static void rechazar(
            String nombre, Runnable operacion) {

        try {
            operacion.run();

            fallidas++;
            System.out.println(
                    "[ERROR] " + nombre
                    + ": se acepto un dato invalido."
            );

        } catch (IllegalArgumentException e) {

            correctas++;
            System.out.println("[OK] " + nombre);

        } catch (RuntimeException e) {

            fallidas++;
            System.out.println(
                    "[ERROR] " + nombre
                    + ": excepcion inesperada: "
                    + e.getClass().getSimpleName()
            );
        }
    }

    //La prueba pasa si el modelo acepta un caso valido.
    private static void aceptar(
            String nombre, Runnable operacion) {

        try {
            operacion.run();

            correctas++;
            System.out.println("[OK] " + nombre);

        } catch (RuntimeException e) {

            fallidas++;
            System.out.println(
                    "[ERROR] " + nombre
                    + ": " + e.getMessage()
            );
        }
    }
}
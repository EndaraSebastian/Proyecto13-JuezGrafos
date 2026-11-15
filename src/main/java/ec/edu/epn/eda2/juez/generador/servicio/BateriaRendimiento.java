package ec.edu.epn.eda2.juez.generador.servicio;

import ec.edu.epn.eda2.juez.core.modelo.CasoGrafo;

import java.util.ArrayList;
import java.util.List;

//Organiza los grafos utilizados para las pruebas de rendimiento.
public final class BateriaRendimiento {

    //Define el peso maximo de las aristas.
    private static final int PESO_MAXIMO = 20;

    //Impide crear instancias de esta clase utilitaria.
    private BateriaRendimiento() {
    }

    //Construye los grafos utilizados en los experimentos.
    public static List<CasoGrafo> crearBateria() {

        List<CasoGrafo> casos = new ArrayList<>();

        //Caso 14: grafo disperso de 100 vertices.
        casos.add(
            crearCaso(
                "14_rendimiento_100_disperso",
                100,
                150,
                20261314L
            )
        );

        //Caso 15: grafo denso de 100 vertices.
        casos.add(
            crearCaso(
                "15_rendimiento_100_denso",
                100,
                3960,
                20261315L
            )
        );

        //Caso 16: grafo disperso de 500 vertices.
        casos.add(
            crearCaso(
                "16_rendimiento_500_disperso",
                500,
                750,
                20261316L
            )
        );

        //Caso 17: grafo denso de 500 vertices.
        casos.add(
            crearCaso(
                "17_rendimiento_500_denso",
                500,
                99800,
                20261317L
            )
        );

        //Caso 18: grafo disperso de 1000 vertices.
        casos.add(
            crearCaso(
                "18_rendimiento_1000_disperso",
                1000,
                1500,
                20261318L
            )
        );

        //Devuelve una lista que no permite modificaciones.
        return List.copyOf(casos);
    }

    //Construye un grafo utilizando una semilla fija.
    private static CasoGrafo crearCaso(
            String nombre,
            int vertices,
            int aristas,
            long semilla) {

        //Crea un generador independiente para cada caso.
        GeneradorAleatorio generador =
                new GeneradorAleatorio(semilla);

        //Garantiza que el grafo generado sea conexo.
        return generador.generarNoDirigido(
            nombre,
            vertices,
            aristas,
            PESO_MAXIMO,
            true
        );
    }
}
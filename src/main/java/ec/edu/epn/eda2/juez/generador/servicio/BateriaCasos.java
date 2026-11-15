package ec.edu.epn.eda2.juez.generador.servicio;

import ec.edu.epn.eda2.juez.core.modelo.CasoGrafo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//Organiza los grafos utilizados en la bateria de pruebas.
public final class BateriaCasos {

    //Impide crear instancias de esta clase utilitaria.
    private BateriaCasos() {
    }

    //Reune los grafos manuales y aleatorios.
    public static List<CasoGrafo> crearBateria() {

        List<CasoGrafo> casos = new ArrayList<>();

        //Agrega los nueve grafos definidos manualmente.
        casos.addAll(CasosManuales.crearCasos());

        //Caso 10: grafo disperso y conexo.
        GeneradorAleatorio generadorDisperso =
                new GeneradorAleatorio(20261301L);

        casos.add(
            generadorDisperso.generarDisperso(
                "10_disperso", 12, 20
            )
        );

        //Caso 11: grafo denso y conexo.
        GeneradorAleatorio generadorDenso =
                new GeneradorAleatorio(20261302L);

        casos.add(
            generadorDenso.generarDenso(
                "11_denso", 12, 20
            )
        );

        //Caso 12: grafo aleatorio con 22 aristas.
        GeneradorAleatorio generadorMedio =
                new GeneradorAleatorio(20261303L);

        casos.add(
            generadorMedio.generarNoDirigido(
                "12_aleatorio_medio",
                12,
                22,
                20,
                true
            )
        );

        //Caso 13: grafo conexo que necesariamente contiene ciclos.
        GeneradorAleatorio generadorCiclos =
                new GeneradorAleatorio(20261304L);

        casos.add(
            generadorCiclos.generarNoDirigido(
                "13_ciclo_aleatorio",
                12,
                30,
                20,
                true
            )
        );

        //Comprueba que no existan nombres de casos repetidos.
        verificarNombres(casos);

        //Devuelve una lista que no puede modificarse.
        return List.copyOf(casos);
    }

    //Verifica que cada caso tenga un nombre unico.
    private static void verificarNombres(List<CasoGrafo> casos) {

        Set<String> nombres = new HashSet<>();

        for (CasoGrafo caso : casos) {

            if (!nombres.add(caso.nombre())) {
                throw new IllegalArgumentException(
                    "Caso duplicado: " + caso.nombre()
                );
            }
        }
    }
}
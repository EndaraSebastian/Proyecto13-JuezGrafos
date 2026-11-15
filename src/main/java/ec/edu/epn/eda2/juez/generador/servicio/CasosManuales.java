package ec.edu.epn.eda2.juez.generador.servicio;

import ec.edu.epn.eda2.juez.core.modelo.Arista;
import ec.edu.epn.eda2.juez.core.modelo.CasoGrafo;
import ec.edu.epn.eda2.juez.core.modelo.TipoGrafo;

import java.util.List;

//Construye grafos manuales para comprobar los algoritmos.
public final class CasosManuales {

    //Impide crear instancias de esta clase utilitaria.
    private CasosManuales() {
    }

    //Devuelve la bateria de grafos manuales.
    public static List<CasoGrafo> crearCasos() {

        return List.of(
            crearConexo(),
            crearDesconexo(),
            crearDAG(),
            crearConCicloDirigido(),
            crearMSTEmpates(),
            crearAislados(),
            crearUnVertice(),
            crearCicloNoDirigido(),
            crearDAGDesconexo()
        );
    }

    //Caso 1: grafo conexo para DFS, BFS y MST.
    private static CasoGrafo crearConexo() {

        return new CasoGrafo(
            "01_conexo",
            TipoGrafo.NO_DIRIGIDO,
            5,
            List.of(
                new Arista(0, 1, 4),
                new Arista(0, 2, 2),
                new Arista(1, 2, 1),
                new Arista(1, 3, 5),
                new Arista(2, 3, 8),
                new Arista(2, 4, 10),
                new Arista(3, 4, 2)
            )
        );
    }

    //Caso 2: grafo desconectado con un vertice aislado.
    private static CasoGrafo crearDesconexo() {

        return new CasoGrafo(
            "02_desconexo",
            TipoGrafo.NO_DIRIGIDO,
            6,
            List.of(
                new Arista(0, 1, 3),
                new Arista(1, 2, 5),
                new Arista(3, 4, 2)
            )
        );
    }

    //Caso 3: grafo dirigido aciclico para orden topologico.
    private static CasoGrafo crearDAG() {

        return new CasoGrafo(
            "03_dag",
            TipoGrafo.DIRIGIDO,
            6,
            List.of(
                new Arista(0, 2, 1),
                new Arista(1, 2, 1),
                new Arista(1, 3, 1),
                new Arista(2, 4, 1),
                new Arista(3, 4, 1),
                new Arista(4, 5, 1)
            )
        );
    }

    //Caso 4: grafo dirigido con un ciclo 0-1-2-0.
    private static CasoGrafo crearConCicloDirigido() {

        return new CasoGrafo(
            "04_ciclo_dirigido",
            TipoGrafo.DIRIGIDO,
            5,
            List.of(
                new Arista(0, 1, 1),
                new Arista(1, 2, 1),
                new Arista(2, 0, 1),
                new Arista(2, 3, 1),
                new Arista(3, 4, 1)
            )
        );
    }

    //Caso 5: grafo con varios arboles de expansion minima.
    private static CasoGrafo crearMSTEmpates() {

        return new CasoGrafo(
            "05_mst_empates",
            TipoGrafo.NO_DIRIGIDO,
            4,
            List.of(
                new Arista(0, 1, 1),
                new Arista(1, 2, 1),
                new Arista(2, 3, 1),
                new Arista(3, 0, 1),
                new Arista(0, 2, 2),
                new Arista(1, 3, 2)
            )
        );
    }

    //Caso 6: grafo sin aristas y con vertices aislados.
    private static CasoGrafo crearAislados() {

        return new CasoGrafo(
            "06_aislados",
            TipoGrafo.NO_DIRIGIDO,
            5,
            List.of()
        );
    }

    //Caso 7: grafo con un solo vertice.
    private static CasoGrafo crearUnVertice() {

        return new CasoGrafo(
            "07_un_vertice",
            TipoGrafo.NO_DIRIGIDO,
            1,
            List.of()
        );
    }

    //Caso 8: grafo no dirigido que contiene un ciclo.
    private static CasoGrafo crearCicloNoDirigido() {

        return new CasoGrafo(
            "08_ciclo_no_dirigido",
            TipoGrafo.NO_DIRIGIDO,
            5,
            List.of(
                new Arista(0, 1, 2),
                new Arista(1, 2, 3),
                new Arista(2, 0, 4),
                new Arista(2, 3, 1),
                new Arista(3, 4, 5)
            )
        );
    }

    //Caso 9: DAG con componentes desconectados.
    private static CasoGrafo crearDAGDesconexo() {

        return new CasoGrafo(
            "09_dag_desconexo",
            TipoGrafo.DIRIGIDO,
            6,
            List.of(
                new Arista(0, 1, 1),
                new Arista(1, 2, 1),
                new Arista(3, 4, 1)
            )
        );
    }
}
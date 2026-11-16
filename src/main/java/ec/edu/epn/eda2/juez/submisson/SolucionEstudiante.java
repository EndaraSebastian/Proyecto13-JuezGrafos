package ec.edu.epn.eda2.juez.submission;

import ec.edu.epn.eda2.juez.core.modelo.CasoGrafo;
import java.util.List;

public interface SolucionEstudiante {
    List<Integer> dfs(CasoGrafo g, int inicio);
    List<Integer> bfs(CasoGrafo g, int inicio);
    List<Integer> ordenTopologico(CasoGrafo g);
    int mst(CasoGrafo g);
}
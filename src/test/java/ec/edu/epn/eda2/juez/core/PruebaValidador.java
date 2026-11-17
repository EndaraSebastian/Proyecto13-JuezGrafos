package ec.edu.epn.eda2.juez.core;

import ec.edu.epn.eda2.juez.core.ejecucion.Algoritmo;
import ec.edu.epn.eda2.juez.core.ejecucion.CasoPrueba;
import ec.edu.epn.eda2.juez.core.ejecucion.CatalogoCasos;
import ec.edu.epn.eda2.juez.core.ejecucion.ConfiguracionJuez;
import ec.edu.epn.eda2.juez.core.ejecucion.EjecutorJuez;
import ec.edu.epn.eda2.juez.core.ejecucion.EstadoEjecucion;
import ec.edu.epn.eda2.juez.core.ejecucion.ReporteJuez;
import ec.edu.epn.eda2.juez.core.ejecucion.ResultadoEjecucion;
import ec.edu.epn.eda2.juez.core.modelo.Arista;
import ec.edu.epn.eda2.juez.core.modelo.GrafoCargado;
import ec.edu.epn.eda2.juez.core.parser.LectorGrafoTxt;
import ec.edu.epn.eda2.juez.core.parser.LectorReferenciaTxt;
import ec.edu.epn.eda2.juez.core.parser.ReferenciaCaso;
import ec.edu.epn.eda2.juez.submission.ResultadoBFS;
import ec.edu.epn.eda2.juez.submission.ResultadoMST;
import ec.edu.epn.eda2.juez.submission.ResultadoTopologico;
import ec.edu.epn.eda2.juez.submission.SolucionModelo;
import ec.edu.epn.eda2.juez.validador.ValidadorGrafos;
import ec.edu.epn.eda2.juez.validador.Veredicto;
import ec.edu.epn.eda2.juez.validador.Veredicto.Estado;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static ec.edu.epn.eda2.juez.core.MiniPrueba.exigir;
import static ec.edu.epn.eda2.juez.core.MiniPrueba.probar;

//Pruebas del validador real y de la submission de ejemplo.
//Ejecutar: java -cp out ec.edu.epn.eda2.juez.core.PruebaValidador
public final class PruebaValidador {

    private static final ConfiguracionJuez RAPIDA =
            ConfiguracionJuez.porDefecto()
                .conCalentamientos(0).conRepeticiones(1);

    private static final ValidadorGrafos V = new ValidadorGrafos();

    private PruebaValidador() {
    }

    //----- Soluciones deliberadamente erroneas -----

    public static class DfsAlReves extends SolucionModelo {
        @Override
        public int[] dfs(GrafoCargado g, int inicio) {
            int[] o = super.dfs(g, inicio);
            for (int i = 0; i < o.length / 2; i++) {
                int t = o[i]; o[i] = o[o.length - 1 - i]; o[o.length - 1 - i] = t;
            }
            return o;
        }
    }

    public static class BfsSinDistancias extends SolucionModelo {
        @Override
        public ResultadoBFS bfs(GrafoCargado g, int inicio) {
            ResultadoBFS r = super.bfs(g, inicio);
            return new ResultadoBFS(r.orden(), new int[g.numVertices()]);
        }
    }

    public static class TopologicoInvertido extends SolucionModelo {
        @Override
        public ResultadoTopologico ordenTopologico(GrafoCargado g) {
            ResultadoTopologico r = super.ordenTopologico(g);
            if (r.hayCiclo()) return r;
            int[] o = r.orden();
            int[] inv = new int[o.length];
            for (int i = 0; i < o.length; i++) inv[i] = o[o.length - 1 - i];
            return ResultadoTopologico.aciclico(inv);
        }
    }

    public static class NuncaDetectaCiclo extends SolucionModelo {
        @Override
        public ResultadoTopologico ordenTopologico(GrafoCargado g) {
            int n = g.numVertices();
            int[] o = new int[n];
            for (int i = 0; i < n; i++) o[i] = i;
            return ResultadoTopologico.aciclico(o);
        }
    }

    public static class MstMaximo extends SolucionModelo {
        @Override
        public ResultadoMST mst(GrafoCargado g) {
            List<Arista> todas = new ArrayList<>(g.aristas());
            todas.sort((a, b) -> Integer.compare(b.peso(), a.peso()));
            int[] p = new int[g.numVertices()];
            for (int i = 0; i < p.length; i++) p[i] = i;
            List<Arista> el = new ArrayList<>();
            for (Arista a : todas) {
                int x = raiz(p, a.origen()), y = raiz(p, a.destino());
                if (x != y) { p[x] = y; el.add(a); }
            }
            return new ResultadoMST(el);
        }
    }

    private static void postorden(
            GrafoCargado g, int u, boolean[] vis, int[] post, int[] cnt) {
        if (vis[u]) return;
        vis[u] = true;
        for (var v : g.vecinos(u)) postorden(g, v.destino(), vis, post, cnt);
        post[cnt[0]++] = u;
    }

    private static int raiz(int[] p, int x) {
        while (p[x] != x) x = p[x] = p[p[x]];
        return x;
    }

    //----- Utilidades -----

    private static CasoPrueba caso(String nombre) {
        return new CasoPrueba(nombre,
            CatalogoCasos.ENTRADA_FUNCIONAL.resolve(nombre + ".txt"),
            CatalogoCasos.REFERENCIA_FUNCIONAL.resolve(nombre + ".txt"));
    }

    private static GrafoCargado grafo(String nombre) throws Exception {
        return LectorGrafoTxt.leer(
            CatalogoCasos.ENTRADA_FUNCIONAL.resolve(nombre + ".txt"));
    }

    private static ReferenciaCaso ref(String nombre) throws Exception {
        return LectorReferenciaTxt.leer(
            CatalogoCasos.REFERENCIA_FUNCIONAL.resolve(nombre + ".txt"));
    }

    private static List<ResultadoEjecucion> juzgar(
            Object solucion, String nombreCaso) {
        return new EjecutorJuez(
            (ec.edu.epn.eda2.juez.submission.SolucionEstudiante) solucion,
            V, RAPIDA).juzgarCaso(caso(nombreCaso));
    }

    private static EstadoEjecucion estado(
            List<ResultadoEjecucion> r, Algoritmo a) {
        for (ResultadoEjecucion x : r) {
            if (x.algoritmo() == a) return x.estado();
        }
        throw new AssertionError("Sin fila para " + a);
    }

    private static void exigirIncorrecto(Veredicto v, String texto) {
        exigir(v.estado() == Estado.INCORRECTO,
            "se esperaba INCORRECTO y fue " + v.estado());
        exigir(v.detalle().contains(texto),
            "detalle '" + v.detalle() + "' no contiene '" + texto + "'");
    }

    private static List<Arista> a(int... t) {
        List<Arista> l = new ArrayList<>();
        for (int i = 0; i < t.length; i += 3) {
            l.add(new Arista(t[i], t[i + 1], t[i + 2]));
        }
        return l;
    }

    //----- Pruebas -----

    public static void main(String[] args) throws Exception {

        System.out.println("PRUEBAS DEL VALIDADOR Y LA SUBMISSION");
        System.out.println("--------------------------------");

        probar("SolucionModelo: toda la bateria (18 casos) es CORRECTA", () -> {
            List<CasoPrueba> todos = new ArrayList<>(CatalogoCasos.descubrir(
                CatalogoCasos.ENTRADA_FUNCIONAL, CatalogoCasos.REFERENCIA_FUNCIONAL));
            todos.addAll(CatalogoCasos.descubrir(
                CatalogoCasos.ENTRADA_RENDIMIENTO, CatalogoCasos.REFERENCIA_RENDIMIENTO));
            exigir(todos.size() == 18, "se esperaban 18 casos: " + todos.size());
            ReporteJuez rep = MiniPrueba.silenciando(() ->
                new EjecutorJuez(new SolucionModelo(), V, RAPIDA).juzgar(todos));
            exigir(!rep.hayFallos(), "hay fallos");
            exigir(rep.contar(EstadoEjecucion.CORRECTO) == 54,
                "correctos: " + rep.contar(EstadoEjecucion.CORRECTO));
            exigir(rep.contar(EstadoEjecucion.INCORRECTO) == 0, "incorrectos");
            exigir(rep.contar(EstadoEjecucion.SIN_VALIDAR) == 0, "sin validar");
        });

        probar("Juez: DFS al reves -> INCORRECTO", () ->
            exigir(estado(juzgar(new DfsAlReves(), "01_conexo"), Algoritmo.DFS)
                == EstadoEjecucion.INCORRECTO, "DFS no rechazado"));

        probar("Juez: BFS con distancias en cero -> INCORRECTO", () ->
            exigir(estado(juzgar(new BfsSinDistancias(), "01_conexo"), Algoritmo.BFS)
                == EstadoEjecucion.INCORRECTO, "BFS no rechazado"));

        probar("Juez: topologico invertido -> INCORRECTO", () ->
            exigir(estado(juzgar(new TopologicoInvertido(), "03_dag"),
                Algoritmo.TOPOLOGICO) == EstadoEjecucion.INCORRECTO, "no rechazado"));

        probar("Juez: no detectar ciclo -> INCORRECTO", () ->
            exigir(estado(juzgar(new NuncaDetectaCiclo(), "04_ciclo_dirigido"),
                Algoritmo.TOPOLOGICO) == EstadoEjecucion.INCORRECTO, "no rechazado"));

        probar("Juez: MST de peso maximo -> INCORRECTO", () ->
            exigir(estado(juzgar(new MstMaximo(), "01_conexo"), Algoritmo.MST)
                == EstadoEjecucion.INCORRECTO, "no rechazado"));

        probar("Juez: las soluciones erroneas no afectan a los otros algoritmos", () ->
            exigir(estado(juzgar(new DfsAlReves(), "01_conexo"), Algoritmo.BFS)
                == EstadoEjecucion.CORRECTO, "BFS deberia ser correcto"));

        probar("DFS: vertice fuera de rango", () ->
            exigirIncorrecto(V.validarDfs(grafo("01_conexo"), ref("01_conexo"),
                new int[]{0, 99}), "fuera de rango"));

        probar("DFS: vertice repetido", () ->
            exigirIncorrecto(V.validarDfs(grafo("01_conexo"), ref("01_conexo"),
                new int[]{0, 1, 1}), "repetido"));

        probar("DFS: faltan vertices alcanzables", () ->
            exigirIncorrecto(V.validarDfs(grafo("01_conexo"), ref("01_conexo"),
                new int[]{0}), "esperaban"));

        probar("DFS: no incluye inalcanzables (02_desconexo) -> correcto", () -> {
            ReferenciaCaso r = ref("02_desconexo");
            exigir(V.validarDfs(grafo("02_desconexo"), r, r.dfsOrden())
                .estado() == Estado.CORRECTO, "deberia ser correcto");
        });

        probar("BFS: arreglo de distancias de longitud incorrecta", () -> {
            ReferenciaCaso r = ref("01_conexo");
            exigirIncorrecto(V.validarBfs(grafo("01_conexo"), r,
                new ResultadoBFS(r.bfsOrden(), new int[]{0})), "distancias");
        });

        probar("BFS: distancia -1 faltante en inalcanzables", () -> {
            ReferenciaCaso r = ref("02_desconexo");
            exigirIncorrecto(V.validarBfs(grafo("02_desconexo"), r,
                new ResultadoBFS(r.bfsOrden(), new int[6])), "distancia");
        });

        probar("BFS: respuesta de referencia es correcta", () -> {
            ReferenciaCaso r = ref("02_desconexo");
            exigir(V.validarBfs(grafo("02_desconexo"), r,
                new ResultadoBFS(r.bfsOrden(), r.bfsDistancias()))
                .estado() == Estado.CORRECTO, "deberia ser correcto");
        });

        probar("Topologico: acepta un orden valido distinto al de Kahn", () -> {
            GrafoCargado g = grafo("09_dag_desconexo");
            ReferenciaCaso r = ref("09_dag_desconexo");
            //Orden alternativo: postorden inverso de un DFS completo.
            int n = g.numVertices();
            boolean[] vis = new boolean[n];
            int[] post = new int[n];
            int[] cnt = {0};
            for (int s = n - 1; s >= 0; s--) postorden(g, s, vis, post, cnt);
            int[] alt = new int[n];
            for (int i = 0; i < n; i++) alt[i] = post[n - 1 - i];
            exigir(!java.util.Arrays.equals(alt,
                new SolucionModelo().ordenTopologico(g).orden()),
                "el orden alternativo deberia diferir del de Kahn");
            exigir(V.validarTopologico(g, r, ResultadoTopologico.aciclico(alt))
                .estado() == Estado.CORRECTO, "orden alternativo valido");
            exigir(V.validarTopologico(g, r,
                ResultadoTopologico.aciclico(r.topoOrden()))
                .estado() == Estado.CORRECTO, "orden de referencia");
        });

        probar("Topologico: orden incompleto", () ->
            exigirIncorrecto(V.validarTopologico(grafo("03_dag"), ref("03_dag"),
                ResultadoTopologico.aciclico(new int[]{0})), "vertices"));

        probar("Topologico: vertice repetido", () -> {
            int n = grafo("03_dag").numVertices();
            int[] o = new int[n];
            exigirIncorrecto(V.validarTopologico(grafo("03_dag"), ref("03_dag"),
                ResultadoTopologico.aciclico(o)), "repetido");
        });

        probar("Topologico: vertice fuera de rango", () -> {
            int n = grafo("03_dag").numVertices();
            int[] o = new int[n];
            for (int i = 0; i < n; i++) o[i] = i;
            o[0] = -1;
            exigirIncorrecto(V.validarTopologico(grafo("03_dag"), ref("03_dag"),
                ResultadoTopologico.aciclico(o)), "fuera de rango");
        });

        probar("Topologico: reportar ciclo en un DAG", () ->
            exigirIncorrecto(V.validarTopologico(grafo("03_dag"), ref("03_dag"),
                ResultadoTopologico.conCiclo()), "ciclo"));

        probar("Topologico: ciclo detectado correctamente", () ->
            exigir(V.validarTopologico(grafo("04_ciclo_dirigido"), ref("04_ciclo_dirigido"),
                ResultadoTopologico.conCiclo()).estado() == Estado.CORRECTO, "ciclo"));

        probar("MST: arista inexistente", () ->
            exigirIncorrecto(V.validarMst(grafo("01_conexo"), ref("01_conexo"),
                new ResultadoMST(a(0, 4, 1))), "no existe"));

        probar("MST: arista con peso distinto al del grafo", () -> {
            Arista real = grafo("01_conexo").aristas().get(0);
            exigirIncorrecto(V.validarMst(grafo("01_conexo"), ref("01_conexo"),
                new ResultadoMST(a(real.origen(), real.destino(), real.peso() + 1))),
                "peso");
        });

        probar("MST: arista repetida o ciclo", () -> {
            Arista e = grafo("08_ciclo_no_dirigido").aristas().get(0);
            exigirIncorrecto(V.validarMst(grafo("08_ciclo_no_dirigido"),
                ref("08_ciclo_no_dirigido"),
                new ResultadoMST(a(e.origen(), e.destino(), e.peso(),
                    e.destino(), e.origen(), e.peso()))), "ciclo");
        });

        probar("MST: conjunto vacio no cubre el grafo", () ->
            exigirIncorrecto(V.validarMst(grafo("01_conexo"), ref("01_conexo"),
                new ResultadoMST(List.of())), "componentes"));

        probar("MST: bosque minimo en grafo desconexo", () ->
            exigir(V.validarMst(grafo("02_desconexo"), ref("02_desconexo"),
                new SolucionModelo().mst(grafo("02_desconexo"))).estado()
                == Estado.CORRECTO, "bosque"));

        probar("MST: arista en sentido contrario es aceptada", () -> {
            GrafoCargado g = grafo("01_conexo");
            ResultadoMST r = new SolucionModelo().mst(g);
            List<Arista> al = new ArrayList<>();
            for (Arista e : r.aristas()) {
                al.add(new Arista(e.destino(), e.origen(), e.peso()));
            }
            exigir(V.validarMst(g, ref("01_conexo"), new ResultadoMST(al))
                .estado() == Estado.CORRECTO, "sentido");
        });

        probar("Validador: nombre y valor por defecto del juez", () -> {
            exigir("ValidadorGrafos".equals(V.nombre()), "nombre");
            exigir(JuezCli.VALIDADOR_POR_DEFECTO.endsWith("ValidadorGrafos"),
                "el validador por defecto debe ser ValidadorGrafos");
        });

        probar("Comando juzgar con SolucionModelo termina sin fallos", () -> {
            int codigo = MiniPrueba.silenciando(() -> JuezCli.ejecutar(new String[]{
                SolucionModelo.class.getName(), "--repeticiones", "1",
                "--calentamientos", "0"}));
            exigir(codigo == 0, "codigo " + codigo);
        });

        probar("Reporte: SolucionModelo reporta memoria en cada fila medida", () -> {
            List<ResultadoEjecucion> filas = juzgar(new SolucionModelo(), "01_conexo");
            for (ResultadoEjecucion f : filas) {
                if (f.estado() == EstadoEjecucion.CORRECTO) {
                    exigir(f.memoriaBytes() == -1 || f.memoriaBytes() > 0,
                        f.algoritmo() + " sin dato de memoria: " + f.memoriaBytes());
                }
            }
        });

        MiniPrueba.terminar();
    }
}

package ec.edu.epn.eda2.juez.core.ejecucion;

import ec.edu.epn.eda2.juez.core.modelo.GrafoCargado;
import ec.edu.epn.eda2.juez.core.parser.LectorGrafoTxt;
import ec.edu.epn.eda2.juez.core.parser.LectorReferenciaTxt;
import ec.edu.epn.eda2.juez.core.parser.ReferenciaCaso;
import ec.edu.epn.eda2.juez.submission.SolucionEstudiante;
import ec.edu.epn.eda2.juez.validador.ValidadorResultado;
import ec.edu.epn.eda2.juez.validador.Veredicto;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;

//Orquesta el juzgamiento: carga cada caso, ejecuta y mide los cuatro
//algoritmos de la solucion, pide el veredicto al validador y arma
//las filas del reporte.
//
//Flujo por caso:
//  1. Leer el TXT de entrada y la referencia (parser).
//  2. Comprobar que la referencia sea coherente con el grafo.
//  3. Para cada algoritmo aplicable: medir la llamada, y si termino
//     bien, entregar el resultado al validador.
//  4. Registrar el estado y el tiempo.
public final class EjecutorJuez {

    //Grafo y referencia de un caso ya cargados.
    private record CasoCargado(
            GrafoCargado grafo, ReferenciaCaso referencia) {
    }

    private final SolucionEstudiante solucion;
    private final ValidadorResultado validador;
    private final ConfiguracionJuez config;

    //Crea el ejecutor con la solucion, el validador y la medicion.
    public EjecutorJuez(
            SolucionEstudiante solucion,
            ValidadorResultado validador,
            ConfiguracionJuez config) {

        this.solucion = Objects.requireNonNull(solucion);
        this.validador = Objects.requireNonNull(validador);
        this.config = Objects.requireNonNull(config);
    }

    //Juzga una lista de casos y devuelve el reporte completo.
    public ReporteJuez juzgar(List<CasoPrueba> casos) {

        List<ResultadoEjecucion> filas = new ArrayList<>();

        for (CasoPrueba caso : casos) {
            filas.addAll(juzgarCaso(caso));
        }

        return new ReporteJuez(
            solucion.nombre(), validador.nombre(), config, filas
        );
    }

    //Juzga un solo caso. Nunca lanza excepciones por culpa de la
    //solucion, del validador ni de un TXT mal formado: todo se
    //convierte en una fila con su estado.
    public List<ResultadoEjecucion> juzgarCaso(CasoPrueba prueba) {

        CasoCargado cargado;

        try {
            cargado = cargar(prueba);
        } catch (IOException | IllegalArgumentException e) {

            return List.of(new ResultadoEjecucion(
                prueba.nombre(), 0, 0, null,
                EstadoEjecucion.ERROR_CASO, -1, -1,
                describir(e)
            ));
        }

        final GrafoCargado grafo = cargado.grafo();
        final ReferenciaCaso ref = cargado.referencia();

        List<ResultadoEjecucion> filas = new ArrayList<>();

        //DFS y BFS aplican a todo grafo, desde el vertice INICIO.
        filas.add(ejecutar(grafo, Algoritmo.DFS,
            () -> solucion.dfs(grafo, ref.inicio()),
            orden -> validador.validarDfs(grafo, ref, orden)));

        filas.add(ejecutar(grafo, Algoritmo.BFS,
            () -> solucion.bfs(grafo, ref.inicio()),
            resultado -> validador.validarBfs(grafo, ref, resultado)));

        //Orden topologico: solo grafos dirigidos.
        if (grafo.esDirigido()) {

            filas.add(ejecutar(grafo, Algoritmo.TOPOLOGICO,
                () -> solucion.ordenTopologico(grafo),
                resultado ->
                    validador.validarTopologico(grafo, ref, resultado)));

        } else {
            filas.add(noAplica(grafo, Algoritmo.TOPOLOGICO));
        }

        //MST: solo grafos no dirigidos.
        if (!grafo.esDirigido()) {

            filas.add(ejecutar(grafo, Algoritmo.MST,
                () -> solucion.mst(grafo),
                resultado -> validador.validarMst(grafo, ref, resultado)));

        } else {
            filas.add(noAplica(grafo, Algoritmo.MST));
        }

        return filas;
    }

    //Lee el grafo y la referencia y comprueba que concuerden.
    private CasoCargado cargar(CasoPrueba prueba) throws IOException {

        if (!Files.isRegularFile(prueba.entrada())) {
            throw new IllegalArgumentException(
                "No existe el archivo de entrada: " + prueba.entrada()
            );
        }

        if (!Files.isRegularFile(prueba.referencia())) {
            throw new IllegalArgumentException(
                "No existe la referencia: " + prueba.referencia()
                + " (ejecute el comando referencias)"
            );
        }

        GrafoCargado grafo = LectorGrafoTxt.leer(prueba.entrada());
        ReferenciaCaso referencia =
                LectorReferenciaTxt.leer(prueba.referencia());

        referencia.verificarCoherencia(grafo);

        return new CasoCargado(grafo, referencia);
    }

    //Mide un algoritmo y, si termino bien, lo valida.
    private <T> ResultadoEjecucion ejecutar(
            GrafoCargado grafo,
            Algoritmo algoritmo,
            Callable<T> tarea,
            Function<T, Veredicto> validar) {

        Medicion<T> medicion = MedidorEjecucion.medir(tarea, config);

        if (medicion.agotoTiempo()) {
            return fila(grafo, algoritmo, EstadoEjecucion.TIMEOUT,
                -1, -1,
                "Supero el limite de " + config.timeoutMs() + " ms.");
        }

        if (!medicion.exitosa()) {
            return fila(grafo, algoritmo,
                EstadoEjecucion.ERROR_EJECUCION, -1, -1,
                describir(medicion.error()));
        }

        if (medicion.resultado() == null) {
            return fila(grafo, algoritmo,
                EstadoEjecucion.ERROR_EJECUCION,
                medicion.medianaNs(), medicion.minimoNs(),
                "El metodo devolvio null.",
                medicion.memoriaMedianaBytes());
        }

        Veredicto veredicto;

        try {
            veredicto = validar.apply(medicion.resultado());

            if (veredicto == null) {
                throw new IllegalStateException(
                    "El validador devolvio null."
                );
            }

        } catch (RuntimeException | StackOverflowError e) {

            return fila(grafo, algoritmo,
                EstadoEjecucion.ERROR_VALIDADOR,
                medicion.medianaNs(), medicion.minimoNs(),
                describir(e), medicion.memoriaMedianaBytes());
        }

        return fila(grafo, algoritmo, convertir(veredicto.estado()),
            medicion.medianaNs(), medicion.minimoNs(),
            veredicto.detalle(), medicion.memoriaMedianaBytes());
    }

    //Fila para un algoritmo que no corresponde al tipo de grafo.
    private static ResultadoEjecucion noAplica(
            GrafoCargado grafo, Algoritmo algoritmo) {

        return fila(grafo, algoritmo, EstadoEjecucion.NO_APLICA,
            -1, -1,
            grafo.esDirigido()
                ? "Solo aplica a grafos no dirigidos."
                : "Solo aplica a grafos dirigidos.");
    }

    //Arma una fila con los datos del grafo (sin memoria medida).
    private static ResultadoEjecucion fila(
            GrafoCargado grafo,
            Algoritmo algoritmo,
            EstadoEjecucion estado,
            long medianaNs,
            long minimoNs,
            String detalle) {

        return fila(grafo, algoritmo, estado, medianaNs, minimoNs,
            detalle, -1);
    }

    //Arma una fila con los datos del grafo y la memoria medida.
    private static ResultadoEjecucion fila(
            GrafoCargado grafo,
            Algoritmo algoritmo,
            EstadoEjecucion estado,
            long medianaNs,
            long minimoNs,
            String detalle,
            long memoriaBytes) {

        return new ResultadoEjecucion(
            grafo.nombre(), grafo.numVertices(), grafo.numAristas(),
            algoritmo, estado, medianaNs, minimoNs, detalle,
            memoriaBytes
        );
    }

    //Traduce el veredicto del validador al estado del reporte.
    private static EstadoEjecucion convertir(Veredicto.Estado estado) {

        return switch (estado) {
            case CORRECTO -> EstadoEjecucion.CORRECTO;
            case INCORRECTO -> EstadoEjecucion.INCORRECTO;
            case PENDIENTE -> EstadoEjecucion.SIN_VALIDAR;
        };
    }

    //Resume una excepcion en una sola linea corta.
    private static String describir(Throwable error) {

        if (error instanceof StackOverflowError) {
            return "StackOverflowError (recursion demasiado profunda).";
        }

        String mensaje = error.getMessage();
        String texto = error.getClass().getSimpleName()
                + (mensaje == null ? "" : ": " + mensaje);

        texto = texto.replace('\r', ' ').replace('\n', ' ');

        if (texto.length() > 160) {
            texto = texto.substring(0, 157) + "...";
        }

        return texto;
    }
}

package ec.edu.epn.eda2.juez.core;

import ec.edu.epn.eda2.juez.core.ejecucion.CasoPrueba;
import ec.edu.epn.eda2.juez.core.ejecucion.CatalogoCasos;
import ec.edu.epn.eda2.juez.core.ejecucion.ConfiguracionJuez;
import ec.edu.epn.eda2.juez.core.ejecucion.EjecutorJuez;
import ec.edu.epn.eda2.juez.core.ejecucion.EstadoEjecucion;
import ec.edu.epn.eda2.juez.core.ejecucion.ReporteJuez;
import ec.edu.epn.eda2.juez.core.ejecucion.ResultadoEjecucion;
import ec.edu.epn.eda2.juez.core.modelo.Arista;
import ec.edu.epn.eda2.juez.core.modelo.GrafoCargado;
import ec.edu.epn.eda2.juez.core.parser.LectorReferenciaTxt;
import ec.edu.epn.eda2.juez.core.parser.ReferenciaCaso;
import ec.edu.epn.eda2.juez.core.parser.ReferenciaCaso.EstadoTopologico;
import ec.edu.epn.eda2.juez.submission.ResultadoBFS;
import ec.edu.epn.eda2.juez.submission.ResultadoMST;
import ec.edu.epn.eda2.juez.submission.ResultadoTopologico;
import ec.edu.epn.eda2.juez.submission.SolucionEstudiante;
import ec.edu.epn.eda2.juez.validador.ValidadorPendiente;
import ec.edu.epn.eda2.juez.validador.ValidadorResultado;
import ec.edu.epn.eda2.juez.validador.Veredicto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ec.edu.epn.eda2.juez.core.MiniPrueba.carpetaTemporal;
import static ec.edu.epn.eda2.juez.core.MiniPrueba.exigir;
import static ec.edu.epn.eda2.juez.core.MiniPrueba.probar;
import static ec.edu.epn.eda2.juez.core.MiniPrueba.silenciando;

//Pruebas del juez completo: parser + ejecucion + medicion + reporte
//+ comando juzgar, usando soluciones y un validador DE PRUEBA.
//
//IMPORTANTE: las soluciones y el validador de esta clase son simulacros
//para comprobar el cableado del core. No son la submission de ejemplo
//ni el validador real (eso lo entrega el Estudiante 2). Aqui la
//"solucion" devuelve las respuestas del archivo de referencia y el
//"validador" las compara de forma exacta.
public final class PruebaEjecutorJuez {

    //Sin calentamiento y una repeticion: las pruebas van rapido.
    private static final ConfiguracionJuez RAPIDA =
            ConfiguracionJuez.porDefecto()
                .conCalentamientos(0).conRepeticiones(1);

    //Impide crear instancias de esta clase de prueba.
    private PruebaEjecutorJuez() {
    }

    //----------------------------------------------------------------
    //Soluciones de prueba (publicas para poder cargarlas por nombre).
    //----------------------------------------------------------------

    //Devuelve las respuestas de la referencia. Para el MST entrega una
    //arista ficticia con el peso minimo (no es un MST real).
    public static class SolucionEco implements SolucionEstudiante {

        @Override
        public int[] dfs(GrafoCargado grafo, int inicio) {
            return referencia(grafo).dfsOrden();
        }

        @Override
        public ResultadoBFS bfs(GrafoCargado grafo, int inicio) {

            ReferenciaCaso ref = referencia(grafo);
            return new ResultadoBFS(ref.bfsOrden(), ref.bfsDistancias());
        }

        @Override
        public ResultadoTopologico ordenTopologico(GrafoCargado grafo) {

            ReferenciaCaso ref = referencia(grafo);

            return (ref.topoEstado() == EstadoTopologico.CICLO)
                    ? ResultadoTopologico.conCiclo()
                    : ResultadoTopologico.aciclico(ref.topoOrden());
        }

        @Override
        public ResultadoMST mst(GrafoCargado grafo) {

            long peso = referencia(grafo).mstPeso();

            return (peso == 0)
                    ? new ResultadoMST(List.of())
                    : new ResultadoMST(
                        List.of(new Arista(0, 1, (int) peso)));
        }
    }

    //Igual que la eco, pero con el DFS al reves (incorrecto).
    public static class SolucionDfsInvertido extends SolucionEco {

        @Override
        public int[] dfs(GrafoCargado grafo, int inicio) {

            int[] orden = super.dfs(grafo, inicio);
            int[] invertido = new int[orden.length];

            for (int i = 0; i < orden.length; i++) {
                invertido[i] = orden[orden.length - 1 - i];
            }

            return invertido;
        }
    }

    //Todos los metodos lanzan una excepcion.
    public static class SolucionQueFalla implements SolucionEstudiante {

        @Override
        public int[] dfs(GrafoCargado grafo, int inicio) {
            throw new IllegalStateException("fallo intencional");
        }

        @Override
        public ResultadoBFS bfs(GrafoCargado grafo, int inicio) {
            throw new UnsupportedOperationException("fallo intencional");
        }

        @Override
        public ResultadoTopologico ordenTopologico(GrafoCargado grafo) {
            throw new IllegalStateException("fallo intencional");
        }

        @Override
        public ResultadoMST mst(GrafoCargado grafo) {
            throw new IllegalStateException("fallo intencional");
        }
    }

    //Todos los metodos devuelven null.
    public static class SolucionNula implements SolucionEstudiante {

        @Override
        public int[] dfs(GrafoCargado grafo, int inicio) {
            return null;
        }

        @Override
        public ResultadoBFS bfs(GrafoCargado grafo, int inicio) {
            return null;
        }

        @Override
        public ResultadoTopologico ordenTopologico(GrafoCargado grafo) {
            return null;
        }

        @Override
        public ResultadoMST mst(GrafoCargado grafo) {
            return null;
        }
    }

    //Igual que la eco, pero el DFS no termina nunca.
    public static class SolucionLenta extends SolucionEco {

        @Override
        public int[] dfs(GrafoCargado grafo, int inicio) {

            while (!Thread.currentThread().isInterrupted()) {
                //Espera activa hasta que el juez interrumpa.
            }

            return new int[0];
        }
    }

    //Clase valida pero sin constructor publico.
    public static class SinConstructorPublico extends SolucionEco {

        private SinConstructorPublico() {
        }
    }

    //----------------------------------------------------------------
    //Validadores de prueba.
    //----------------------------------------------------------------

    //Compara de forma exacta con la referencia (solo para pruebas:
    //un validador real debe aceptar todas las respuestas validas).
    public static class ValidadorExacto implements ValidadorResultado {

        @Override
        public Veredicto validarDfs(
                GrafoCargado grafo, ReferenciaCaso ref, int[] orden) {

            return Arrays.equals(orden, ref.dfsOrden())
                    ? Veredicto.correcto()
                    : Veredicto.incorrecto("DFS distinto al esperado.");
        }

        @Override
        public Veredicto validarBfs(
                GrafoCargado grafo,
                ReferenciaCaso ref,
                ResultadoBFS resultado) {

            return Arrays.equals(resultado.orden(), ref.bfsOrden())
                    && Arrays.equals(resultado.distancias(),
                                     ref.bfsDistancias())
                    ? Veredicto.correcto()
                    : Veredicto.incorrecto("BFS distinto al esperado.");
        }

        @Override
        public Veredicto validarTopologico(
                GrafoCargado grafo,
                ReferenciaCaso ref,
                ResultadoTopologico resultado) {

            boolean ciclo = ref.topoEstado() == EstadoTopologico.CICLO;

            if (resultado.hayCiclo() != ciclo) {
                return Veredicto.incorrecto("Estado de ciclo erroneo.");
            }

            return (ciclo
                    || Arrays.equals(resultado.orden(), ref.topoOrden()))
                    ? Veredicto.correcto()
                    : Veredicto.incorrecto("Orden distinto al esperado.");
        }

        @Override
        public Veredicto validarMst(
                GrafoCargado grafo,
                ReferenciaCaso ref,
                ResultadoMST resultado) {

            return resultado.pesoTotal() == ref.mstPeso()
                    ? Veredicto.correcto()
                    : Veredicto.incorrecto("Peso del MST erroneo.");
        }
    }

    //Un validador con un error propio: siempre lanza excepcion.
    public static class ValidadorQueFalla implements ValidadorResultado {

        @Override
        public Veredicto validarDfs(
                GrafoCargado grafo, ReferenciaCaso ref, int[] orden) {
            throw new IllegalStateException("bug del validador");
        }

        @Override
        public Veredicto validarBfs(
                GrafoCargado grafo,
                ReferenciaCaso ref,
                ResultadoBFS resultado) {
            throw new IllegalStateException("bug del validador");
        }

        @Override
        public Veredicto validarTopologico(
                GrafoCargado grafo,
                ReferenciaCaso ref,
                ResultadoTopologico resultado) {
            throw new IllegalStateException("bug del validador");
        }

        @Override
        public Veredicto validarMst(
                GrafoCargado grafo,
                ReferenciaCaso ref,
                ResultadoMST resultado) {
            throw new IllegalStateException("bug del validador");
        }
    }

    //----------------------------------------------------------------
    //Pruebas.
    //----------------------------------------------------------------

    public static void main(String[] args) throws Exception {

        System.out.println("PRUEBAS DEL JUEZ (EJECUCION Y REPORTE)");
        System.out.println("--------------------------------");

        List<CasoPrueba> funcionales = CatalogoCasos.descubrir(
            CatalogoCasos.ENTRADA_FUNCIONAL,
            CatalogoCasos.REFERENCIA_FUNCIONAL
        );

        List<CasoPrueba> rendimiento = CatalogoCasos.descubrir(
            CatalogoCasos.ENTRADA_RENDIMIENTO,
            CatalogoCasos.REFERENCIA_RENDIMIENTO
        );

        pruebasDeJuzgamiento(funcionales, rendimiento);
        pruebasDeFallos(funcionales);
        pruebasDeCasosInvalidos(funcionales);
        pruebasDeReporte(funcionales);
        pruebasDeComando();

        MiniPrueba.terminar();
    }

    //Flujo normal: correcto, sin validar y rendimiento.
    private static void pruebasDeJuzgamiento(
            List<CasoPrueba> funcionales,
            List<CasoPrueba> rendimiento) {

        probar("El catalogo lista 13 + 5 casos en orden", () -> {

            exigir(funcionales.size() == 13, "funcionales");
            exigir(rendimiento.size() == 5, "rendimiento");
            exigir(funcionales.get(0).nombre().equals("01_conexo"),
                "primero: " + funcionales.get(0).nombre());
            exigir(funcionales.get(12).nombre()
                    .equals("13_ciclo_aleatorio"),
                "ultimo: " + funcionales.get(12).nombre());
        });

        probar("Solucion correcta: 39 CORRECTO y 13 NO_APLICA", () -> {

            ReporteJuez r = new EjecutorJuez(
                new SolucionEco(), new ValidadorExacto(), RAPIDA
            ).juzgar(funcionales);

            //10 no dirigidos y 3 dirigidos, 4 algoritmos por caso.
            exigir(r.resultados().size() == 52,
                "filas: " + r.resultados().size());
            exigir(r.contar(EstadoEjecucion.CORRECTO) == 39,
                "correctos: " + r.contar(EstadoEjecucion.CORRECTO));
            exigir(r.contar(EstadoEjecucion.NO_APLICA) == 13,
                "no aplica: " + r.contar(EstadoEjecucion.NO_APLICA));
            exigir(!r.hayFallos(), "no debia haber fallos");

            for (ResultadoEjecucion fila : r.resultados()) {

                if (fila.estado() == EstadoEjecucion.CORRECTO) {
                    exigir(fila.medianaNs() >= 0 && fila.minimoNs() >= 0,
                        "falta tiempo en " + fila);
                } else {
                    exigir(fila.medianaNs() == -1,
                        "NO_APLICA no se mide: " + fila);
                }
            }
        });

        probar("TOPOLOGICO solo en dirigidos y MST solo en no dirigidos",
            () -> {

            ReporteJuez r = new EjecutorJuez(
                new SolucionEco(), new ValidadorExacto(), RAPIDA
            ).juzgar(funcionales);

            for (ResultadoEjecucion fila : r.resultados()) {

                boolean dirigido = List.of(
                    "03_dag", "04_ciclo_dirigido", "09_dag_desconexo"
                ).contains(fila.caso());

                switch (fila.algoritmo()) {

                    case TOPOLOGICO -> exigir(
                        (fila.estado() == EstadoEjecucion.NO_APLICA)
                            == !dirigido,
                        "TOPOLOGICO en " + fila.caso());

                    case MST -> exigir(
                        (fila.estado() == EstadoEjecucion.NO_APLICA)
                            == dirigido,
                        "MST en " + fila.caso());

                    default -> exigir(
                        fila.estado() == EstadoEjecucion.CORRECTO,
                        fila.algoritmo() + " en " + fila.caso());
                }
            }
        });

        probar("Validador pendiente: SIN_VALIDAR y sin fallos", () -> {

            ReporteJuez r = new EjecutorJuez(
                new SolucionEco(), new ValidadorPendiente(), RAPIDA
            ).juzgar(funcionales);

            exigir(r.contar(EstadoEjecucion.SIN_VALIDAR) == 39,
                "sin validar: " + r.contar(EstadoEjecucion.SIN_VALIDAR));
            exigir(r.contar(EstadoEjecucion.CORRECTO) == 0,
                "no debe haber CORRECTO sin validar");
            exigir(!r.hayFallos(), "SIN_VALIDAR no es fallo");

            for (ResultadoEjecucion fila : r.resultados()) {
                if (fila.estado() == EstadoEjecucion.SIN_VALIDAR) {
                    exigir(fila.medianaNs() >= 0, "tiempo en " + fila);
                }
            }
        });

        probar("Casos de rendimiento: 15 CORRECTO y 5 NO_APLICA", () -> {

            ReporteJuez r = new EjecutorJuez(
                new SolucionEco(), new ValidadorExacto(), RAPIDA
            ).juzgar(rendimiento);

            exigir(r.resultados().size() == 20,
                "filas: " + r.resultados().size());
            exigir(r.contar(EstadoEjecucion.CORRECTO) == 15,
                "correctos: " + r.contar(EstadoEjecucion.CORRECTO));
            exigir(r.contar(EstadoEjecucion.NO_APLICA) == 5,
                "no aplica: " + r.contar(EstadoEjecucion.NO_APLICA));
        });

        probar("Respuesta incorrecta: INCORRECTO y la corrida falla", () -> {

            ReporteJuez r = new EjecutorJuez(
                new SolucionDfsInvertido(), new ValidadorExacto(), RAPIDA
            ).juzgar(funcionales);

            ResultadoEjecucion dfs = buscar(r, "01_conexo", "DFS");

            exigir(dfs.estado() == EstadoEjecucion.INCORRECTO,
                "estado: " + dfs.estado());
            exigir(!dfs.detalle().isEmpty(), "falta el motivo");
            exigir(r.hayFallos(), "debia haber fallos");

            //Lo demas sigue bien: el error de un metodo no afecta al resto.
            exigir(buscar(r, "01_conexo", "BFS").estado()
                    == EstadoEjecucion.CORRECTO,
                "BFS debia seguir correcto");
            exigir(buscar(r, "01_conexo", "MST").estado()
                    == EstadoEjecucion.CORRECTO,
                "MST debia seguir correcto");
        });
    }

    //Errores de la solucion y del validador.
    private static void pruebasDeFallos(List<CasoPrueba> funcionales) {

        probar("Excepciones de la solucion: ERROR_EJECUCION", () -> {

            ReporteJuez r = new EjecutorJuez(
                new SolucionQueFalla(), new ValidadorExacto(), RAPIDA
            ).juzgar(funcionales);

            exigir(r.contar(EstadoEjecucion.ERROR_EJECUCION) == 39,
                "errores: " + r.contar(EstadoEjecucion.ERROR_EJECUCION));
            exigir(buscar(r, "01_conexo", "DFS").detalle()
                    .contains("fallo intencional"),
                "el detalle debe incluir el mensaje");
            exigir(r.hayFallos(), "debia haber fallos");
        });

        probar("Devolver null: ERROR_EJECUCION", () -> {

            ReporteJuez r = new EjecutorJuez(
                new SolucionNula(), new ValidadorExacto(), RAPIDA
            ).juzgar(funcionales);

            exigir(r.contar(EstadoEjecucion.ERROR_EJECUCION) == 39,
                "errores: " + r.contar(EstadoEjecucion.ERROR_EJECUCION));
            exigir(buscar(r, "01_conexo", "BFS").detalle()
                    .contains("null"),
                "el detalle debe mencionar null");
        });

        probar("Solucion que no termina: TIMEOUT en ese metodo", () -> {

            List<CasoPrueba> uno =
                    CatalogoCasos.filtrar(funcionales, "01_conexo");

            exigir(uno.size() == 1, "filtro");

            long inicio = System.nanoTime();

            ReporteJuez r = new EjecutorJuez(
                new SolucionLenta(), new ValidadorExacto(),
                RAPIDA.conTimeoutMs(300)
            ).juzgar(uno);

            long milisegundos = (System.nanoTime() - inicio) / 1_000_000;

            exigir(buscar(r, "01_conexo", "DFS").estado()
                    == EstadoEjecucion.TIMEOUT,
                "DFS debia ser TIMEOUT");
            exigir(buscar(r, "01_conexo", "BFS").estado()
                    == EstadoEjecucion.CORRECTO,
                "BFS debia seguir correcto");
            exigir(milisegundos < 5_000,
                "el juez tardo " + milisegundos + " ms");
            exigir(r.hayFallos(), "TIMEOUT es un fallo");
        });

        probar("Error del validador: ERROR_VALIDADOR", () -> {

            ReporteJuez r = new EjecutorJuez(
                new SolucionEco(), new ValidadorQueFalla(), RAPIDA
            ).juzgar(funcionales);

            exigir(r.contar(EstadoEjecucion.ERROR_VALIDADOR) == 39,
                "errores: " + r.contar(EstadoEjecucion.ERROR_VALIDADOR));
            exigir(buscar(r, "01_conexo", "DFS").detalle()
                    .contains("bug del validador"),
                "el detalle debe incluir el mensaje");
            exigir(buscar(r, "01_conexo", "DFS").medianaNs() >= 0,
                "el tiempo se conserva aunque falle el validador");
        });
    }

    //TXT mal formados o referencias faltantes no detienen al juez.
    private static void pruebasDeCasosInvalidos(
            List<CasoPrueba> funcionales) {

        probar("Casos invalidos: ERROR_CASO sin detener al resto", () -> {

            Path carpeta = carpetaTemporal();
            Path origen = CatalogoCasos.ENTRADA_FUNCIONAL;
            Path refs = CatalogoCasos.REFERENCIA_FUNCIONAL;

            //1. TXT con aristas faltantes.
            Files.writeString(carpeta.resolve("malo.txt"),
                "U 3 5\n0 1 4\n");
            Files.writeString(carpeta.resolve("malo_ref.txt"), "x");

            //2. Entrada valida sin archivo de referencia.
            Files.copy(origen.resolve("01_conexo.txt"),
                carpeta.resolve("sin_ref.txt"));

            //3. Referencia de otro caso (CASO no coincide).
            Files.copy(origen.resolve("01_conexo.txt"),
                carpeta.resolve("cruzado.txt"));
            Files.copy(refs.resolve("01_conexo.txt"),
                carpeta.resolve("cruzado_ref.txt"));

            //4. Referencia que no concuerda con el grafo.
            Files.writeString(carpeta.resolve("incoh.txt"),
                "U 3 2\n0 1 4\n1 2 7\n");
            Files.writeString(carpeta.resolve("incoh_ref.txt"),
                String.join("\n",
                    "FORMATO=REFERENCIA_V1", "CASO=incoh", "INICIO=0",
                    "BFS_ORDEN=0", "BFS_DISTANCIAS=0", "DFS_ORDEN=0",
                    "TOPO_ESTADO=NO_APLICA", "TOPO_ORDEN=NO_APLICA",
                    "MST_PESO=11", "MST_COMPONENTES=1") + "\n");

            List<CasoPrueba> casos = new ArrayList<>();
            casos.add(new CasoPrueba("malo",
                carpeta.resolve("malo.txt"),
                carpeta.resolve("malo_ref.txt")));
            casos.add(new CasoPrueba("sin_ref",
                carpeta.resolve("sin_ref.txt"),
                carpeta.resolve("no_existe.txt")));
            casos.add(new CasoPrueba("cruzado",
                carpeta.resolve("cruzado.txt"),
                carpeta.resolve("cruzado_ref.txt")));
            casos.add(new CasoPrueba("incoh",
                carpeta.resolve("incoh.txt"),
                carpeta.resolve("incoh_ref.txt")));
            casos.add(funcionales.get(0));

            ReporteJuez r = new EjecutorJuez(
                new SolucionEco(), new ValidadorExacto(), RAPIDA
            ).juzgar(casos);

            exigir(r.contar(EstadoEjecucion.ERROR_CASO) == 4,
                "casos con error: "
                + r.contar(EstadoEjecucion.ERROR_CASO));

            for (ResultadoEjecucion fila : r.resultados()) {

                if (fila.estado() == EstadoEjecucion.ERROR_CASO) {
                    exigir(fila.algoritmo() == null,
                        "el error es del caso completo: " + fila);
                    exigir(!fila.detalle().isEmpty(),
                        "falta el motivo: " + fila);
                }
            }

            //El caso valido se juzgo normalmente: 3 correctos + 1 N/A.
            exigir(r.contar(EstadoEjecucion.CORRECTO) == 3,
                "correctos: " + r.contar(EstadoEjecucion.CORRECTO));
            exigir(r.hayFallos(), "ERROR_CASO es un fallo");
        });
    }

    //Salida por consola y exportacion CSV.
    private static void pruebasDeReporte(List<CasoPrueba> funcionales) {

        probar("El reporte muestra estado y tiempo por algoritmo", () -> {

            ReporteJuez r = new EjecutorJuez(
                new SolucionEco(), new ValidadorExacto(), RAPIDA
            ).juzgar(funcionales);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            r.imprimir(new PrintStream(buffer, true,
                StandardCharsets.UTF_8));

            String texto = buffer.toString(StandardCharsets.UTF_8);

            exigir(texto.contains("MEDIANA(ms)"), "falta la columna tiempo");
            exigir(texto.contains("MEM(KB)"), "falta la columna memoria");
            exigir(texto.contains("01_conexo"), "falta el caso");
            exigir(texto.contains("CORRECTO"), "falta CORRECTO");
            exigir(texto.contains("NO_APLICA"), "falta NO_APLICA");
            exigir(texto.contains("39 correctos"), "falta el resumen");
            exigir(texto.contains("RESULTADO: TODO CORRECTO"),
                "falta el resultado global");
            exigir(texto.chars().allMatch(c -> c < 128),
                "la salida debe ser ASCII para PowerShell");
        });

        probar("El resultado global distingue fallos y sin validar", () -> {

            ByteArrayOutputStream a = new ByteArrayOutputStream();
            new EjecutorJuez(new SolucionDfsInvertido(),
                    new ValidadorExacto(), RAPIDA)
                .juzgar(funcionales)
                .imprimir(new PrintStream(a, true,
                    StandardCharsets.UTF_8));

            exigir(a.toString(StandardCharsets.UTF_8)
                    .contains("RESULTADO: CON FALLOS"),
                "debia decir CON FALLOS");

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            new EjecutorJuez(new SolucionEco(),
                    new ValidadorPendiente(), RAPIDA)
                .juzgar(funcionales)
                .imprimir(new PrintStream(b, true,
                    StandardCharsets.UTF_8));

            exigir(b.toString(StandardCharsets.UTF_8)
                    .contains("RESULTADO: SIN VALIDAR"),
                "debia decir SIN VALIDAR");
        });

        probar("Exporta CSV con encabezado y una fila por resultado", () -> {

            ReporteJuez r = new EjecutorJuez(
                new SolucionQueFalla(), new ValidadorExacto(), RAPIDA
            ).juzgar(funcionales);

            Path csv = carpetaTemporal().resolve("sub").resolve("r.csv");
            r.exportarCsv(csv);

            List<String> lineas = Files.readAllLines(csv);

            exigir(lineas.size() == 53,
                "lineas: " + lineas.size());
            exigir(lineas.get(0).equals("caso,vertices,aristas,algoritmo,"
                    + "estado,mediana_ns,minimo_ns,memoria_bytes,detalle"),
                "encabezado: " + lineas.get(0));
            exigir(lineas.get(1).startsWith("01_conexo,5,7,DFS,"
                    + "ERROR_EJECUCION,-1,-1,-1,\""),
                "primera fila: " + lineas.get(1));
        });
    }

    //Comando "juzgar" y carga de clases por nombre.
    private static void pruebasDeComando() {

        String eco = SolucionEco.class.getName();
        String exacto = ValidadorExacto.class.getName();

        probar("juzgar: solucion correcta devuelve 0 y guarda el CSV", () -> {

            Path csv = carpetaTemporal().resolve("juez.csv");

            int codigo = silenciando(() -> JuezCli.ejecutar(new String[] {
                eco, "--validador", exacto,
                "--calentamientos", "0", "--repeticiones", "1",
                "--csv", csv.toString()
            }));

            exigir(codigo == 0, "codigo de salida " + codigo);
            exigir(Files.readAllLines(csv).size() == 53, "filas del CSV");
        });

        probar("juzgar: solucion incorrecta devuelve 1", () -> {

            int codigo = silenciando(() -> JuezCli.ejecutar(new String[] {
                SolucionDfsInvertido.class.getName(),
                "--validador", exacto,
                "--calentamientos", "0", "--repeticiones", "1"
            }));

            exigir(codigo == 1, "codigo de salida " + codigo);
        });

        probar("juzgar: con ValidadorPendiente explicito devuelve 0", () -> {

            int codigo = silenciando(() -> JuezCli.ejecutar(new String[] {
                eco, "--validador", ValidadorPendiente.class.getName(),
                "--calentamientos", "0", "--repeticiones", "1"
            }));

            exigir(codigo == 0, "codigo de salida " + codigo);
        });

        probar("El validador por defecto se puede instanciar", () -> {

            ValidadorResultado v = JuezCli.instanciar(
                JuezCli.VALIDADOR_POR_DEFECTO, ValidadorResultado.class
            );

            exigir(v != null, "no se creo el validador por defecto");
        });

        probar("juzgar: --casos rendimiento y --filtro", () -> {

            int codigo = silenciando(() -> JuezCli.ejecutar(new String[] {
                eco, "--validador", exacto, "--casos", "rendimiento",
                "--filtro", "100_", "--calentamientos", "0",
                "--repeticiones", "1"
            }));

            exigir(codigo == 0, "codigo de salida " + codigo);
        });

        probar("juzgar: --entrada y --referencia propias", () -> {

            int codigo = silenciando(() -> JuezCli.ejecutar(new String[] {
                eco, "--validador", exacto,
                "--entrada", "casos/entrada",
                "--referencia", "casos/referencia",
                "--filtro", "03_dag", "--calentamientos", "0",
                "--repeticiones", "1"
            }));

            exigir(codigo == 0, "codigo de salida " + codigo);
        });

        rechazarComando("Sin argumentos");
        rechazarComando("Clase inexistente", "no.existe.Clase");
        rechazarComando("Clase que no implementa la interfaz",
            "java.lang.String");
        rechazarComando("Clase sin constructor publico",
            SinConstructorPublico.class.getName());
        rechazarComando("Validador inexistente",
            eco, "--validador", "no.existe.Validador");
        rechazarComando("Opcion desconocida", eco, "--rapido");
        rechazarComando("Falta el valor de una opcion",
            eco, "--validador");
        rechazarComando("Entero invalido", eco, "--repeticiones", "abc");
        rechazarComando("Repeticiones en cero", eco, "--repeticiones", "0");
        rechazarComando("Conjunto de casos invalido",
            eco, "--casos", "otros");
        rechazarComando("Filtro sin resultados",
            eco, "--filtro", "zzz_no_existe");
        rechazarComando("--entrada sin --referencia",
            eco, "--entrada", "casos/entrada");
        rechazarComando("Carpeta de entrada inexistente",
            eco, "--entrada", "no_existe", "--referencia", "tampoco");
    }

    //El comando debe rechazar los argumentos invalidos con un mensaje.
    private static void rechazarComando(String nombre, String... args) {

        probar("juzgar rechaza: " + nombre, () -> {

            try {
                silenciando(() -> JuezCli.ejecutar(args));
            } catch (IllegalArgumentException e) {

                exigir(e.getMessage() != null
                        && !e.getMessage().isBlank(),
                    "el mensaje no debe estar vacio");

                return;
            }

            throw new AssertionError("se acepto un argumento invalido");
        });
    }

    //Busca la fila de un caso y algoritmo.
    private static ResultadoEjecucion buscar(
            ReporteJuez reporte, String caso, String algoritmo) {

        for (ResultadoEjecucion fila : reporte.resultados()) {

            if (fila.caso().equals(caso)
                    && fila.algoritmo() != null
                    && fila.algoritmo().name().equals(algoritmo)) {

                return fila;
            }
        }

        throw new AssertionError(
            "no hay fila para " + caso + " / " + algoritmo
        );
    }

    //Carga la referencia de un caso (funcional o de rendimiento).
    private static ReferenciaCaso referencia(GrafoCargado grafo) {

        try {
            Path funcional = CatalogoCasos.REFERENCIA_FUNCIONAL
                    .resolve(grafo.nombre() + ".txt");

            Path ruta = Files.exists(funcional)
                    ? funcional
                    : CatalogoCasos.REFERENCIA_RENDIMIENTO
                        .resolve(grafo.nombre() + ".txt");

            return LectorReferenciaTxt.leer(ruta);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

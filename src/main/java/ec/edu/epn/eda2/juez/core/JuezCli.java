package ec.edu.epn.eda2.juez.core;

import ec.edu.epn.eda2.juez.core.ejecucion.CasoPrueba;
import ec.edu.epn.eda2.juez.core.ejecucion.CatalogoCasos;
import ec.edu.epn.eda2.juez.core.ejecucion.ConfiguracionJuez;
import ec.edu.epn.eda2.juez.core.ejecucion.EjecutorJuez;
import ec.edu.epn.eda2.juez.core.ejecucion.ReporteJuez;
import ec.edu.epn.eda2.juez.submission.SolucionEstudiante;
import ec.edu.epn.eda2.juez.validador.ValidadorResultado;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

//Comando "juzgar" del programa: carga la solucion de un estudiante
//por su nombre de clase, la ejecuta sobre los casos y muestra el
//reporte.
//
//Uso:
//  juzgar <ClaseSolucion> [opciones]
//
//Codigo de salida: 0 si no hay fallos; 1 si hay respuestas
//incorrectas, errores o timeouts. SIN_VALIDAR no cuenta como fallo.
public final class JuezCli {

    //Validador que se usa cuando no se indica --validador.
    //Validador real (ValidadorGrafos). Para volver al provisional use
    //--validador ec.edu.epn.eda2.juez.validador.ValidadorPendiente.
    public static final String VALIDADOR_POR_DEFECTO =
            "ec.edu.epn.eda2.juez.validador.ValidadorGrafos";

    //Texto de ayuda del comando.
    public static final String USO = String.join("\n",
        "Uso: juzgar <ClaseSolucion> [opciones]",
        "",
        "  <ClaseSolucion>  nombre completo de una clase publica que",
        "                   implemente SolucionEstudiante",
        "",
        "Opciones:",
        "  --validador <Clase>     validador a usar (por defecto el",
        "                          de JuezCli.VALIDADOR_POR_DEFECTO)",
        "  --casos <conjunto>      funcional (por defecto), rendimiento",
        "                          o todos",
        "  --entrada <carpeta>     carpeta de TXT de entrada propia",
        "  --referencia <carpeta>  carpeta de referencias propia",
        "                          (--entrada y --referencia van juntas)",
        "  --filtro <texto>        solo casos cuyo nombre contenga texto",
        "  --repeticiones <n>      repeticiones medidas (por defecto 5)",
        "  --calentamientos <n>    ejecuciones descartadas (por defecto 2)",
        "  --timeout-ms <n>        limite por ejecucion (por defecto 10000)",
        "  --pila-mb <n>           pila del hilo de la solucion (por",
        "                          defecto 16)",
        "  --csv <archivo>         guarda ademas el reporte como CSV"
    );

    //Impide crear instancias de esta clase utilitaria.
    private JuezCli() {
    }

    //Ejecuta el comando con los argumentos posteriores a "juzgar".
    public static int ejecutar(String[] args) throws IOException {

        if (args.length == 0) {
            throw new IllegalArgumentException(USO);
        }

        String claseSolucion = args[0];
        String claseValidador = null;
        String conjunto = "funcional";
        Path entrada = null;
        Path referencia = null;
        String filtro = null;
        Path csv = null;
        ConfiguracionJuez config = ConfiguracionJuez.porDefecto();

        for (int i = 1; i < args.length; i++) {

            String opcion = args[i];

            switch (opcion) {

                case "--validador" ->
                    claseValidador = valor(args, ++i, opcion);

                case "--casos" ->
                    conjunto = valor(args, ++i, opcion);

                case "--entrada" ->
                    entrada = Path.of(valor(args, ++i, opcion));

                case "--referencia" ->
                    referencia = Path.of(valor(args, ++i, opcion));

                case "--filtro" ->
                    filtro = valor(args, ++i, opcion);

                case "--csv" ->
                    csv = Path.of(valor(args, ++i, opcion));

                case "--repeticiones" ->
                    config = config.conRepeticiones(
                        entero(valor(args, ++i, opcion), opcion));

                case "--calentamientos" ->
                    config = config.conCalentamientos(
                        entero(valor(args, ++i, opcion), opcion));

                case "--timeout-ms" ->
                    config = config.conTimeoutMs(
                        entero(valor(args, ++i, opcion), opcion));

                case "--pila-mb" ->
                    config = config.conPilaBytes(
                        entero(valor(args, ++i, opcion), opcion)
                        * 1024L * 1024L);

                default -> throw new IllegalArgumentException(
                    "Opcion desconocida: " + opcion + "\n" + USO);
            }
        }

        List<CasoPrueba> casos =
                elegirCasos(conjunto, entrada, referencia);

        if (filtro != null) {
            casos = CatalogoCasos.filtrar(casos, filtro);
        }

        if (casos.isEmpty()) {
            throw new IllegalArgumentException(
                "No se encontraron casos para juzgar."
            );
        }

        SolucionEstudiante solucion =
                instanciar(claseSolucion, SolucionEstudiante.class);

        ValidadorResultado validador = instanciar(
            (claseValidador == null)
                ? VALIDADOR_POR_DEFECTO
                : claseValidador,
            ValidadorResultado.class
        );

        System.out.println("PROYECTO 13 - JUEZ DE GRAFOS");
        System.out.println("Juzgando " + casos.size() + " casos...");
        System.out.println();

        ReporteJuez reporte =
                new EjecutorJuez(solucion, validador, config)
                        .juzgar(casos);

        reporte.imprimir(System.out);

        if (csv != null) {
            reporte.exportarCsv(csv);
            System.out.println("CSV guardado en: " + csv);
        }

        return reporte.hayFallos() ? 1 : 0;
    }

    //Decide que carpetas de casos se juzgan.
    private static List<CasoPrueba> elegirCasos(
            String conjunto,
            Path entrada,
            Path referencia) throws IOException {

        List<CasoPrueba> casos = new ArrayList<>();

        if (entrada != null || referencia != null) {

            if (entrada == null || referencia == null) {
                throw new IllegalArgumentException(
                    "--entrada y --referencia deben usarse juntas."
                );
            }

            casos.addAll(CatalogoCasos.descubrir(entrada, referencia));
            return casos;
        }

        switch (conjunto) {

            case "funcional" ->
                casos.addAll(CatalogoCasos.descubrir(
                    CatalogoCasos.ENTRADA_FUNCIONAL,
                    CatalogoCasos.REFERENCIA_FUNCIONAL));

            case "rendimiento" ->
                casos.addAll(CatalogoCasos.descubrir(
                    CatalogoCasos.ENTRADA_RENDIMIENTO,
                    CatalogoCasos.REFERENCIA_RENDIMIENTO));

            case "todos" -> {
                casos.addAll(CatalogoCasos.descubrir(
                    CatalogoCasos.ENTRADA_FUNCIONAL,
                    CatalogoCasos.REFERENCIA_FUNCIONAL));

                casos.addAll(CatalogoCasos.descubrir(
                    CatalogoCasos.ENTRADA_RENDIMIENTO,
                    CatalogoCasos.REFERENCIA_RENDIMIENTO));
            }

            default -> throw new IllegalArgumentException(
                "--casos debe ser funcional, rendimiento o todos.");
        }

        return casos;
    }

    //Crea un objeto a partir del nombre completo de su clase.
    public static <T> T instanciar(String nombreClase, Class<T> tipo) {

        Class<?> clase;

        try {
            clase = Class.forName(nombreClase);
        } catch (ClassNotFoundException | LinkageError e) {
            throw new IllegalArgumentException(
                "No se encontro la clase " + nombreClase
                + ". Use el nombre completo con paquete y compile "
                + "antes de ejecutar."
            );
        }

        if (!tipo.isAssignableFrom(clase)) {
            throw new IllegalArgumentException(
                "La clase " + nombreClase + " no implementa "
                + tipo.getSimpleName() + "."
            );
        }

        try {
            return tipo.cast(
                clase.getDeclaredConstructor().newInstance()
            );
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                "No se pudo crear " + nombreClase + ": la clase debe "
                + "ser publica y tener un constructor publico sin "
                + "argumentos (" + e + ")."
            );
        }
    }

    //Obtiene el valor que sigue a una opcion.
    private static String valor(
            String[] args, int indice, String opcion) {

        if (indice >= args.length) {
            throw new IllegalArgumentException(
                "Falta el valor de " + opcion + "."
            );
        }

        return args[indice];
    }

    //Convierte el valor de una opcion a entero.
    private static int entero(String texto, String opcion) {

        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                opcion + " requiere un entero y recibio '" + texto + "'."
            );
        }
    }
}

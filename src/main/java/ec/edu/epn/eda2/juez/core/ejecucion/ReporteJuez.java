package ec.edu.epn.eda2.juez.core.ejecucion;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//Reporte final del juez: tabla por caso y algoritmo con el estado
//(CORRECTO / INCORRECTO / ...) y el tiempo, mas un resumen.
public final class ReporteJuez {

    private final String solucion;
    private final String validador;
    private final ConfiguracionJuez config;
    private final List<ResultadoEjecucion> filas;

    //Crea el reporte con los resultados ya calculados.
    public ReporteJuez(
            String solucion,
            String validador,
            ConfiguracionJuez config,
            List<ResultadoEjecucion> filas) {

        this.solucion = solucion;
        this.validador = validador;
        this.config = config;
        this.filas = List.copyOf(filas);
    }

    //Todas las filas del reporte.
    public List<ResultadoEjecucion> resultados() {
        return filas;
    }

    //Cuenta las filas con un estado dado.
    public int contar(EstadoEjecucion estado) {

        int total = 0;

        for (ResultadoEjecucion fila : filas) {
            if (fila.estado() == estado) {
                total++;
            }
        }

        return total;
    }

    //True si alguna fila es un fallo (incorrecto, error o timeout).
    //SIN_VALIDAR y NO_APLICA no son fallos.
    public boolean hayFallos() {

        for (ResultadoEjecucion fila : filas) {
            if (fila.estado().esFallo()) {
                return true;
            }
        }

        return false;
    }

    //Imprime el reporte en consola (solo ASCII, para PowerShell).
    public void imprimir(PrintStream salida) {

        salida.println("JUEZ DE GRAFOS - REPORTE");
        salida.println("Solucion : " + solucion);
        salida.println("Validador: " + validador);
        salida.println(
            "Medicion : " + config.calentamientos()
            + " calentamientos + " + config.repeticiones()
            + " repeticiones | limite " + config.timeoutMs()
            + " ms | Java " + System.getProperty("java.version")
        );
        salida.println(
            "Tiempo   : solo la llamada a la solucion "
            + "(sin lectura ni validacion)"
        );
        salida.println();

        int ancho = "CASO".length();

        for (ResultadoEjecucion fila : filas) {
            ancho = Math.max(ancho, fila.caso().length());
        }

        String formato = "%-" + ancho + "s %6s %8s  %-10s %-15s %12s %12s"
                + " %12s  %s";

        salida.println(String.format(Locale.ROOT, formato,
            "CASO", "V", "E", "ALGORITMO", "ESTADO",
            "MEDIANA(ms)", "MIN(ms)", "MEM(KB)", "DETALLE"));

        for (ResultadoEjecucion fila : filas) {

            boolean conGrafo = fila.algoritmo() != null;

            salida.println(String.format(Locale.ROOT, formato,
                fila.caso(),
                conGrafo ? String.valueOf(fila.vertices()) : "-",
                conGrafo ? String.valueOf(fila.aristas()) : "-",
                conGrafo ? fila.algoritmo().name() : "-",
                fila.estado().name(),
                milisegundos(fila.medianaNs()),
                milisegundos(fila.minimoNs()),
                kilobytes(fila.memoriaBytes()),
                fila.detalle()));
        }

        salida.println();
        salida.println(
            "RESUMEN: "
            + contar(EstadoEjecucion.CORRECTO) + " correctos | "
            + contar(EstadoEjecucion.INCORRECTO) + " incorrectos | "
            + contar(EstadoEjecucion.SIN_VALIDAR) + " sin validar | "
            + contar(EstadoEjecucion.NO_APLICA) + " no aplica | "
            + contar(EstadoEjecucion.ERROR_EJECUCION)
            + " errores de ejecucion | "
            + contar(EstadoEjecucion.TIMEOUT) + " timeout | "
            + contar(EstadoEjecucion.ERROR_VALIDADOR)
            + " errores del validador | "
            + contar(EstadoEjecucion.ERROR_CASO) + " casos con error"
        );

        salida.println("RESULTADO: " + veredictoGlobal());
    }

    //Guarda el reporte como CSV (tiempos en nanosegundos).
    public void exportarCsv(Path archivo) throws IOException {

        List<String> lineas = new ArrayList<>();

        lineas.add("caso,vertices,aristas,algoritmo,estado,"
            + "mediana_ns,minimo_ns,memoria_bytes,detalle");

        for (ResultadoEjecucion fila : filas) {

            lineas.add(String.join(",",
                fila.caso(),
                String.valueOf(fila.vertices()),
                String.valueOf(fila.aristas()),
                fila.algoritmo() == null ? "" : fila.algoritmo().name(),
                fila.estado().name(),
                String.valueOf(fila.medianaNs()),
                String.valueOf(fila.minimoNs()),
                String.valueOf(fila.memoriaBytes()),
                campoCsv(fila.detalle())));
        }

        if (archivo.getParent() != null) {
            Files.createDirectories(archivo.getParent());
        }

        Files.write(archivo, lineas, StandardCharsets.UTF_8);
    }

    //Resume el resultado de toda la corrida en una frase.
    private String veredictoGlobal() {

        if (hayFallos()) {
            return "CON FALLOS";
        }

        if (contar(EstadoEjecucion.SIN_VALIDAR) > 0) {
            return "SIN VALIDAR (el validador esta pendiente)";
        }

        return "TODO CORRECTO";
    }

    //Convierte nanosegundos a milisegundos con 3 decimales.
    private static String milisegundos(long nanos) {

        if (nanos < 0) {
            return "-";
        }

        return String.format(Locale.ROOT, "%.3f", nanos / 1_000_000.0);
    }

    //Convierte bytes a KB con un decimal ("-" si no se midio).
    private static String kilobytes(long bytes) {

        if (bytes < 0) {
            return "-";
        }

        return String.format(Locale.ROOT, "%.1f", bytes / 1024.0);
    }

    //Escapa un campo de texto para CSV.
    private static String campoCsv(String texto) {
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }
}

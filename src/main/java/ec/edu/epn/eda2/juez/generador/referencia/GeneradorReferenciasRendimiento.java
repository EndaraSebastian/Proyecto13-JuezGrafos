package ec.edu.epn.eda2.juez.generador.referencia;

import ec.edu.epn.eda2.juez.core.modelo.Arista;
import ec.edu.epn.eda2.juez.core.modelo.CasoGrafo;
import ec.edu.epn.eda2.juez.generador.servicio.BateriaRendimiento;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

//Coordina la generacion de referencias para grafos grandes.
public final class GeneradorReferenciasRendimiento {

    //Define las carpetas utilizadas por el modulo.
    private static final Path CARPETA_ENTRADA =
            Path.of("casos", "rendimiento");

    private static final Path CARPETA_REFERENCIA =
            Path.of("casos", "referencias-rendimiento");

    //Evita crear instancias innecesarias.
    private GeneradorReferenciasRendimiento() {
    }

    //Genera las referencias y controla su sobrescritura.
    public static void generar(boolean sobrescribir)
            throws IOException {

        List<CasoGrafo> casos = BateriaRendimiento.crearBateria();

        int generados = 0;
        int omitidos = 0;

        System.out.println("REFERENCIAS DE RENDIMIENTO");
        System.out.println("--------------------------------");

        //Procesa individualmente cada grafo.
        for (CasoGrafo grafo : casos) {

            //Comprueba que el TXT coincida con el grafo original.
            verificarEntrada(grafo);

            Path archivo = CARPETA_REFERENCIA.resolve(
                    grafo.nombre() + ".txt"
            );

            //Conserva las referencias existentes por defecto.
            if (Files.exists(archivo) && !sobrescribir) {

                System.out.println("Omitido: " + grafo.nombre());
                omitidos++;
                continue;
            }

            //Calcula las respuestas utilizando los algoritmos.
            Map<String, String> referencia =
                    CalculadorReferencias.calcular(grafo);

            //Guarda los resultados en una carpeta independiente.
            EscritorReferenciaTxt.guardar(
                    grafo,
                    referencia,
                    CARPETA_REFERENCIA,
                    sobrescribir
            );

            System.out.println(
                    "Generado: " + grafo.nombre() + ".txt"
            );

            generados++;
        }

        System.out.println("--------------------------------");
        System.out.println("Generados: " + generados);
        System.out.println("Omitidos: " + omitidos);
    }

    //Comprueba que el TXT de entrada no haya sido modificado.
    private static void verificarEntrada(CasoGrafo grafo)
            throws IOException {

        Path archivo = CARPETA_ENTRADA.resolve(
                grafo.nombre() + ".txt"
        );

        if (!Files.isRegularFile(archivo)) {
            throw new IOException(
                    "No existe el grafo: " + archivo
            );
        }

        //Lee el archivo progresivamente para controlar memoria.
        try (BufferedReader lector = Files.newBufferedReader(
                archivo, StandardCharsets.UTF_8)) {

            String encabezado = grafo.tipo().codigo()
                    + " " + grafo.vertices()
                    + " " + grafo.aristas().size();

            if (!encabezado.equals(lector.readLine())) {
                throw new IOException(
                        "Encabezado diferente: " + archivo
                );
            }

            //Compara las conexiones con la bateria reproducible.
            for (Arista arista : grafo.aristas()) {

                String esperada = arista.origen()
                        + " " + arista.destino()
                        + " " + arista.peso();

                String actual = lector.readLine();

                if (!esperada.equals(actual)) {
                    throw new IOException(
                            "Arista diferente en: " + archivo
                    );
                }
            }

            //No deben existir conexiones adicionales.
            if (lector.readLine() != null) {
                throw new IOException(
                        "El archivo contiene datos adicionales."
                );
            }
        }
    }
}
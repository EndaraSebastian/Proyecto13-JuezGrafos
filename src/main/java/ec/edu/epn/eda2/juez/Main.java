package ec.edu.epn.eda2.juez;

import ec.edu.epn.eda2.juez.core.modelo.CasoGrafo;
import ec.edu.epn.eda2.juez.generador.io.EscritorGrafoTxt;
import ec.edu.epn.eda2.juez.generador.referencia.CalculadorReferencias;
import ec.edu.epn.eda2.juez.generador.referencia.EscritorReferenciaTxt;
import ec.edu.epn.eda2.juez.generador.servicio.BateriaCasos;
import ec.edu.epn.eda2.juez.generador.servicio.BateriaRendimiento;
import ec.edu.epn.eda2.juez.generador.referencia.GeneradorReferenciasRendimiento;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

//Punto de entrada principal del framework de grafos.
public final class Main {

    //Define las carpetas utilizadas por el sistema.
    private static final Path CARPETA_ENTRADA =
            Path.of("casos", "entrada");

    private static final Path CARPETA_REFERENCIA =
            Path.of("casos", "referencia");

    //Carpeta destinada a los casos de rendimiento.
    private static final Path CARPETA_RENDIMIENTO =
            Path.of("casos", "rendimiento");

    //Impide crear instancias innecesarias de Main.
    private Main() {
    }

    //Recibe y procesa los comandos del programa.
    public static void main(String[] args) {

        if (args.length == 0 || args[0].equalsIgnoreCase("ayuda")) {
            mostrarAyuda();
            return;
        }

        try {

            //Selecciona la funcionalidad solicitada.
            switch (args[0].toLowerCase()) {

                case "generar":
                    generarCasos();
                    break;

                case "rendimiento":
                    generarRendimiento();
                    break;

                case "referencias-rendimiento": {
                    //Comprueba los argumentos permitidos.
                    if (args.length > 2 ||
                            (args.length == 2 &&
                            !"--sobrescribir".equals(args[1]))) {

                        System.out.println(
                                "Uso: referencias-rendimiento [--sobrescribir]"
                        );
                        break;
                    }

                    //Conserva los archivos existentes por defecto.
                    boolean sobrescribir = args.length == 2;

                    GeneradorReferenciasRendimiento.generar(
                            sobrescribir
                    );
                    break;
                }

                case "referencias":

                    //Permite reemplazar archivos solo cuando se solicita.
                    if (args.length > 2 ||
                            (args.length == 2 &&
                            !args[1].equalsIgnoreCase("--sobrescribir"))) {

                        throw new IllegalArgumentException(
                            "Opcion invalida para referencias."
                        );
                    }

                    boolean sobrescribir = args.length == 2;
                    generarReferencias(sobrescribir);
                    break;

                default:
                    throw new IllegalArgumentException(
                        "Comando no reconocido: " + args[0]
                    );
            }

        } catch (IOException | IllegalArgumentException e) {

            //Informa el problema y finaliza con codigo de error.
            System.err.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }

    //Genera los grafos y los guarda en archivos TXT.
    private static void generarCasos() throws IOException {

        List<CasoGrafo> casos = BateriaCasos.crearBateria();

        System.out.println("PROYECTO 13 - JUEZ DE GRAFOS");
        System.out.println("Generacion de casos de prueba");
        System.out.println("-----------------------------------");

        //Guarda cada grafo en la carpeta de entrada.
        for (CasoGrafo caso : casos) {

            Path archivo = EscritorGrafoTxt.guardar(
                caso, CARPETA_ENTRADA
            );

            System.out.println(
                "Generado: " + archivo
                + " | V=" + caso.vertices()
                + " | E=" + caso.aristas().size()
            );
        }

        System.out.println("-----------------------------------");
        System.out.println("Total de casos: " + casos.size());
        System.out.println("Generacion finalizada correctamente.");
    }

    //Calcula y guarda las respuestas de referencia.
    private static void generarReferencias(
            boolean sobrescribir) throws IOException {

        List<CasoGrafo> casos = BateriaCasos.crearBateria();

        int generados = 0;
        int omitidos = 0;

        System.out.println("PROYECTO 13 - JUEZ DE GRAFOS");
        System.out.println("Generacion de referencias");
        System.out.println("-----------------------------------");

        //Procesa cada grafo de la bateria.
        for (CasoGrafo caso : casos) {

            Path archivo = CARPETA_REFERENCIA.resolve(
                caso.nombre() + ".txt"
            );

            //Protege las referencias creadas anteriormente.
            if (!sobrescribir && Files.exists(archivo)) {

                System.out.println("Omitido (ya existe): " + archivo);
                omitidos++;
                continue;
            }

            //Calcula las respuestas mediante los algoritmos.
            Map<String, String> referencia =
                    CalculadorReferencias.calcular(caso);

            //Guarda las respuestas en el archivo correspondiente.
            Path guardado = EscritorReferenciaTxt.guardar(
                caso,
                referencia,
                CARPETA_REFERENCIA,
                sobrescribir
            );

            System.out.println("Generado: " + guardado);
            generados++;
        }

        System.out.println("-----------------------------------");
        System.out.println("Referencias generadas: " + generados);
        System.out.println("Referencias omitidas: " + omitidos);
        System.out.println("Proceso finalizado correctamente.");
    }

    //Genera los grafos utilizados en pruebas de rendimiento.
    private static void generarRendimiento() throws IOException {

        List<CasoGrafo> casos = BateriaRendimiento.crearBateria();

        System.out.println("PROYECTO 13 - JUEZ DE GRAFOS");
        System.out.println("Generacion de grafos de rendimiento");
        System.out.println("-----------------------------------");

        //Guarda cada grafo en su carpeta independiente.
        for (CasoGrafo caso : casos) {

            Path archivo = EscritorGrafoTxt.guardar(
                caso, CARPETA_RENDIMIENTO
            );

            System.out.println(
                "Generado: " + archivo
                + " | V=" + caso.vertices()
                + " | E=" + caso.aristas().size()
            );
        }

        System.out.println("-----------------------------------");
        System.out.println("Total de casos: " + casos.size());
        System.out.println("Generacion finalizada correctamente.");
    }

    //Muestra los comandos disponibles.
    private static void mostrarAyuda() {

        System.out.println("PROYECTO 13 - JUEZ DE GRAFOS");
        System.out.println();
        System.out.println("Comandos disponibles:");
        System.out.println("  generar");
        System.out.println("    Genera los grafos de prueba.");
        System.out.println();
        System.out.println("  referencias");
        System.out.println("    Crea las referencias que no existen.");
        System.out.println();
        System.out.println("  referencias --sobrescribir");
        System.out.println("    Regenera las referencias existentes.");
        System.out.println();
        System.out.println("  rendimiento");
        System.out.println("    Genera los grafos grandes de prueba.");
        System.out.println();

        System.out.println("  referencias-rendimiento");
        System.out.println("    Calcula referencias para grafos grandes.");
        System.out.println("  referencias-rendimiento --sobrescribir");
        System.out.println("    Reemplaza las referencias existentes.");
        System.out.println();

        System.out.println("  ayuda");
        System.out.println("    Muestra esta informacion.");
    }
}
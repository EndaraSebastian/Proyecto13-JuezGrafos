package ec.edu.epn.eda2.juez.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

//Utilidades minimas para las pruebas del core, en el mismo estilo
//de las pruebas existentes (sin librerias externas).
final class MiniPrueba {

    //Cuerpo de una prueba: puede lanzar cualquier excepcion.
    interface Cuerpo {
        void ejecutar() throws Exception;
    }

    private static int correctas = 0;
    private static int fallidas = 0;
    private static final List<Path> temporales = new ArrayList<>();

    //Impide crear instancias de esta clase utilitaria.
    private MiniPrueba() {
    }

    //Ejecuta una prueba y registra [OK] o [ERROR].
    static void probar(String nombre, Cuerpo cuerpo) {

        try {
            cuerpo.ejecutar();

            correctas++;
            System.out.println("[OK] " + nombre);

        } catch (AssertionError e) {

            fallidas++;
            System.out.println("[ERROR] " + nombre + ": " + e.getMessage());

        } catch (Exception e) {

            fallidas++;
            System.out.println(
                "[ERROR] " + nombre + ": excepcion inesperada: " + e
            );
        }
    }

    //Falla la prueba si la condicion no se cumple.
    static void exigir(boolean condicion, String mensaje) {

        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    //Crea una carpeta temporal que se borra al terminar.
    static Path carpetaTemporal() throws IOException {

        Path carpeta = Files.createTempDirectory("juez-prueba");
        temporales.add(carpeta);
        return carpeta;
    }

    //Ejecuta una accion sin mostrar su salida por consola.
    static <T> T silenciando(Callable<T> accion) throws Exception {

        PrintStream original = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));

        try {
            return accion.call();
        } finally {
            System.setOut(original);
        }
    }

    //Borra temporales, imprime el resumen y falla si hubo errores.
    static void terminar() {

        for (Path carpeta : temporales) {

            try (Stream<Path> ruta = Files.walk(carpeta)) {

                ruta.sorted(Comparator.reverseOrder())
                    .forEach(p -> p.toFile().delete());

            } catch (IOException e) {
                //Limpieza de mejor esfuerzo.
            }
        }

        System.out.println("--------------------------------");
        System.out.println("Correctas: " + correctas);
        System.out.println("Fallidas: " + fallidas);

        if (fallidas > 0) {
            System.exit(1);
        }
    }
}

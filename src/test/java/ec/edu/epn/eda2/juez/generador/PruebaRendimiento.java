package ec.edu.epn.eda2.juez.generador;

import ec.edu.epn.eda2.juez.generador.servicio.GeneradorAleatorio;
import ec.edu.epn.eda2.juez.core.modelo.CasoGrafo;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

//Comprueba los grafos utilizados para pruebas de rendimiento.
public final class PruebaRendimiento {

    private static final Path CARPETA =
            Path.of("casos", "rendimiento");

    private static int correctas = 0;
    private static int fallidas = 0;

    //Define las caracteristicas esperadas de cada grafo.
    private record Especificacion(
            String nombre, int vertices, int aristas) {
    }

    private static final List<Especificacion> CASOS = List.of(
        new Especificacion("14_rendimiento_100_disperso", 100, 150),
        new Especificacion("15_rendimiento_100_denso", 100, 3960),
        new Especificacion("16_rendimiento_500_disperso", 500, 750),
        new Especificacion("17_rendimiento_500_denso", 500, 99800),
        new Especificacion("18_rendimiento_1000_disperso", 1000, 1500)
    );

    //Evita crear instancias de esta clase.
    private PruebaRendimiento() {
    }

    //Ejecuta las pruebas de los archivos de rendimiento.
    public static void main(String[] args) {

        System.out.println("PRUEBAS DE GRAFOS DE RENDIMIENTO");
        System.out.println("--------------------------------");

        //Comprueba que existan exactamente cinco archivos TXT.
        probar("Cantidad de archivos", () -> {

            try (Stream<Path> archivos = Files.list(CARPETA)) {

                long cantidad = archivos
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".txt"))
                        .count();

                comprobar(cantidad == CASOS.size(),
                        "Se esperaban cinco archivos TXT.");
            }
        });

        //Comprueba individualmente cada grafo.
        for (Especificacion caso : CASOS) {

            probar(caso.nombre(), () -> verificarArchivo(caso));
        }

        //Comprueba que la misma semilla genere los mismos grafos.
        probar("Reproducibilidad", () -> {

            GeneradorAleatorio a = new GeneradorAleatorio(20261314L);
            GeneradorAleatorio b = new GeneradorAleatorio(20261314L);

            CasoGrafo primero = a.generarNoDirigido(
                    "prueba", 100, 150, 20, true);

            CasoGrafo segundo = b.generarNoDirigido(
                    "prueba", 100, 150, 20, true);

            comprobar(primero.aristas().equals(segundo.aristas()),
                    "La semilla no produjo el mismo grafo.");
        });

        //Muestra los resultados finales.
        System.out.println("--------------------------------");
        System.out.println("Correctas: " + correctas);
        System.out.println("Fallidas: " + fallidas);

        if (fallidas > 0) {
            System.exit(1);
        }
    }

    //Lee el TXT sin cargar todo el archivo en memoria.
    private static void verificarArchivo(Especificacion caso)
            throws IOException {

        Path archivo = CARPETA.resolve(caso.nombre() + ".txt");

        comprobar(Files.isRegularFile(archivo),
                "El archivo no existe.");

        try (BufferedReader lector = Files.newBufferedReader(
                archivo, StandardCharsets.UTF_8)) {

            //Comprueba el encabezado del grafo.
            String encabezado = lector.readLine();

            comprobar(encabezado != null, "Archivo vacio.");

            String esperado = "U " + caso.vertices()
                    + " " + caso.aristas();

            comprobar(esperado.equals(encabezado.trim()),
                    "Encabezado incorrecto.");

            int vertices = caso.vertices();
            int[] padre = new int[vertices];

            //Inicializa Union-Find para verificar conectividad.
            for (int i = 0; i < vertices; i++) {
                padre[i] = i;
            }

            int componentes = vertices;
            int aristasLeidas = 0;

            Set<Long> conexiones = new HashSet<>();

            String linea;

            //Lee y verifica cada arista del archivo.
            while ((linea = lector.readLine()) != null) {

                String[] partes = linea.trim().split("\\s+");

                comprobar(partes.length == 3,
                        "Formato de arista incorrecto.");

                int origen = Integer.parseInt(partes[0]);
                int destino = Integer.parseInt(partes[1]);
                int peso = Integer.parseInt(partes[2]);

                //Comprueba que los vertices sean validos.
                comprobar(
                        origen >= 0 && origen < vertices
                        && destino >= 0 && destino < vertices,
                        "Vertice fuera de rango."
                );

                //Comprueba pesos y ausencia de bucles propios.
                comprobar(origen != destino,
                        "Se encontro un bucle propio.");

                comprobar(peso >= 1 && peso <= 20,
                        "Peso fuera del rango esperado.");

                //Normaliza la conexion no dirigida.
                int menor = Math.min(origen, destino);
                int mayor = Math.max(origen, destino);

                long clave = (long) menor * vertices + mayor;

                //Detecta conexiones duplicadas.
                comprobar(conexiones.add(clave),
                        "Se encontro una arista duplicada.");

                //Une componentes para comprobar conectividad.
                int raizA = buscar(padre, origen);
                int raizB = buscar(padre, destino);

                if (raizA != raizB) {
                    padre[raizA] = raizB;
                    componentes--;
                }

                aristasLeidas++;
            }

            //Comprueba la cantidad exacta de conexiones.
            comprobar(aristasLeidas == caso.aristas(),
                    "Cantidad de aristas incorrecta.");

            //Todos los vertices deben pertenecer a una componente.
            comprobar(componentes == 1,
                    "El grafo no es conexo.");
        }
    }

    //Busca el representante usando compresion de caminos.
    private static int buscar(int[] padre, int vertice) {

        if (padre[vertice] != vertice) {
            padre[vertice] = buscar(padre, padre[vertice]);
        }

        return padre[vertice];
    }

    //Comprueba una condicion.
    private static void comprobar(
            boolean condicion, String mensaje) {

        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    //Ejecuta una prueba y registra el resultado.
    private static void probar(String nombre, Prueba prueba) {

        try {
            prueba.ejecutar();

            correctas++;
            System.out.println("[OK] " + nombre);

        } catch (Exception | AssertionError e) {

            fallidas++;
            System.out.println(
                    "[ERROR] " + nombre + ": " + e.getMessage()
            );
        }
    }

    //Permite ejecutar pruebas que leen archivos.
    @FunctionalInterface
    private interface Prueba {
        void ejecutar() throws IOException;
    }
}
package ec.edu.epn.eda2.juez.core.ejecucion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

//Descubre los casos que hay en las carpetas del proyecto.
//Cada TXT de entrada se empareja con la referencia del mismo nombre.
public final class CatalogoCasos {

    //Carpetas de la bateria funcional (13 casos pequenos).
    public static final Path ENTRADA_FUNCIONAL =
            Path.of("casos", "entrada");

    public static final Path REFERENCIA_FUNCIONAL =
            Path.of("casos", "referencia");

    //Carpetas de la bateria de rendimiento (5 casos grandes).
    public static final Path ENTRADA_RENDIMIENTO =
            Path.of("casos", "rendimiento");

    public static final Path REFERENCIA_RENDIMIENTO =
            Path.of("casos", "referencias-rendimiento");

    //Impide crear instancias de esta clase utilitaria.
    private CatalogoCasos() {
    }

    //Lista los TXT de una carpeta, ordenados por nombre, y les asigna
    //la referencia del mismo nombre. Si falta la referencia, el
    //ejecutor lo informa como ERROR_CASO.
    public static List<CasoPrueba> descubrir(
            Path carpetaEntrada,
            Path carpetaReferencia) throws IOException {

        if (!Files.isDirectory(carpetaEntrada)) {
            throw new IllegalArgumentException(
                "No existe la carpeta de entrada: " + carpetaEntrada
                + ". Ejecute los comandos generar y referencias, "
                + "y lance el juez desde la raiz del proyecto."
            );
        }

        try (Stream<Path> archivos = Files.list(carpetaEntrada)) {

            return archivos
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(".txt"))
                .sorted()
                .map(p -> {
                    String archivo = p.getFileName().toString();
                    String nombre =
                            archivo.substring(0, archivo.length() - 4);

                    return new CasoPrueba(
                        nombre, p, carpetaReferencia.resolve(archivo)
                    );
                })
                .toList();
        }
    }

    //Deja solo los casos cuyo nombre contiene el texto indicado.
    public static List<CasoPrueba> filtrar(
            List<CasoPrueba> casos, String texto) {

        return casos.stream()
            .filter(c -> c.nombre().contains(texto))
            .toList();
    }
}

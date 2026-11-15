package ec.edu.epn.eda2.juez.generador.referencia;

import ec.edu.epn.eda2.juez.core.modelo.CasoGrafo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//Guarda los resultados de referencia en archivos TXT.
public final class EscritorReferenciaTxt {

    //Impide crear instancias de esta clase utilitaria.
    private EscritorReferenciaTxt() {
    }

    //Guarda una referencia sin reemplazar archivos existentes.
    public static Path guardar(
            CasoGrafo grafo,
            Map<String, String> referencia,
            Path carpeta) throws IOException {

        return guardar(grafo, referencia, carpeta, false);
    }

    //Guarda la referencia y permite controlar la sobrescritura.
    public static Path guardar(
            CasoGrafo grafo,
            Map<String, String> referencia,
            Path carpeta,
            boolean sobrescribir) throws IOException {

        //Verifica que los parametros sean validos.
        Objects.requireNonNull(grafo, "El grafo es obligatorio.");
        Objects.requireNonNull(referencia, "La referencia es obligatoria.");
        Objects.requireNonNull(carpeta, "La carpeta es obligatoria.");

        //Comprueba que se utilice el formato acordado.
        if (!"REFERENCIA_V1".equals(referencia.get("FORMATO"))) {
            throw new IllegalArgumentException(
                "Formato de referencia incorrecto."
            );
        }

        //Comprueba que la respuesta corresponda al grafo.
        if (!grafo.nombre().equals(referencia.get("CASO"))) {
            throw new IllegalArgumentException(
                "La referencia no corresponde al caso."
            );
        }

        //Crea la carpeta cuando no existe.
        Files.createDirectories(carpeta);

        //Construye la ubicacion del archivo.
        Path archivo = carpeta.resolve(grafo.nombre() + ".txt");

        List<String> lineas = new ArrayList<>();

        //Convierte los resultados al formato CLAVE=VALOR.
        for (Map.Entry<String, String> dato : referencia.entrySet()) {

            String clave = dato.getKey();
            String valor = dato.getValue();

            //Evita claves o valores que alteren el formato TXT.
            if (clave == null || !clave.matches("[A-Z_]+")
                    || valor == null
                    || valor.contains("\n")
                    || valor.contains("\r")) {

                throw new IllegalArgumentException(
                    "Dato de referencia invalido."
                );
            }

            lineas.add(clave + "=" + valor);
        }

        //Guarda el archivo utilizando codificacion UTF-8.
        if (sobrescribir) {

            Files.write(
                archivo,
                lineas,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
            );

        } else {

            //Protege archivos existentes contra reemplazos accidentales.
            Files.write(
                archivo,
                lineas,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
            );
        }

        //Devuelve la ubicacion del archivo generado.
        return archivo;
    }
}
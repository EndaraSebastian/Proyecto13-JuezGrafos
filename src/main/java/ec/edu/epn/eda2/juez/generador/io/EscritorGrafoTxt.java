package ec.edu.epn.eda2.juez.generador.io;

import ec.edu.epn.eda2.juez.core.modelo.Arista;
import ec.edu.epn.eda2.juez.core.modelo.CasoGrafo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//Guarda los grafos en archivos TXT para el framework.
public final class EscritorGrafoTxt {

    //Impide crear objetos de esta clase utilitaria.
    private EscritorGrafoTxt() {
    }

    //Guarda un caso de prueba en la carpeta indicada.
    public static Path guardar(
            CasoGrafo grafo, Path carpeta) throws IOException {

        //Verifica que los parametros no sean nulos.
        Objects.requireNonNull(grafo, "El grafo es obligatorio.");
        Objects.requireNonNull(carpeta, "La carpeta es obligatoria.");

        //Crea la carpeta de destino cuando no existe.
        Files.createDirectories(carpeta);

        //Construye la ruta del archivo TXT.
        Path archivo = carpeta.resolve(grafo.nombre() + ".txt");

        List<String> lineas = new ArrayList<>();

        //Primera linea: tipo, vertices y cantidad de aristas.
        String encabezado = grafo.tipo().codigo()
                + " " + grafo.vertices()
                + " " + grafo.aristas().size();

        lineas.add(encabezado);

        //Agrega las conexiones con sus respectivos pesos.
        for (Arista arista : grafo.aristas()) {

            String linea = arista.origen()
                    + " " + arista.destino()
                    + " " + arista.peso();

            lineas.add(linea);
        }

        //Escribe el contenido utilizando codificacion UTF-8.
        Files.write(
            archivo,
            lineas,
            StandardCharsets.UTF_8
        );

        //Devuelve la ubicacion del archivo generado.
        return archivo;
    }
}
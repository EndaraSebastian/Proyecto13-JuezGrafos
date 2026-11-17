package ec.edu.epn.eda2.juez.core.parser;

import ec.edu.epn.eda2.juez.core.modelo.Arista;
import ec.edu.epn.eda2.juez.core.modelo.CasoGrafo;
import ec.edu.epn.eda2.juez.core.modelo.GrafoCargado;
import ec.edu.epn.eda2.juez.core.modelo.TipoGrafo;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

//Lee archivos TXT con formato GRAFO_V1 y los carga en memoria.
//
//Formato:
//  TIPO V E            (TIPO = D dirigido, U no dirigido)
//  ORIGEN DESTINO PESO (exactamente E lineas)
//
//Se ignoran las lineas en blanco y un BOM UTF-8 inicial.
//Cualquier otra desviacion lanza ErrorFormatoException con el
//numero de linea, para que el error sea facil de ubicar.
public final class LectorGrafoTxt {

    //Limite de seguridad para no agotar la memoria con un V enorme.
    public static final int MAX_VERTICES = 2_000_000;

    //Impide crear instancias de esta clase utilitaria.
    private LectorGrafoTxt() {
    }

    //Lee un archivo y devuelve el grafo listo para las soluciones.
    public static GrafoCargado leer(Path archivo) throws IOException {
        return new GrafoCargado(leerCaso(archivo));
    }

    //Lee un archivo y devuelve el caso del modelo compartido.
    //El nombre del caso es el nombre del archivo sin ".txt".
    public static CasoGrafo leerCaso(Path archivo) throws IOException {

        Objects.requireNonNull(archivo, "El archivo es obligatorio.");

        String nombreArchivo = archivo.getFileName().toString();
        List<String> lineas;

        try {
            lineas = Files.readAllLines(
                archivo, StandardCharsets.UTF_8
            );
        } catch (CharacterCodingException e) {
            throw new ErrorFormatoException(
                nombreArchivo, 0, "El archivo no es UTF-8 valido."
            );
        }

        return parsear(nombreDeCaso(nombreArchivo), nombreArchivo, lineas);
    }

    //Convierte el contenido (ya leido) en un caso. Sirve tambien
    //para pruebas sin tocar el disco.
    public static CasoGrafo parsear(
            String nombreCaso,
            String origen,
            List<String> lineas) {

        if (nombreCaso == null
                || !nombreCaso.matches("[a-zA-Z0-9_-]+")) {

            throw new ErrorFormatoException(
                origen, 0,
                "El nombre del caso solo admite letras, numeros, "
                + "'_' y '-'."
            );
        }

        TipoGrafo tipo = null;
        int vertices = 0;
        int aristasEsperadas = 0;
        boolean hayEncabezado = false;

        List<Arista> aristas = new ArrayList<>();
        Set<Long> conexiones = new HashSet<>();

        for (int i = 0; i < lineas.size(); i++) {

            int numero = i + 1;
            String texto = lineas.get(i);

            //Quita la marca BOM que algunos editores de Windows agregan.
            if (i == 0 && texto.startsWith("﻿")) {
                texto = texto.substring(1);
            }

            texto = texto.strip();

            if (texto.isEmpty()) {
                continue;
            }

            String[] campos = texto.split("\\s+");

            //Primera linea con contenido: TIPO V E.
            if (!hayEncabezado) {

                if (campos.length != 3) {
                    throw error(origen, numero,
                        "El encabezado debe tener 3 campos: TIPO V E.");
                }

                tipo = leerTipo(campos[0], origen, numero);
                vertices = leerEntero(campos[1], "V", origen, numero);
                aristasEsperadas =
                        leerEntero(campos[2], "E", origen, numero);

                if (vertices < 1) {
                    throw error(origen, numero,
                        "V debe ser al menos 1.");
                }

                if (vertices > MAX_VERTICES) {
                    throw error(origen, numero,
                        "V supera el maximo admitido ("
                        + MAX_VERTICES + ").");
                }

                if (aristasEsperadas < 0) {
                    throw error(origen, numero,
                        "E no puede ser negativo.");
                }

                hayEncabezado = true;
                continue;
            }

            //Mas lineas de las que declara el encabezado.
            if (aristas.size() == aristasEsperadas) {
                throw error(origen, numero,
                    "Sobran lineas: el encabezado declara "
                    + aristasEsperadas + " aristas.");
            }

            if (campos.length != 3) {
                throw error(origen, numero,
                    "Una arista debe tener 3 campos: "
                    + "ORIGEN DESTINO PESO.");
            }

            int origenArista =
                    leerEntero(campos[0], "ORIGEN", origen, numero);
            int destino =
                    leerEntero(campos[1], "DESTINO", origen, numero);
            int peso =
                    leerEntero(campos[2], "PESO", origen, numero);

            if (origenArista < 0 || origenArista >= vertices
                    || destino < 0 || destino >= vertices) {

                throw error(origen, numero,
                    "Vertice fuera de rango (validos: 0 a "
                    + (vertices - 1) + ").");
            }

            if (origenArista == destino) {
                throw error(origen, numero,
                    "No se admiten bucles propios.");
            }

            if (peso <= 0) {
                throw error(origen, numero,
                    "El peso debe ser un entero positivo.");
            }

            //En grafos no dirigidos 0-1 y 1-0 son la misma arista.
            int a = origenArista;
            int b = destino;

            if (tipo == TipoGrafo.NO_DIRIGIDO && a > b) {
                a = destino;
                b = origenArista;
            }

            long clave = (long) a * vertices + b;

            if (!conexiones.add(clave)) {
                throw error(origen, numero,
                    "Arista duplicada " + origenArista + " "
                    + destino + ".");
            }

            aristas.add(new Arista(origenArista, destino, peso));
        }

        if (!hayEncabezado) {
            throw error(origen, 0,
                "El archivo esta vacio: falta el encabezado.");
        }

        if (aristas.size() != aristasEsperadas) {
            throw error(origen, 0,
                "Faltan aristas: el encabezado declara "
                + aristasEsperadas + " y hay " + aristas.size() + ".");
        }

        //El modelo compartido vuelve a validar como ultima defensa.
        try {
            return new CasoGrafo(nombreCaso, tipo, vertices, aristas);
        } catch (IllegalArgumentException e) {
            throw error(origen, 0, e.getMessage());
        }
    }

    //Interpreta el codigo de tipo: D o U (en mayuscula).
    private static TipoGrafo leerTipo(
            String texto, String origen, int numero) {

        for (TipoGrafo tipo : TipoGrafo.values()) {
            if (tipo.codigo().equals(texto)) {
                return tipo;
            }
        }

        throw error(origen, numero,
            "TIPO debe ser D o U y se encontro '" + texto + "'.");
    }

    //Convierte un campo a entero con un mensaje de error claro.
    private static int leerEntero(
            String texto, String campo, String origen, int numero) {

        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw error(origen, numero,
                campo + " debe ser un entero y se encontro '"
                + texto + "'.");
        }
    }

    //Quita la extension .txt para obtener el nombre del caso.
    private static String nombreDeCaso(String nombreArchivo) {

        if (nombreArchivo.endsWith(".txt")) {
            return nombreArchivo.substring(
                0, nombreArchivo.length() - 4
            );
        }

        return nombreArchivo;
    }

    //Crea el error con archivo y linea.
    private static ErrorFormatoException error(
            String origen, int numero, String mensaje) {

        return new ErrorFormatoException(origen, numero, mensaje);
    }
}

package ec.edu.epn.eda2.juez.core.parser;

import ec.edu.epn.eda2.juez.core.parser.ReferenciaCaso.EstadoTopologico;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//Lee archivos de referencia con formato REFERENCIA_V1.
//
//Cada linea es CLAVE=VALOR. Claves obligatorias: FORMATO, CASO,
//INICIO, BFS_ORDEN, BFS_DISTANCIAS, DFS_ORDEN, TOPO_ESTADO,
//MST_PESO y MST_COMPONENTES. TOPO_ORDEN es opcional. Las claves
//desconocidas se ignoran para permitir ampliar el formato.
public final class LectorReferenciaTxt {

    private static final String NO_APLICA = "NO_APLICA";

    //Impide crear instancias de esta clase utilitaria.
    private LectorReferenciaTxt() {
    }

    //Lee la referencia de un archivo. El valor de CASO debe coincidir
    //con el nombre del archivo.
    public static ReferenciaCaso leer(Path archivo) throws IOException {

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

        ReferenciaCaso referencia = parsear(nombreArchivo, lineas);

        String esperado = nombreArchivo.endsWith(".txt")
                ? nombreArchivo.substring(0, nombreArchivo.length() - 4)
                : nombreArchivo;

        if (!referencia.caso().equals(esperado)) {
            throw new ErrorFormatoException(
                nombreArchivo, 0,
                "CASO=" + referencia.caso()
                + " no coincide con el nombre del archivo."
            );
        }

        return referencia;
    }

    //Convierte el contenido (ya leido) en una referencia.
    public static ReferenciaCaso parsear(
            String origen, List<String> lineas) {

        Map<String, String> valores = new HashMap<>();
        Map<String, Integer> lineaDe = new HashMap<>();

        for (int i = 0; i < lineas.size(); i++) {

            int numero = i + 1;
            String texto = lineas.get(i);

            if (i == 0 && texto.startsWith("﻿")) {
                texto = texto.substring(1);
            }

            texto = texto.strip();

            if (texto.isEmpty()) {
                continue;
            }

            int igual = texto.indexOf('=');

            if (igual <= 0) {
                throw error(origen, numero,
                    "Se esperaba CLAVE=VALOR.");
            }

            String clave = texto.substring(0, igual).strip();
            String valor = texto.substring(igual + 1).strip();

            if (valores.containsKey(clave)) {
                throw error(origen, numero,
                    "Clave repetida: " + clave + ".");
            }

            valores.put(clave, valor);
            lineaDe.put(clave, numero);
        }

        if (!"REFERENCIA_V1".equals(valores.get("FORMATO"))) {
            throw error(origen, 0,
                "Falta FORMATO=REFERENCIA_V1 o es otro formato.");
        }

        String caso = obligatoria(valores, "CASO", origen);

        if (!caso.matches("[a-zA-Z0-9_-]+")) {
            throw error(origen, lineaDe.get("CASO"),
                "CASO no es un nombre valido.");
        }

        int inicio = entero(
            obligatoria(valores, "INICIO", origen),
            "INICIO", origen, lineaDe.get("INICIO")
        );

        if (inicio < 0) {
            throw error(origen, lineaDe.get("INICIO"),
                "INICIO no puede ser negativo.");
        }

        int[] bfsOrden = lista(valores, lineaDe, "BFS_ORDEN", origen);
        int[] bfsDistancias =
                lista(valores, lineaDe, "BFS_DISTANCIAS", origen);
        int[] dfsOrden = lista(valores, lineaDe, "DFS_ORDEN", origen);

        //Estado y orden topologico.
        String textoEstado =
                obligatoria(valores, "TOPO_ESTADO", origen);
        EstadoTopologico estado;

        try {
            estado = EstadoTopologico.valueOf(textoEstado);
        } catch (IllegalArgumentException e) {
            throw error(origen, lineaDe.get("TOPO_ESTADO"),
                "TOPO_ESTADO debe ser ACICLICO, CICLO o NO_APLICA.");
        }

        int[] topoOrden = new int[0];

        if (valores.containsKey("TOPO_ORDEN")
                && !NO_APLICA.equals(valores.get("TOPO_ORDEN"))) {

            topoOrden = lista(valores, lineaDe, "TOPO_ORDEN", origen);
        }

        //Datos del MST: ambos aplican o ninguno.
        String textoPeso = obligatoria(valores, "MST_PESO", origen);
        String textoComp =
                obligatoria(valores, "MST_COMPONENTES", origen);

        boolean pesoNoAplica = NO_APLICA.equals(textoPeso);
        boolean compNoAplica = NO_APLICA.equals(textoComp);

        if (pesoNoAplica != compNoAplica) {
            throw error(origen, lineaDe.get("MST_PESO"),
                "MST_PESO y MST_COMPONENTES deben aplicar ambos "
                + "o ninguno.");
        }

        long mstPeso = 0;
        int mstComponentes = 0;

        if (!pesoNoAplica) {

            try {
                mstPeso = Long.parseLong(textoPeso);
            } catch (NumberFormatException e) {
                throw error(origen, lineaDe.get("MST_PESO"),
                    "MST_PESO debe ser un entero o NO_APLICA.");
            }

            if (mstPeso < 0) {
                throw error(origen, lineaDe.get("MST_PESO"),
                    "MST_PESO no puede ser negativo.");
            }

            mstComponentes = entero(textoComp, "MST_COMPONENTES",
                origen, lineaDe.get("MST_COMPONENTES"));
        }

        return new ReferenciaCaso(
            caso, inicio, bfsOrden, bfsDistancias, dfsOrden,
            estado, topoOrden, !pesoNoAplica, mstPeso, mstComponentes
        );
    }

    //Obtiene una clave que debe existir.
    private static String obligatoria(
            Map<String, String> valores, String clave, String origen) {

        String valor = valores.get(clave);

        if (valor == null) {
            throw error(origen, 0, "Falta la clave " + clave + ".");
        }

        return valor;
    }

    //Convierte "1,2,3" en un arreglo de enteros.
    private static int[] lista(
            Map<String, String> valores,
            Map<String, Integer> lineaDe,
            String clave,
            String origen) {

        String valor = obligatoria(valores, clave, origen);
        int numero = lineaDe.get(clave);

        if (valor.isEmpty()) {
            return new int[0];
        }

        String[] partes = valor.split(",", -1);
        int[] resultado = new int[partes.length];

        for (int i = 0; i < partes.length; i++) {
            resultado[i] = entero(
                partes[i].strip(), clave, origen, numero
            );
        }

        return resultado;
    }

    //Convierte un texto a entero con un mensaje de error claro.
    private static int entero(
            String texto, String clave, String origen, int numero) {

        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw error(origen, numero,
                clave + " contiene '" + texto
                + "', que no es un entero.");
        }
    }

    //Crea el error con archivo y linea.
    private static ErrorFormatoException error(
            String origen, int numero, String mensaje) {

        return new ErrorFormatoException(origen, numero, mensaje);
    }
}

package ec.edu.epn.eda2.juez.validador;

import ec.edu.epn.eda2.juez.core.modelo.GrafoCargado;
import ec.edu.epn.eda2.juez.core.parser.ReferenciaCaso;
import ec.edu.epn.eda2.juez.submission.ResultadoBFS;
import ec.edu.epn.eda2.juez.submission.ResultadoMST;
import ec.edu.epn.eda2.juez.submission.ResultadoTopologico;

/**
 * PUNTO DE CONEXION PARA EL ESTUDIANTE 2.
 *
 * <p>El juez (core) llama a uno de estos metodos despues de ejecutar
 * y medir cada algoritmo de la solucion del estudiante. Aqui se
 * decide si la respuesta es correcta, no solo comparando con la
 * referencia sino comprobando sus propiedades (por ejemplo, que un
 * orden topologico respete todas las aristas o que un MST sea
 * aciclico y use aristas que existan en el grafo).
 *
 * <p>Reglas del contrato:
 * <ul>
 *   <li>Devolver un {@link Veredicto}; nunca null.</li>
 *   <li>No modificar el grafo ni la referencia.</li>
 *   <li>El juez llama a {@code validarTopologico} solo en grafos
 *       dirigidos y a {@code validarMst} solo en no dirigidos.</li>
 *   <li>Los resultados nunca son null (el juez ya lo comprobo),
 *       pero su contenido puede ser absurdo: indices fuera de rango,
 *       arreglos de longitud incorrecta, aristas inexistentes. El
 *       validador debe devolver INCORRECTO con un motivo claro, no
 *       lanzar excepciones. Si lanza una, el juez la registra como
 *       ERROR_VALIDADOR.</li>
 *   <li>Pueden existir varias respuestas correctas (orden topologico,
 *       MST con empates): aceptar todas las validas.</li>
 * </ul>
 *
 * <p>Para conectarlo: implementar esta interfaz con constructor
 * publico sin argumentos y ejecutar
 * {@code Main juzgar <Solucion> --validador <ClaseValidador>}, o
 * cambiar el valor por defecto en {@code JuezCli}.
 */
public interface ValidadorResultado {

    /** Valida el orden de descubrimiento devuelto por el DFS. */
    Veredicto validarDfs(
            GrafoCargado grafo, ReferenciaCaso referencia, int[] orden);

    /** Valida el orden y las distancias devueltos por el BFS. */
    Veredicto validarBfs(
            GrafoCargado grafo,
            ReferenciaCaso referencia,
            ResultadoBFS resultado);

    /** Valida el orden topologico o la deteccion de ciclo. */
    Veredicto validarTopologico(
            GrafoCargado grafo,
            ReferenciaCaso referencia,
            ResultadoTopologico resultado);

    /** Valida el arbol o bosque de expansion minima. */
    Veredicto validarMst(
            GrafoCargado grafo,
            ReferenciaCaso referencia,
            ResultadoMST resultado);

    /** Nombre que aparece en el reporte del juez. */
    default String nombre() {
        return getClass().getSimpleName();
    }
}

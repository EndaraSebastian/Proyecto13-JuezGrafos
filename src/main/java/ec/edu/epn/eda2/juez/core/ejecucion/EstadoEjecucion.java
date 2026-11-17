package ec.edu.epn.eda2.juez.core.ejecucion;

//Estado final de un algoritmo en un caso.
public enum EstadoEjecucion {

    //El validador acepto la respuesta.
    CORRECTO(false),

    //El validador rechazo la respuesta.
    INCORRECTO(true),

    //Se ejecuto y midio, pero no hubo validacion real.
    SIN_VALIDAR(false),

    //El algoritmo no corresponde al tipo de grafo.
    NO_APLICA(false),

    //La solucion lanzo una excepcion o devolvio null.
    ERROR_EJECUCION(true),

    //La solucion supero el limite de tiempo.
    TIMEOUT(true),

    //El validador fallo con una excepcion (error del validador).
    ERROR_VALIDADOR(true),

    //No se pudo cargar el grafo o su referencia.
    ERROR_CASO(true);

    private final boolean fallo;

    //Indica si este estado cuenta como fallo del juzgamiento.
    EstadoEjecucion(boolean fallo) {
        this.fallo = fallo;
    }

    //True si el estado debe hacer fallar la corrida completa.
    public boolean esFallo() {
        return fallo;
    }
}

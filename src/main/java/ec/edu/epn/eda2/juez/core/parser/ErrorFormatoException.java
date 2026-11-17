package ec.edu.epn.eda2.juez.core.parser;

//Error al leer un archivo TXT de grafo o de referencia.
//Extiende IllegalArgumentException para que Main lo informe
//como cualquier otro argumento invalido.
public final class ErrorFormatoException
        extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    private final String origen;
    private final int linea;

    //Crea el error indicando archivo y numero de linea (0 si no aplica).
    public ErrorFormatoException(
            String origen, int linea, String mensaje) {

        super(construir(origen, linea, mensaje));
        this.origen = origen;
        this.linea = linea;
    }

    //Nombre del archivo donde ocurrio el error.
    public String origen() {
        return origen;
    }

    //Numero de linea (empieza en 1) o 0 si el error es global.
    public int linea() {
        return linea;
    }

    //Arma un mensaje del tipo: archivo.txt:3: descripcion.
    private static String construir(
            String origen, int linea, String mensaje) {

        String donde = (origen == null) ? "entrada" : origen;

        if (linea > 0) {
            donde = donde + ":" + linea;
        }

        return donde + ": " + mensaje;
    }
}

package ec.edu.epn.eda2.juez.core.modelo;

//Define los tipos de grafos admitidos por el sistema.
public enum TipoGrafo {

    DIRIGIDO("D"),
    NO_DIRIGIDO("U");

    private final String codigo;

    //Asigna un codigo a cada tipo de grafo.
    TipoGrafo(String codigo) {
        this.codigo = codigo;
    }

    //Devuelve el codigo utilizado en los archivos TXT.
    public String codigo() {
        return codigo;
    }
}
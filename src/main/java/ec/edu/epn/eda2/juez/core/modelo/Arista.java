package ec.edu.epn.eda2.juez.core.modelo;

//Representa una conexion entre dos vertices y su peso.
public record Arista(int origen, int destino, int peso) {

    //Verifica que los datos de la arista sean validos.
    public Arista {
        if (origen < 0 || destino < 0) {
            throw new IllegalArgumentException(
                "Los vertices no pueden ser negativos."
            );
        }

        if (peso <= 0) {
            throw new IllegalArgumentException(
                "El peso debe ser positivo."
            );
        }
    }
}
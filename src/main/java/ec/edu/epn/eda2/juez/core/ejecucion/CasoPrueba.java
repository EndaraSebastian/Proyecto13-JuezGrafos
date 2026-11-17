package ec.edu.epn.eda2.juez.core.ejecucion;

import java.nio.file.Path;

//Un caso a juzgar: archivo de entrada y archivo de referencia.
public record CasoPrueba(String nombre, Path entrada, Path referencia) {
}

package ec.edu.epn.eda2.juez.core.ejecucion;

//Parametros de medicion del juez.
//
//calentamientos: ejecuciones descartadas antes de medir (JIT de la JVM).
//repeticiones:   ejecuciones medidas por cada algoritmo y caso.
//timeoutMs:      limite de tiempo de UNA ejecucion.
//pilaBytes:      tamano de pila del hilo de la solucion (0 = el de la JVM).
public record ConfiguracionJuez(
        int calentamientos,
        int repeticiones,
        long timeoutMs,
        long pilaBytes) {

    //Valida los rangos permitidos.
    public ConfiguracionJuez {

        if (calentamientos < 0) {
            throw new IllegalArgumentException(
                "Los calentamientos no pueden ser negativos."
            );
        }

        if (repeticiones < 1) {
            throw new IllegalArgumentException(
                "Debe haber al menos una repeticion."
            );
        }

        if (timeoutMs < 1) {
            throw new IllegalArgumentException(
                "El timeout debe ser de al menos 1 ms."
            );
        }

        if (pilaBytes < 0) {
            throw new IllegalArgumentException(
                "El tamano de pila no puede ser negativo."
            );
        }
    }

    //2 calentamientos, 5 repeticiones, 10 s de limite y 16 MB de pila.
    public static ConfiguracionJuez porDefecto() {
        return new ConfiguracionJuez(2, 5, 10_000, 16L * 1024 * 1024);
    }

    //Copia con otra cantidad de calentamientos.
    public ConfiguracionJuez conCalentamientos(int valor) {
        return new ConfiguracionJuez(
            valor, repeticiones, timeoutMs, pilaBytes
        );
    }

    //Copia con otra cantidad de repeticiones.
    public ConfiguracionJuez conRepeticiones(int valor) {
        return new ConfiguracionJuez(
            calentamientos, valor, timeoutMs, pilaBytes
        );
    }

    //Copia con otro limite de tiempo.
    public ConfiguracionJuez conTimeoutMs(long valor) {
        return new ConfiguracionJuez(
            calentamientos, repeticiones, valor, pilaBytes
        );
    }

    //Copia con otro tamano de pila.
    public ConfiguracionJuez conPilaBytes(long valor) {
        return new ConfiguracionJuez(
            calentamientos, repeticiones, timeoutMs, valor
        );
    }
}

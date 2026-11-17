package ec.edu.epn.eda2.juez.core;

import ec.edu.epn.eda2.juez.core.ejecucion.ConfiguracionJuez;
import ec.edu.epn.eda2.juez.core.ejecucion.Medicion;
import ec.edu.epn.eda2.juez.core.ejecucion.MedidorEjecucion;

import java.util.concurrent.atomic.AtomicInteger;

import static ec.edu.epn.eda2.juez.core.MiniPrueba.exigir;
import static ec.edu.epn.eda2.juez.core.MiniPrueba.probar;

//Pruebas del medidor de tiempo: repeticiones, captura de errores,
//limite de tiempo y validacion de la configuracion.
public final class PruebaMedidor {

    //Impide crear instancias de esta clase de prueba.
    private PruebaMedidor() {
    }

    public static void main(String[] args) {

        System.out.println("PRUEBAS DEL MEDIDOR");
        System.out.println("--------------------------------");

        probar("Hace calentamientos + repeticiones y mide cada una", () -> {

            AtomicInteger llamadas = new AtomicInteger();

            ConfiguracionJuez config =
                    new ConfiguracionJuez(2, 3, 5_000, 0);

            Medicion<Integer> m = MedidorEjecucion.medir(
                llamadas::incrementAndGet, config
            );

            exigir(m.exitosa(), "debia terminar bien");
            exigir(llamadas.get() == 5,
                "se esperaban 5 llamadas y fueron " + llamadas.get());
            exigir(m.tiemposNs().length == 3, "3 tiempos medidos");
            exigir(m.resultado() == 3,
                "se valida la primera repeticion medida (3) y fue "
                + m.resultado());
            exigir(m.minimoNs() >= 0, "minimo no negativo");
            exigir(m.medianaNs() >= m.minimoNs(), "mediana >= minimo");
            exigir(m.promedioNs() >= m.minimoNs(), "promedio >= minimo");
        });

        probar("El tiempo medido refleja el trabajo de la tarea", () -> {

            ConfiguracionJuez config =
                    new ConfiguracionJuez(0, 2, 5_000, 0);

            Medicion<Integer> m = MedidorEjecucion.medir(() -> {
                Thread.sleep(30);
                return 0;
            }, config);

            exigir(m.exitosa(), "debia terminar bien");
            exigir(m.medianaNs() >= 25_000_000L,
                "una tarea de 30 ms midio " + m.medianaNs() + " ns");
        });

        probar("Una excepcion se captura y se informa", () -> {

            Medicion<Integer> m = MedidorEjecucion.medir(() -> {
                throw new IllegalStateException("boom");
            }, ConfiguracionJuez.porDefecto());

            exigir(!m.exitosa(), "no debia ser exitosa");
            exigir(!m.agotoTiempo(), "no fue timeout");
            exigir(m.error() instanceof IllegalStateException,
                "tipo de error: " + m.error());
            exigir("boom".equals(m.error().getMessage()), "mensaje");
        });

        probar("Un error en el calentamiento tambien se informa", () -> {

            AtomicInteger llamadas = new AtomicInteger();

            Medicion<Integer> m = MedidorEjecucion.medir(() -> {
                llamadas.incrementAndGet();
                throw new RuntimeException("falla temprano");
            }, new ConfiguracionJuez(2, 3, 5_000, 0));

            exigir(!m.exitosa(), "no debia ser exitosa");
            exigir(llamadas.get() == 1,
                "debia detenerse en el primer error: " + llamadas.get());
        });

        probar("StackOverflowError se captura sin tumbar al juez", () -> {

            Medicion<Integer> m = MedidorEjecucion.medir(
                () -> recursionInfinita(0),
                new ConfiguracionJuez(0, 1, 10_000, 512L * 1024)
            );

            exigir(!m.exitosa(), "no debia ser exitosa");
            exigir(m.error() instanceof StackOverflowError,
                "tipo de error: " + m.error());
        });

        probar("La pila configurable permite recursion profunda", () -> {

            Medicion<Integer> m = MedidorEjecucion.medir(
                () -> recursionAcotada(20_000),
                ConfiguracionJuez.porDefecto()
                    .conCalentamientos(0).conRepeticiones(1)
            );

            exigir(m.exitosa(), "error: " + m.error());
            exigir(m.resultado() == 20_000, "resultado");
        });

        probar("Supera el limite: TIMEOUT sin esperar a que termine", () -> {

            long inicio = System.nanoTime();

            Medicion<Integer> m = MedidorEjecucion.medir(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    //Espera activa hasta que el juez interrumpa.
                }
                return 1;
            }, new ConfiguracionJuez(0, 1, 200, 0));

            long milisegundos = (System.nanoTime() - inicio) / 1_000_000;

            exigir(m.agotoTiempo(), "debia agotar el tiempo");
            exigir(!m.exitosa(), "no debia ser exitosa");
            exigir(m.minimoNs() == -1 && m.medianaNs() == -1,
                "sin tiempos medidos");
            exigir(milisegundos < 3_000,
                "tardo " + milisegundos + " ms en rendirse");
        });

        probar("Una tarea que devuelve null se mide igual", () -> {

            Medicion<Integer> m = MedidorEjecucion.medir(
                () -> null,
                new ConfiguracionJuez(0, 1, 5_000, 0)
            );

            exigir(m.exitosa(), "el medidor no juzga el resultado");
            exigir(m.resultado() == null, "resultado null");
        });

        probar("La configuracion rechaza valores invalidos", () -> {

            rechazarConfig(-1, 1, 1_000, 0);
            rechazarConfig(0, 0, 1_000, 0);
            rechazarConfig(0, 1, 0, 0);
            rechazarConfig(0, 1, 1_000, -1);
        });

        probar("Configuracion por defecto: 2 + 5, 10 s, 16 MB", () -> {

            ConfiguracionJuez c = ConfiguracionJuez.porDefecto();

            exigir(c.calentamientos() == 2, "calentamientos");
            exigir(c.repeticiones() == 5, "repeticiones");
            exigir(c.timeoutMs() == 10_000, "timeout");
            exigir(c.pilaBytes() == 16L * 1024 * 1024, "pila");
        });

        probar("Memoria: mide los bytes asignados por la tarea", () -> {

            ConfiguracionJuez config =
                    new ConfiguracionJuez(1, 3, 5_000, 0);

            Medicion<Integer> m = MedidorEjecucion.medir(
                () -> new byte[4 * 1024 * 1024].length, config
            );

            exigir(m.exitosa(), "debia terminar bien");
            exigir(m.memoriaBytes().length == 3, "3 mediciones de memoria");

            long mem = m.memoriaMedianaBytes();

            //Si la JVM no soporta la medicion se informa -1 (valido).
            exigir(mem == -1 || mem >= 4L * 1024 * 1024,
                "se esperaban >= 4 MB y fueron " + mem);
        });

        probar("Memoria: una tarea sin asignaciones usa casi nada", () -> {

            ConfiguracionJuez config =
                    new ConfiguracionJuez(1, 3, 5_000, 0);

            Medicion<Integer> m = MedidorEjecucion.medir(() -> 42, config);

            long mem = m.memoriaMedianaBytes();

            exigir(mem == -1 || mem < 64 * 1024,
                "una tarea trivial no deberia asignar mucho: " + mem);
        });

        probar("Memoria: fallo o timeout no tienen dato (-1)", () -> {

            Medicion<Integer> m = MedidorEjecucion.medir(() -> {
                throw new IllegalStateException("falla");
            }, new ConfiguracionJuez(0, 1, 5_000, 0));

            exigir(m.memoriaMedianaBytes() == -1, "debia ser -1");
        });

        MiniPrueba.terminar();
    }

    //Recursion que nunca termina: provoca StackOverflowError.
    private static int recursionInfinita(int n) {
        return recursionInfinita(n + 1) + 1;
    }

    //Recursion de profundidad n que devuelve n.
    private static int recursionAcotada(int n) {
        return (n == 0) ? 0 : recursionAcotada(n - 1) + 1;
    }

    //Comprueba que una configuracion invalida se rechace.
    private static void rechazarConfig(
            int calentamientos, int repeticiones,
            long timeoutMs, long pilaBytes) {

        try {
            new ConfiguracionJuez(
                calentamientos, repeticiones, timeoutMs, pilaBytes
            );
        } catch (IllegalArgumentException e) {
            return;
        }

        throw new AssertionError(
            "se acepto la configuracion " + calentamientos + ","
            + repeticiones + "," + timeoutMs + "," + pilaBytes
        );
    }
}

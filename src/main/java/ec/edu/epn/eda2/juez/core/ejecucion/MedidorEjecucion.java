package ec.edu.epn.eda2.juez.core.ejecucion;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

//Ejecuta una tarea (la llamada a la solucion del estudiante) y mide
//cuanto tarda.
//
//Como se mide:
//- La tarea corre en un hilo aparte, con pila configurable.
//- El cronometro (System.nanoTime) rodea SOLO la llamada a la tarea:
//  no incluye la lectura del TXT, el armado del grafo ni la validacion.
//- La memoria se mide como los BYTES ASIGNADOS por el hilo de la
//  solucion durante la llamada (ThreadMXBean.getThreadAllocatedBytes).
//  No depende del recolector de basura, asi que es reproducible.
//  Mide lo que la solucion reserva en total, no su pico residente.
//  Si la JVM no lo soporta se informa -1.
//- Se hacen N calentamientos (descartados) y M repeticiones medidas.
//- Cada ejecucion tiene un limite de tiempo. Si se supera, se
//  interrumpe el hilo y se informa TIMEOUT.
//- Cualquier excepcion o error (incluido StackOverflowError) se
//  captura y se informa sin detener al juez.
//
//Limite conocido: un hilo que ignora la interrupcion (ciclo infinito)
//sigue consumiendo CPU hasta que termina la JVM. Es un hilo demonio,
//asi que no impide cerrar el programa.
public final class MedidorEjecucion {

    //Valor devuelto por una ejecucion y lo que tardo.
    private record Corrida<T>(T valor, long nanos, long bytes) {
    }

    //Bean que informa los bytes asignados por hilo (null si no hay).
    private static final com.sun.management.ThreadMXBean HILOS = crearBean();

    private static com.sun.management.ThreadMXBean crearBean() {

        try {
            if (ManagementFactory.getThreadMXBean()
                    instanceof com.sun.management.ThreadMXBean bean
                    && bean.isThreadAllocatedMemorySupported()) {

                bean.setThreadAllocatedMemoryEnabled(true);
                return bean;
            }
        } catch (RuntimeException | LinkageError e) {
            //Sin soporte: la memoria se informa como -1.
        }

        return null;
    }

    //Bytes asignados por el hilo actual (-1 si no se puede medir).
    @SuppressWarnings("deprecation") //getId(): compatible con Java 17
    private static long bytesAsignados() {

        if (HILOS == null) {
            return -1;
        }

        return HILOS.getThreadAllocatedBytes(Thread.currentThread().getId());
    }

    //Impide crear instancias de esta clase utilitaria.
    private MedidorEjecucion() {
    }

    //Mide la tarea segun la configuracion dada.
    public static <T> Medicion<T> medir(
            Callable<T> tarea, ConfiguracionJuez config) {

        ExecutorService hilo = Executors.newSingleThreadExecutor(
            trabajo -> {
                Thread t = new Thread(
                    null, trabajo, "juez-solucion", config.pilaBytes()
                );
                t.setDaemon(true);
                return t;
            }
        );

        try {

            //Calentamiento: da tiempo al JIT y se descarta.
            for (int i = 0; i < config.calentamientos(); i++) {
                unaCorrida(hilo, tarea, config.timeoutMs());
            }

            T primero = null;
            long[] tiempos = new long[config.repeticiones()];
            long[] memoria = new long[config.repeticiones()];

            for (int i = 0; i < config.repeticiones(); i++) {

                Corrida<T> corrida =
                        unaCorrida(hilo, tarea, config.timeoutMs());

                //Se valida la respuesta de la primera repeticion.
                if (i == 0) {
                    primero = corrida.valor();
                }

                tiempos[i] = corrida.nanos();
                memoria[i] = corrida.bytes();
            }

            return Medicion.exito(primero, tiempos, memoria);

        } catch (TimeoutException e) {

            return Medicion.tiempoAgotado();

        } catch (ExecutionException e) {

            Throwable causa =
                    (e.getCause() != null) ? e.getCause() : e;

            return Medicion.fallo(causa);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            return Medicion.fallo(e);

        } finally {

            //Libera el hilo (interrumpe si quedo ejecutando algo).
            hilo.shutdownNow();
        }
    }

    //Ejecuta la tarea una vez en el hilo y cronometra solo la llamada.
    private static <T> Corrida<T> unaCorrida(
            ExecutorService hilo,
            Callable<T> tarea,
            long timeoutMs)
            throws ExecutionException, TimeoutException,
                   InterruptedException {

        Callable<Corrida<T>> cronometrada = () -> {
            long bytesAntes = bytesAsignados();
            long inicio = System.nanoTime();
            T valor = tarea.call();
            long fin = System.nanoTime();
            long bytesDespues = bytesAsignados();

            long bytes = (bytesAntes < 0 || bytesDespues < 0)
                ? -1 : bytesDespues - bytesAntes;

            return new Corrida<>(valor, fin - inicio, bytes);
        };

        Future<Corrida<T>> futuro = hilo.submit(cronometrada);

        try {
            return futuro.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            futuro.cancel(true);
            throw e;
        }
    }
}

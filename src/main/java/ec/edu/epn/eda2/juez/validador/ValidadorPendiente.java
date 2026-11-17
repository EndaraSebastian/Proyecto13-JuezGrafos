package ec.edu.epn.eda2.juez.validador;

import ec.edu.epn.eda2.juez.core.modelo.GrafoCargado;
import ec.edu.epn.eda2.juez.core.parser.ReferenciaCaso;
import ec.edu.epn.eda2.juez.submission.ResultadoBFS;
import ec.edu.epn.eda2.juez.submission.ResultadoMST;
import ec.edu.epn.eda2.juez.submission.ResultadoTopologico;

//Validador provisional: no valida nada y responde PENDIENTE.
//Permite probar el juez completo (carga, ejecucion, tiempos y
//reporte) mientras el Estudiante 2 termina su validador real.
//El reporte muestra SIN_VALIDAR en lugar de CORRECTO/INCORRECTO.
public final class ValidadorPendiente implements ValidadorResultado {

    private static final String MOTIVO =
            "Validador del Estudiante 2 sin conectar.";

    //Constructor publico sin argumentos (exigido para la carga).
    public ValidadorPendiente() {
    }

    @Override
    public Veredicto validarDfs(
            GrafoCargado grafo, ReferenciaCaso referencia, int[] orden) {

        return Veredicto.pendiente(MOTIVO);
    }

    @Override
    public Veredicto validarBfs(
            GrafoCargado grafo,
            ReferenciaCaso referencia,
            ResultadoBFS resultado) {

        return Veredicto.pendiente(MOTIVO);
    }

    @Override
    public Veredicto validarTopologico(
            GrafoCargado grafo,
            ReferenciaCaso referencia,
            ResultadoTopologico resultado) {

        return Veredicto.pendiente(MOTIVO);
    }

    @Override
    public Veredicto validarMst(
            GrafoCargado grafo,
            ReferenciaCaso referencia,
            ResultadoMST resultado) {

        return Veredicto.pendiente(MOTIVO);
    }

    @Override
    public String nombre() {
        return "ValidadorPendiente";
    }
}

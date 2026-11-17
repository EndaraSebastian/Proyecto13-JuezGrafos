package ec.edu.epn.eda2.juez.validador;

import java.util.Objects;

//Veredicto que devuelve el validador para una respuesta.
//CORRECTO e INCORRECTO traen un detalle opcional; PENDIENTE indica
//que no hubo validacion real (por ejemplo, validador sin conectar).
public record Veredicto(Estado estado, String detalle) {

    //Posibles resultados de una validacion.
    public enum Estado {
        CORRECTO, INCORRECTO, PENDIENTE
    }

    //Evita estado nulo y normaliza el detalle.
    public Veredicto {
        Objects.requireNonNull(estado, "El estado es obligatorio.");
        detalle = (detalle == null) ? "" : detalle;
    }

    //La respuesta es correcta.
    public static Veredicto correcto() {
        return new Veredicto(Estado.CORRECTO, "");
    }

    //La respuesta es correcta, con una nota.
    public static Veredicto correcto(String detalle) {
        return new Veredicto(Estado.CORRECTO, detalle);
    }

    //La respuesta es incorrecta; el motivo aparece en el reporte.
    public static Veredicto incorrecto(String motivo) {
        return new Veredicto(Estado.INCORRECTO, motivo);
    }

    //No se pudo validar todavia.
    public static Veredicto pendiente(String motivo) {
        return new Veredicto(Estado.PENDIENTE, motivo);
    }
}

package server;

public interface Service {

    /**
     * Ejecuta el servicio con una entrada dada y retorna una respuesta.
     * @param input La entrada proporcionada al servicio.
     * @return El resultado de la ejecución del servicio.
     */
    String execute(String input);

    /**
     * Devuelve el nombre del servicio.
     * Este nombre se usará para identificar el servicio.
     * @return El nombre del servicio.
     */
    String getName();

    /**
     * Devuelve una descripción del servicio, como instrucciones o detalles sobre su funcionamiento.
     * @return La descripción del servicio.
     */
    String getHelp();
}

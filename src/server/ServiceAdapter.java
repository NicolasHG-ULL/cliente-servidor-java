// server/ServiceAdapter.java
package server;

import java.lang.reflect.Method;

public class ServiceAdapter implements Service {

    private Object serviceInstance;
    private Class<?> serviceClass;

    public ServiceAdapter(Object serviceInstance, Class<?> serviceClass) {
        this.serviceInstance = serviceInstance;
        this.serviceClass = serviceClass;
    }

    @Override
    public String getName() {
        try {
            // Llamamos al método getName() del servicio adaptado
            Method getNameMethod = serviceClass.getMethod("getName");
            return (String) getNameMethod.invoke(serviceInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String execute(String input) {
        try {
            // Llamamos al método execute() del servicio adaptado
            Method executeMethod = serviceClass.getMethod("execute", String.class);
            return (String) executeMethod.invoke(serviceInstance, input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getHelp() {
        try {
            // Llamamos al método getHelp() del servicio adaptado
            Method getHelpMethod = serviceClass.getMethod("getHelp");
            return (String) getHelpMethod.invoke(serviceInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

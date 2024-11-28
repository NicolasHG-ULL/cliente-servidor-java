package server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.lang.reflect.Constructor;

public class ServiceLoader {

    public static Service loadService(String jarFilePath) throws Exception {
        // Crear URL para el JAR
        File jarFile = new File(jarFilePath);
        URL jarURL = jarFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{jarURL});

        String serviceClassName = getServiceClassNameFromManifest(jarFilePath);

        // Cargar la clase del servicio desde el JAR
        Class<?> clazz = classLoader.loadClass(serviceClassName);

        // Instanciamos el servicio
        Constructor<?> constructor = clazz.getConstructor();
        Object serviceInstance = constructor.newInstance();

        // Adaptamos el servicio a la interfaz server.Service
        return new ServiceAdapter(serviceInstance, clazz);
    }

    // Esta función lee el MANIFEST.MF y obtiene el nombre de la clase del servicio
    private static String getServiceClassNameFromManifest(String jarFilePath) throws IOException {
        try (JarFile jar = new JarFile(jarFilePath)) {
            JarEntry entry = jar.getJarEntry("META-INF/MANIFEST.MF");
            if (entry != null) {
                Manifest manifest = new Manifest(jar.getInputStream(entry));
                String serviceClass = manifest.getMainAttributes().getValue("Service-Class");
                if (serviceClass != null) {
                    return serviceClass;
                }
            }
        }
        throw new IOException("No se encontró el atributo 'Service-Class' en el MANIFEST.MF.");
    }
}

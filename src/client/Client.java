package client;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final String LOCAL_SERVICES_PATH = "local-services";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
             DataInputStream dataIn = new DataInputStream(socket.getInputStream())) {

            // Leer mensaje inicial del servidor
            System.out.println(dataIn.readUTF());

            int mainMenuOption;
            do {
                System.out.println("\nSeleccione la opción que quiera ejecutar:");
                System.out.println("1. Mostrar servicios disponibles");
                System.out.println("2. Ejecutar un servicio");
                System.out.println("3. Subir un nuevo servicio");
                System.out.println("4. Refrescar lista de servicios");
                System.out.println("5. Salir");
                System.out.print("Opción: ");
                mainMenuOption = scanner.nextInt();
                scanner.nextLine(); // Consumir salto de línea

                switch (mainMenuOption) {
                    case 1:
                        showServicesFromServer(dataOut, dataIn);
                        break;
                    case 2:
                        showServicesFromServer(dataOut, dataIn);

                        System.out.print("Índice del servicio: ");
                        int activeServiceIndex = scanner.nextInt();
                        scanner.nextLine();

                        dataOut.writeUTF("ACTIVE_SERVICE");
                        dataOut.writeInt(activeServiceIndex - 1);
                        dataOut.flush();

                        String serverResponse = dataIn.readUTF();
                        if ("null".equals(serverResponse)) {
                            System.out.println("Índice no válido. Volviendo al menú principal.");
                            break;
                        }

                        System.out.println("\nEstá ejecutando el servicio: " + serverResponse);
                        serviceMenu(scanner, serverResponse, dataOut, dataIn);
                        break;
                    case 3:
                        uploadJarFile(scanner, dataOut, dataIn);
                        break;
                    case 4:
                        dataOut.writeUTF("RELOAD_SERVICES");
                        dataOut.flush();
                        System.out.println(dataIn.readUTF());
                        break;
                    case 5:
                        System.out.println("Saliendo del cliente. ¡Hasta luego!");
                        dataOut.writeUTF("EXIT");
                        dataOut.flush();
                        break;
                    default:
                        System.out.println("Opción no válida. Intente de nuevo.");
                }

            } while (mainMenuOption != 5);

        } catch (IOException e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
            e.printStackTrace();
        }

        scanner.close();
    }

    private static void showServicesFromServer(DataOutputStream dataOut, DataInputStream dataIn) throws IOException {
        dataOut.writeUTF("LIST_SERVICES");
        dataOut.flush();

        int serviceCount = dataIn.readInt();
        System.out.println("\nServicios disponibles:");
        for (int i = 0; i < serviceCount; i++) {
            System.out.println((i + 1) + ". " + dataIn.readUTF());
        }
    }

    private static void serviceMenu(Scanner scanner, String serviceName, DataOutputStream dataOut, DataInputStream dataIn) throws IOException {
        int serviceMenuOption;
        do {
            System.out.println("\nMenú del servicio activo: " + serviceName);
            System.out.println("1. Mostrar instrucciones");
            System.out.println("2. Enviar entrada");
            System.out.println("3. Salir");
            System.out.print("Opción: ");
            serviceMenuOption = scanner.nextInt();
            scanner.nextLine();

            switch (serviceMenuOption) {
                case 1:
                    dataOut.writeUTF("GET_INSTRUCTIONS");
                    dataOut.flush();
                    System.out.println("Instrucciones: " + dataIn.readUTF());
                    break;
                case 2:
                    System.out.print("✏️ Ingrese una cadena para enviar al servicio: ");
                    String input = scanner.nextLine();
                    dataOut.writeUTF("EXECUTE_SERVICE");
                    dataOut.writeUTF(input);
                    dataOut.flush();
                    System.out.println("\uD83D\uDCAC Respuesta: " + dataIn.readUTF());
                    break;
                case 3:
                    dataOut.writeUTF("DEACTIVATE_SERVICE");
                    dataOut.flush();
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        } while (serviceMenuOption != 3);
    }

    private static void uploadJarFile(Scanner scanner, DataOutputStream dataOut, DataInputStream dataIn) throws IOException {
        File folder = new File(LOCAL_SERVICES_PATH);
        File[] jarFiles = folder.listFiles((dir, name) -> name.endsWith(".jar"));

        if (jarFiles == null || jarFiles.length == 0) {
            System.out.println("No se encontraron archivos .jar en 'local-services'.");
            return;
        }

        System.out.println("\nSeleccione el archivo .jar que desea subir:");
        for (int i = 0; i < jarFiles.length; i++) {
            System.out.println((i + 1) + ". " + jarFiles[i].getName());
        }
        System.out.print("Índice del archivo: ");
        int selectedIndex = scanner.nextInt();
        scanner.nextLine();

        if (selectedIndex < 1 || selectedIndex > jarFiles.length) {
            System.out.println("Selección no válida.");
            return;
        }

        File selectedFile = jarFiles[selectedIndex - 1];
        System.out.println("Seleccionado: " + selectedFile.getName());

        // Enviar comando, nombre y tamaño
        dataOut.writeUTF("UPLOAD_JAR");
        dataOut.writeUTF(selectedFile.getName());
        long fileSize = selectedFile.length();
        dataOut.writeLong(fileSize);
        dataOut.flush();

        // Enviar contenido del archivo
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(selectedFile))) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = bis.read(buffer)) != -1) {
                dataOut.write(buffer, 0, read);
            }
            dataOut.flush();
        }

        // Leer confirmación del servidor
        System.out.println(dataIn.readUTF());
    }
}

package services.java;

public class GreetingService implements Service {

    public GreetingService() {
        // Constructor vacío
    }

    @Override
    public String getName() {
        return "Greeting Service";
    }

    @Override
    public String execute(String name) {
        return "Hello, " + name + "!";
    }

    @Override
    public String getHelp() {
        return "This service greets the person by name.";
    }
}
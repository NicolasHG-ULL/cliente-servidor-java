package services.java;

public class ByeService implements Service {

    public ByeService() {
        // Constructor vacío
    }

    @Override
    public String getName() {
        return "Bye bye Service";
    }

    @Override
    public String execute(String name) {
        return "Bye bye, " + name + "!";
    }

    @Override
    public String getHelp() {
        return "This service dismiss the person by name.";
    }
}
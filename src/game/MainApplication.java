package game;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The main entry point of the application. Processes the View and Controller
 * classes and creates the application window.
 */
public class MainApplication extends Application {

    /**
     * The main method. Launches the program.
     * @param args - The launch arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Loads the relevant classes and creates a stage.
     * @param stage - The stage
     */
    @Override
    public void start(Stage stage) {
        View view = new View();
        new Controller(view);
        stage.setScene(view.getScene());
        stage.setTitle(view.getGameName());
        stage.setResizable(false);
        stage.show();
    }
}
package game;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

/**
 * Contains the input handlers for handling button presses in the View.
 */
public class Controller {

    // View object
    private View view;

    /**
     * Constructs the controller. Adds button handler.
     * @param view - input view
     */
    public Controller(View view) {
        this.view = view;
        view.addButtonHandler(new ActionHandler());
    }

    /**
     * Handles all actions, performing the relevant command to each button.
     */
    private class ActionHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent event) {
            Button pressedButton = (Button) event.getSource();
            switch (pressedButton.getText()) {
                case "Load":
                    view.loadMap();
                    break;
                case "Save":
                    view.saveMap();
                    break;
                case "Save As":
                    view.saveAsMap();
                    break;
                case "Dig":
                    view.dig();
                    break;
                case "▲":
                    view.movePlayer("north");
                    break;
                case "▼":
                    view.movePlayer("south");
                    break;
                case "◀":
                    view.movePlayer("west");
                    break;
                case "▶":
                    view.movePlayer("east");
                    break;
                case "△":
                    view.moveBlock("north");
                    break;
                case "▽":
                    view.moveBlock("south");
                    break;
                case "◁":
                    view.moveBlock("west");
                    break;
                case "▷":
                    view.moveBlock("east");
                    break;
                case "+":
                    view.zoomCamera("in");
                    break;
                case "–":
                    view.zoomCamera("out");
                    break;
                case "<":
                    view.rotateCamera("west");
                    break;
                case ">":
                    view.rotateCamera("east");
                    break;
            }
        }
    }

}

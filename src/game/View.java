package game;

import csse2002.block.world.Block;
import csse2002.block.world.Position;
import csse2002.block.world.WorldMap;
import csse2002.block.world.WorldMapFormatException;
import csse2002.block.world.WorldMapInconsistentException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.Pair;


/**
 * Contains all view-related logic, including methods for updating the 3D view,
 * creating buttons, creating controls etc.
 */
public class View {

    // Padding around the inventory items
    private static final double BUTTON_PADDING = 5;

    // Size of the blocks in the 3D view
    private static final double BLOCK_SIZE = 2;

    // Amount of blocks radius to load when loading a world
    private static final int LOAD_RADIUS = 10;

    // Name of the game
    private static final String GAME_NAME = "Mimicraft";

    // Grid pane storing the subgrids
    private GridPane masterGrid;

    // Materials for texturing blocks
    private PhongMaterial grassMat, soilMat, woodMat, stoneMat, playerMat
            = new PhongMaterial();

    // List of buttons
    private LinkedList<Button> buttonList = new LinkedList<>();

    // The camera model
    private PerspectiveCamera camera;

    // The player model
    private Sphere player;

    // The world map object, for which all operations are based
    private WorldMap worldMap = null;

    // Position object that stores the builder's current position
    private Position currentPosition = new Position(0,0);

    // The root group (contains all 3D objects)
    private Group root;

    // The group containing all objects, sent to MainApplication
    private Group group;

    // The master scene object, sent to MainApplication
    private Scene scene;

    // Subscene that goes in masterGrid, used for rendering 3D
    private SubScene subScene;

    // The scroll pane inside inventory
    private ScrollPane scrollPane;

    // The grid of controls
    private GridPane controlGrid;

    // The current file (loaded from the FileChooser
    private File currentFile;

    /**
     * Creates a View.
     */
    public View() {
        masterGrid = new GridPane();
        loadTextures();
        addComponents();
    }

    /**
     * Returns the scene object.
     * @return the scene object
     */
    public Scene getScene() {
        scene = new Scene(masterGrid,800,600);
        return scene;
    }

    /**
     * Returns the game name.
     * @return the game name
     */
    public String getGameName() {
        return GAME_NAME;
    }

    /**
     * Returns a PhongMaterial object corresponding to the input block type
     * string.
     * @param type - the block type (String)
     * @return a PhongMaterial corresponding to the parameter
     */
    private PhongMaterial getMaterial(String type) {
        switch (type) {
            case "grass":
                return grassMat;
            case "soil":
                return soilMat;
            case "wood":
                return woodMat;
            case "stone":
                return stoneMat;
            case "player":
                return playerMat;
            default:
                return null;
        }
    }

    /**
     * Sets the current position based off the input positions.
     * @param x - the input x coordinate
     * @param z - the input z coordinate
     */
    private void setCurrentPosition(int x, int z) {
        currentPosition = new Position(x,z);
    }

    /**
     * Loads the textures.
     */
    private void loadTextures() {
        grassMat = new PhongMaterial();
        soilMat = new PhongMaterial();
        woodMat = new PhongMaterial();
        stoneMat = new PhongMaterial();
        playerMat = new PhongMaterial();

        grassMat.setDiffuseMap(new Image("images/diffuse/grass.jpg"));
        soilMat.setDiffuseMap(new Image("images/diffuse/soil.jpg"));
        woodMat.setDiffuseMap(new Image("images/diffuse/wood.jpg"));
        stoneMat.setDiffuseMap(new Image("images/diffuse/stone.jpg"));
        playerMat.setDiffuseMap(new Image("images/diffuse/laughing.png"));
    }

    /**
     * Creates a worldMap object based off a file.
     * @param filename - the filename for the world map to load
     */
    private void createWorld(String filename) {
        try {
            worldMap = new WorldMap(filename);
            currentPosition = worldMap.getStartPosition();
            updateWorld();
        } catch (FileNotFoundException e) {

        } catch (WorldMapFormatException e) {
            createPopup("Error: World map has invalid format.");
            return;
        } catch (WorldMapInconsistentException e) {
            createPopup("Error: World map is inconsistent.");
            return;
        }
    }

    /**
     * Updates the world, adding all blocks to the world based off the worldMap
     * object.
     */
    private void updateWorld() {
        // Deletes all Boxes or Cylinders
        Iterator<Node> iter = root.getChildren().iterator();
        while (iter.hasNext()) {
            Node node = iter.next();
            if (node instanceof Box || node instanceof Cylinder) {
                iter.remove();
            }
        }

        int xLowerBound = currentPosition.getX() - LOAD_RADIUS;
        int xUpperBound = currentPosition.getX() + LOAD_RADIUS;
        int yLowerBound = currentPosition.getY() - LOAD_RADIUS;
        int yUpperBound = currentPosition.getY() + LOAD_RADIUS;

        // Loops through every tile and creates blocks based off the tile
        for (int i = xLowerBound; i <= xUpperBound; i++) {
            for (int j = yLowerBound; j <= yUpperBound; j++) {
                Position ijPos = new Position(i,j);
                if (worldMap.getTile(ijPos) != null) {
                    List<Block> blocks = worldMap.getTile(ijPos).getBlocks();
                    for (int k = 0; k < blocks.size(); k++) {
                        Box box = new Box(BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                        switch (blocks.get(k).getBlockType()) {
                            case "grass":
                                box.setMaterial(grassMat);
                                break;
                            case "stone":
                                box.setMaterial(stoneMat);
                                break;
                            case "wood":
                                box.setMaterial(woodMat);
                                break;
                            case "soil":
                                box.setMaterial(soilMat);
                                break;
                        }
                        box.setTranslateX(i*BLOCK_SIZE);
                        box.setTranslateZ(-j*BLOCK_SIZE);
                        box.setTranslateY(-k*BLOCK_SIZE);
                        root.getChildren().add(box);
                        if (k == (blocks.size() - 1)) {
                            addIndicators(i,j,k,ijPos);
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds exit indicators to the specified tile.
     * @param i - the i coordinate
     * @param j - the j coordinate
     * @param k - the k coordinate
     * @param ijPos - the position ijPos
     */
    private void addIndicators(int i, int j, int k, Position ijPos) {

        // Creates map containing positions for indicators corresponding to
        // each direction
        HashMap<String, Pair<Double, Double>> map = new HashMap<>();
        map.put("west", new Pair<>(-BLOCK_SIZE/6,(double)0));
        map.put("east", new Pair<>(BLOCK_SIZE/6,(double)0));
        map.put("north", new Pair<>((double)0,BLOCK_SIZE/6));
        map.put("south", new Pair<>((double)0,-BLOCK_SIZE/6));

        for (String dir : map.keySet()) {
            if (worldMap.getTile(ijPos).getExits().containsKey(dir)) {
                Cylinder ind = new Cylinder(BLOCK_SIZE/16, BLOCK_SIZE/8);
                ind.setMaterial(new PhongMaterial(Color.web("#00000066")));
                ind.setTranslateX((i +
                        map.get(dir).getKey()) * BLOCK_SIZE);
                ind.setTranslateZ((-j +
                        map.get(dir).getValue()) * BLOCK_SIZE);
                ind.setTranslateY((-k - BLOCK_SIZE/4) * BLOCK_SIZE);
                root.getChildren().add(ind);
            }
        }
    }

    /**
     * Creates a Sphere representing the player, moving it to the center of
     * the screen.
     */
    private void createPlayer() {
        player = new Sphere();
        player.setRadius(BLOCK_SIZE/2);
        player.setTranslateY((-worldMap.getTile(currentPosition).
                getBlocks().size()*BLOCK_SIZE));
        player.setTranslateX(currentPosition.getX()*BLOCK_SIZE);
        player.setTranslateZ(-currentPosition.getY()*BLOCK_SIZE);
        player.setMaterial(getMaterial("player"));
        root.getChildren().add(player);
    }

    /**
     * Creates a timeline for the rotation of the player.
     * @param direction - the direction to move in
     * @param rotateX - the rotation in the X axis
     * @param rotateZ - the rotation in the Z axis
     * @return a timeline with the specified rotations.
     */
    private Timeline createTimeline(String direction,
            Rotate rotateX, Rotate rotateZ) {
        Map<String, Pair<DoubleProperty, Integer>> timelineTranslations
                = new HashMap<>();
        timelineTranslations.put("west", new Pair<>(rotateZ.angleProperty(),
                -360));
        timelineTranslations.put("east", new Pair<>(rotateZ.angleProperty(),
                360));
        timelineTranslations.put("north", new Pair<>(rotateX.angleProperty(),
                -360));
        timelineTranslations.put("south", new Pair<>(rotateX.angleProperty(),
                360));

        player.getTransforms().setAll(rotateX, rotateZ);

        return new Timeline(
                new KeyFrame(Duration.seconds(0.25),
                        new KeyValue(timelineTranslations.get(direction).getKey(),
                                timelineTranslations.get(direction).getValue())));
    }

    /**
     * Create a camera and moves it to the correct position
     * @return A PerspectiveCamera
     */
    private PerspectiveCamera createCamera() {
        camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll (
                new Rotate(-45, Rotate.Y_AXIS),
                new Rotate(-30, Rotate.X_AXIS),
                new Translate(0, 0, -20));
        camera.setTranslateY((-worldMap.getTile(currentPosition).getBlocks().size())*BLOCK_SIZE);
        camera.setTranslateX(currentPosition.getX()*BLOCK_SIZE);
        camera.setTranslateZ(-currentPosition.getY()*BLOCK_SIZE);
        root.getChildren().add(camera);
        return camera;
    }

    /**
     * Updates the node height (eg for updating the Player's object when moving
     * up)
     * @param node - the input node
     */
    private void updateNodeHeight(Node node) {
        node.setTranslateY(-worldMap.getTile(currentPosition)
                .getBlocks().size()*BLOCK_SIZE);
    }

    /**
     * Creates a SubScene to add to the group.
     * @param group - the group to add the created subScene to.
     */
    private void create3DView(Group group) {
        root = new Group();
        subScene = new SubScene(root, 600,600,
                true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.LIGHTSKYBLUE);
        if (worldMap != null) {
            subScene.setCamera(createCamera());
        }
        group.getChildren().add(subScene);
    }

    /**
     * Creates the control grid using JavaFX.
     */
    private void createControlGrid() {
        Label blockLabel = new Label("Block");
        Label builderLabel = new Label("Builder");
        Label cameraLabel = new Label("Camera");

        Button upBuilderButton = new Button("▲");
        Button downBuilderButton = new Button("▼");
        Button leftBuilderButton = new Button("◀");
        Button rightBuilderButton = new Button("▶");

        Button upBlockButton = new Button("△");
        Button downBlockButton = new Button("▽");
        Button leftBlockButton = new Button("◁");
        Button rightBlockButton = new Button("▷");

        Button upRotateButton = new Button("+");
        Button downRotateButton = new Button("–");
        Button leftRotateButton = new Button("<");
        Button rightRotateButton = new Button(">");

        Button loadButton = new Button("Load");
        Button saveButton = new Button("Save");
        Button saveAsButton = new Button("Save As");
        Button digButton = new Button("Dig");

        buttonList.add(upBuilderButton);
        buttonList.add(downBuilderButton);
        buttonList.add(leftBuilderButton);
        buttonList.add(rightBuilderButton);

        buttonList.add(upBlockButton);
        buttonList.add(downBlockButton);
        buttonList.add(leftBlockButton);
        buttonList.add(rightBlockButton);

        buttonList.add(upRotateButton);
        buttonList.add(downRotateButton);
        buttonList.add(leftRotateButton);
        buttonList.add(rightRotateButton);

        buttonList.add(digButton);
        buttonList.add(loadButton);
        buttonList.add(saveButton);
        buttonList.add(saveAsButton);

        controlGrid.setPrefWidth(100);
        controlGrid.setAlignment(Pos.CENTER);
        controlGrid.setHgap(3);
        controlGrid.setVgap(10);

        GridPane.setHalignment(blockLabel, HPos.CENTER);
        GridPane.setHalignment(builderLabel, HPos.CENTER);
        GridPane.setHalignment(cameraLabel, HPos.CENTER);
        GridPane.setHalignment(loadButton, HPos.CENTER);
        GridPane.setHalignment(saveAsButton, HPos.CENTER);
        GridPane.setHalignment(saveButton, HPos.CENTER);
        GridPane.setHalignment(digButton, HPos.CENTER);

        controlGrid.add(digButton, 0, 0, 3,1);
        controlGrid.add(builderLabel, 0, 1, 3,1);
        controlGrid.add(upBuilderButton, 1,2);
        controlGrid.add(downBuilderButton, 1,4);
        controlGrid.add(leftBuilderButton, 0,3);
        controlGrid.add(rightBuilderButton, 2,3);
        controlGrid.add(blockLabel, 0, 5, 3,1);
        controlGrid.add(upBlockButton, 1,6);
        controlGrid.add(downBlockButton, 1,8);
        controlGrid.add(leftBlockButton, 0,7);
        controlGrid.add(rightBlockButton, 2,7);
        controlGrid.add(cameraLabel, 0, 9, 3, 1);
        controlGrid.add(upRotateButton, 1,10);
        controlGrid.add(downRotateButton, 1,12);
        controlGrid.add(leftRotateButton, 0, 11);
        controlGrid.add(rightRotateButton, 2, 11);
        controlGrid.add(loadButton, 0, 13, 3, 1);
        controlGrid.add(saveButton, 0, 14, 3, 1);
        controlGrid.add(saveAsButton, 0, 15, 3, 1);
    }

    /**
     * Sets the buttons active (except for Load)
     */
    private void setButtonsActive() {
        for (Button button : buttonList) {
            button.setDisable(false);
        }
    }

    /**
     * Sets the buttons inactive (except for Load)
     */
    private void setButtonsInactive() {
        for (Button button : buttonList) {
            if (!button.getText().equals("Load")) {
                button.setDisable(true);
            }
        }
    }

    /**
     * Updates inventory items based off the builder's inventory
     * @param blockGrid - the grid pane to add the inventory listing to
     */
    private void createInventoryItems(GridPane blockGrid) {
        Image grassImg = new Image("images/sprites/grass.jpg");
        Image soilImg = new Image("images/sprites/soil.jpg");
        Image woodImg = new Image("images/sprites/wood.jpg");
        Image stoneImg = new Image("images/sprites/stone.jpg");

        if (worldMap != null) {
            for (int i = 0; i < worldMap.getBuilder().getInventory().size(); i++) {
                final int index = i;
                Button button = new Button();
                switch (worldMap.getBuilder().getInventory().get(i).getBlockType()) {
                    case "grass":
                        button.setGraphic(new ImageView(grassImg));
                        break;
                    case "stone":
                        button.setGraphic(new ImageView(stoneImg));
                        break;
                    case "soil":
                        button.setGraphic(new ImageView(soilImg));
                        break;
                    case "wood":
                        button.setGraphic(new ImageView(woodImg));
                        break;
                }
                button.setOnAction(e -> placeBlock(index));
                blockGrid.add(button, 0, i+1);
            }
        }
    }

    /**
     * Creates the inventory listing from createInventoryItems.
     */
    private void createInventoryListing() {
        GridPane blockGrid = new GridPane();
        blockGrid.setGridLinesVisible(false);
        blockGrid.setPadding(new Insets(BUTTON_PADDING));
        blockGrid.setHgap(BUTTON_PADDING);
        blockGrid.setVgap(BUTTON_PADDING);

        Label inventoryLabel = new Label("Inventory");
        inventoryLabel.setId("inventoryLabel");
        inventoryLabel.setMaxWidth(Double.MAX_VALUE);
        inventoryLabel.setAlignment(Pos.CENTER);
        blockGrid.add(inventoryLabel, 0,0);

        createInventoryItems(blockGrid);
        scrollPane.setContent(blockGrid);

    }

    /**
     * Creates a popup with the given message as input.
     * @param msg - the message to display
     */
    private void createPopup(String msg) {
        Alert alert = new Alert(AlertType.INFORMATION);
        if (msg.contains("Error:")) {
            alert.setTitle("Error");
        } else {
            alert.setTitle("Alert");
        }
        alert.setHeaderText(null);
        alert.setContentText(msg);

        alert.showAndWait();
    }

    /**
     * Update the position to a new position.
     * @param deltaX - the change in X coordinate
     * @param deltaZ - the change in Z coordinate
     */
    private void updatePosition(int deltaX, int deltaZ) {
        camera.setTranslateX(camera.getTranslateX()-(BLOCK_SIZE*deltaX));
        camera.setTranslateZ(camera.getTranslateZ()-(BLOCK_SIZE*deltaZ));

        player.setTranslateX(player.getTranslateX()-(BLOCK_SIZE*deltaX));
        player.setTranslateZ(player.getTranslateZ()-(BLOCK_SIZE*deltaZ));
    }

    /**
     * Updates the 3D view.
     * @param deltaX - the change in X coordinate
     * @param deltaZ - the change in Z coordinate
     */
    private void updateEverything(int deltaX, int deltaZ) {
        updateWorld();
        updatePosition(deltaX, deltaZ);
        updateNodeHeight(player);
        updateNodeHeight(camera);
    }

    /**
     * Add components to the masterGrid.
     */
    private void addComponents() {
        root = new Group();
        group = new Group();
        scrollPane = new ScrollPane();
        controlGrid = new GridPane();

        // 3D
        createWorld("");
        subScene = new SubScene(root, 600,600,
                true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.LIGHTSKYBLUE);
        group.getChildren().add(subScene);

        // Creates inventory pane
        scrollPane.setPrefSize(100, 600);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        createInventoryListing();

        createControlGrid();
        setButtonsInactive();

        masterGrid.add(controlGrid, 0,0,1,1);
        masterGrid.add(group, 1, 0, 1, 1);
        masterGrid.add(scrollPane, 2, 0, 1, 1);
    }

    /**
     * Move player in the direction provided
     * @param direction - the direction to move the player in
     */
    public void movePlayer(String direction) {

        Rotate rotateZ = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);
        Rotate rotateX = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);

        HashMap<String, Pair<Integer, Integer>> directionDeltas = new HashMap<>();
        directionDeltas.put("west", new Pair<>(1,0));
        directionDeltas.put("east", new Pair<>(-1,0));
        directionDeltas.put("north", new Pair<>(0,-1));
        directionDeltas.put("south", new Pair<>(0,1));

        for (String dir : directionDeltas.keySet()) {
            if (direction.equals(dir)) {
                try {
                    worldMap.getBuilder()
                            .moveTo(worldMap.getTile(currentPosition)
                                    .getExits().get(direction));
                    createTimeline(direction, rotateX, rotateZ).playFromStart();
                    setCurrentPosition(currentPosition.getX() -
                                    directionDeltas.get(dir).getKey(),
                            currentPosition.getY() +
                                    directionDeltas.get(dir).getValue());
                    updateEverything(directionDeltas.get(dir).getKey(),
                            directionDeltas.get(dir).getValue());
                } catch (Exception e) {
                    createPopup("Can't move this way");
                }
            }
        }
    }

    /**
     * Move the block in the direction provided.
     * @param direction - the direction to move the block
     */
    public void moveBlock(String direction) {
        try {
            worldMap.getBuilder().getCurrentTile().moveBlock(direction);
            updateEverything(0,0);
        } catch (Exception e) {
            createPopup("Can't move block");
        }
    }

    /**
     * Dig the block that the builder is currently on.
     */
    public void dig() {
        try {
            worldMap.getBuilder().digOnCurrentTile();
            updateEverything(0, 0);
            createInventoryListing();
        } catch (Exception e) {
            createPopup("Can't dig on current tile");
        }
    }

    /**
     * Place the block on the tile that the builder is currently on.
     * @param index - the index from the inventory to place
     */
    public void placeBlock(int index) {
        try {
            worldMap.getBuilder().dropFromInventory(index);
            updateEverything(0, 0);
            createInventoryListing();
        } catch (Exception e) {
            createPopup("Can't place block on current tile");
        }

    }

    /**
     * Rotate the camera in the direction provided.
     * @param direction - the direction to rotate the camera
     */
    public void rotateCamera(String direction) {
        int angle = 0;
        switch (direction) {
            case "west":
                angle = -45;
                break;
            case "east":
                angle = 45;
                break;
        }
        ((Rotate) camera.getTransforms().get(0)).setAngle(
                ((Rotate) camera.getTransforms().get(0)).getAngle() + angle);
    }

    /**
     * Zoom the camera in or out, depending on the direction.
     * @param direction - the direction to zoom the camera (in/out)
     */
    public void zoomCamera(String direction) {
        int dz = 0;
        switch (direction) {
            case "in":
                dz = 5;
                break;
            case "out":
                dz = -5;
                break;
        }
        if (camera.getTransforms().get(2).getTz() + dz >= -55 &&
                camera.getTransforms().get(2).getTz() + dz <= -5) {
            ((Translate) camera.getTransforms().get(2)).setZ(
                    ((Translate) camera.getTransforms().get(2)).getZ() + dz);
        }
    }

    /**
     * Opens a file dialog to load a map.
     */
    public void loadMap() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Map");
        currentFile = fileChooser.showOpenDialog(scene.getWindow());
        try {
            if (currentFile != null) {
                String filename = currentFile.getAbsolutePath();
                createWorld(filename);
                create3DView(group);
                createPlayer();
                updateWorld();
                createInventoryListing();
                setButtonsActive();
            }
        } catch (Exception e) {
            createPopup("Error: " + e);
            return;
        }
    }

    /**
     * Saves the map in the current file location.
     */
    public void saveMap() {
        try {
            worldMap.saveMap(currentFile.getAbsolutePath());
        } catch (IOException e) {
            createPopup("Error: IOException");
            return;
        }

    }

    /**
     * Opens a file dialog to save a map.
     */
    public void saveAsMap() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Map");
        File outputFile = fileChooser.showSaveDialog(scene.getWindow());
        if (outputFile != null) {
            try {
                worldMap.saveMap(outputFile.getAbsolutePath());
            } catch (IOException e) {
                createPopup("Error: IOException");
                return;
            }
        }
    }

    /**
     * Adds a handler to the button (to be handled by the Controller class)
     * @param handle - the handler to set the button's action to
     */
    public void addButtonHandler(EventHandler<ActionEvent> handle) {
        for (Button button : buttonList) {
            button.setOnAction(handle);
        }
    }

}

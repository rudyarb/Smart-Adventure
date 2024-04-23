package views;

import AdventureModel.AdventureGame;
import AdventureModel.AdventureObject;
import AdventureModel.Question;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.layout.*;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.event.EventHandler; //you will need this too!
import javafx.scene.AccessibleRole;

import java.awt.event.TextEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

/**
 * Class AdventureGameView.
 *
 * This is the Class that will visualize your model.
 * You are asked to demo your visualization via a Zoom
 * recording. Place a link to your recording below.
 *
 * Youtube LINK: <https://youtu.be/bGKfN4gc0iA>
 * PASSWORD: <N/A>
 */
public class AdventureGameView {

    AdventureGame model; //model of the game
    Stage stage; //stage on which all is rendered
    Button saveButton, loadButton, helpButton; //buttons
    Boolean helpToggle = false; //is help on display?


    GridPane gridPane = new GridPane(); //to hold images and buttons
    Label roomDescLabel = new Label(); //to hold room description and/or instructions
    VBox objectsInRoom = new VBox(); //to hold room items
    VBox objectsInInventory = new VBox(); //to hold inventory items
    ImageView roomImageView; //to hold room image
    TextField inputTextField; //for user input

    private MediaPlayer mediaPlayer; //to play audio
    private boolean mediaPlaying; //to know if the audio is playing

    /**
     * Adventure Game View Constructor
     * __________________________
     * Initializes attributes
     */
    public AdventureGameView(AdventureGame model, Stage stage) {
        this.model = model;
        this.stage = stage;
        intiUI();
    }

    /**
     * Initialize the UI
     */
    public void intiUI() {

        // setting up the stage
        this.stage.setTitle("navinarb's Adventure Game");

        //Inventory + Room items
        objectsInInventory.setSpacing(10);
        objectsInInventory.setAlignment(Pos.TOP_CENTER);
        objectsInRoom.setSpacing(10);
        objectsInRoom.setAlignment(Pos.TOP_CENTER);

        // GridPane, anyone?
        gridPane.setPadding(new Insets(20));
        gridPane.setBackground(new Background(new BackgroundFill(
                Color.valueOf("#000000"),
                new CornerRadii(0),
                new Insets(0)
        )));

        //Three columns, three rows for the GridPane
        ColumnConstraints column1 = new ColumnConstraints(150);
        ColumnConstraints column2 = new ColumnConstraints(650);
        ColumnConstraints column3 = new ColumnConstraints(150);
        column3.setHgrow( Priority.SOMETIMES ); //let some columns grow to take any extra space
        column1.setHgrow( Priority.SOMETIMES );

        // Row constraints
        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints( 550 );
        RowConstraints row3 = new RowConstraints();
        row1.setVgrow( Priority.SOMETIMES );
        row3.setVgrow( Priority.SOMETIMES );

        gridPane.getColumnConstraints().addAll( column1 , column2 , column1 );
        gridPane.getRowConstraints().addAll( row1 , row2 , row1 );

        // Buttons
        saveButton = new Button("Save");
        saveButton.setId("Save");
        customizeButton(saveButton, 100, 50);
        makeButtonAccessible(saveButton, "Save Button", "This button saves the game.", "This button saves the game. Click it in order to save your current progress, so you can play more later.");
        addSaveEvent();

        loadButton = new Button("Load");
        loadButton.setId("Load");
        customizeButton(loadButton, 100, 50);
        makeButtonAccessible(loadButton, "Load Button", "This button loads a game from a file.", "This button loads the game from a file. Click it in order to load a game that you saved at a prior date.");
        addLoadEvent();

        helpButton = new Button("Instructions");
        helpButton.setId("Instructions");
        customizeButton(helpButton, 200, 50);
        makeButtonAccessible(helpButton, "Help Button", "This button gives game instructions.",  this.getHelpText());
        addInstructionEvent();

        HBox topButtons = new HBox();
        topButtons.getChildren().addAll(saveButton, helpButton, loadButton);
        topButtons.setSpacing(10);
        topButtons.setAlignment(Pos.CENTER);

        inputTextField = new TextField();
        inputTextField.setFont(new Font("Arial", 16));
        inputTextField.setFocusTraversable(true);

        inputTextField.setAccessibleRole(AccessibleRole.TEXT_AREA);
        inputTextField.setAccessibleRoleDescription("Text Entry Box");
        inputTextField.setAccessibleText("Enter commands in this box.");
        inputTextField.setAccessibleHelp("This is the area in which you can enter commands you would like to play.  Enter a command and hit return to continue.");
        addTextHandlingEvent(); //attach an event to this input field

        //labels for inventory and room items
        Label objLabel =  new Label("Type of Questions");
        objLabel.setAlignment(Pos.CENTER);
        objLabel.setStyle("-fx-text-fill: white;");
        objLabel.setFont(new Font("Arial", 16));

        Label invLabel =  new Label("Points");
        invLabel.setAlignment(Pos.CENTER);
        invLabel.setStyle("-fx-text-fill: white;");
        invLabel.setFont(new Font("Arial", 16));

        //add all the widgets to the GridPane
        gridPane.add( objLabel, 0, 0, 1, 1 );  // Add label
        gridPane.add( topButtons, 1, 0, 1, 1 );  // Add buttons
        gridPane.add( invLabel, 2, 0, 1, 1 );  // Add label

        Label commandLabel = new Label("What would you like to do?");
        commandLabel.setStyle("-fx-text-fill: white;");
        commandLabel.setFont(new Font("Arial", 16));

        updateScene(""); //method displays an image and whatever text is supplied
        updateItems(); //update items shows inventory and objects in rooms

        // adding the text area and submit button to a VBox
        VBox textEntry = new VBox();
        textEntry.setStyle("-fx-background-color: #000000;");
        textEntry.setPadding(new Insets(20, 20, 20, 20));
        textEntry.getChildren().addAll(commandLabel, inputTextField);
        textEntry.setSpacing(10);
        textEntry.setAlignment(Pos.CENTER);
        gridPane.add( textEntry, 0, 2, 3, 1 );

        // Render everything
        var scene = new Scene( gridPane ,  1000, 800);
        scene.setFill(Color.BLACK);
        this.stage.setScene(scene);
        this.stage.setResizable(false);
        this.stage.show();

    }


    /**
     * makeButtonAccessible
     * __________________________
     * For information about ARIA standards, see
     * https://www.w3.org/WAI/standards-guidelines/aria/
     *
     * @param inputButton the button to add screenreader hooks to
     * @param name ARIA name
     * @param shortString ARIA accessible text
     * @param longString ARIA accessible help text
     */
    public static void makeButtonAccessible(Button inputButton, String name, String shortString, String longString) {
        inputButton.setAccessibleRole(AccessibleRole.BUTTON);
        inputButton.setAccessibleRoleDescription(name);
        inputButton.setAccessibleText(shortString);
        inputButton.setAccessibleHelp(longString);
        inputButton.setFocusTraversable(true);
    }

    /**
     * customizeButton
     * __________________________
     *
     * @param inputButton the button to make stylish :)
     * @param w width
     * @param h height
     */
    private void customizeButton(Button inputButton, int w, int h) {
        inputButton.setPrefSize(w, h);
        inputButton.setFont(new Font("Arial", 16));
        inputButton.setStyle("-fx-background-color: #17871b; -fx-text-fill: white;");
    }

    /**
     * addTextHandlingEvent
     * __________________________
     * Add an event handler to the inputTextField attribute
     *
     * Your event handler should respond when users
     * hits the ENTER or TAB KEY. If the user hits
     * the ENTER Key, strip white space from the
     * input to inputTextField and pass the stripped
     * string to submitEvent for processing.
     *
     * If the user hits the TAB key, move the focus
     * of the scene onto any other node in the scene
     * graph by invoking requestFocus method.
     */
    private void addTextHandlingEvent() {
        inputTextField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER)  {
                submitEvent(inputTextField.getText().strip());
                inputTextField.clear();
            } else if (keyEvent.getCode() == KeyCode.TAB) {
                gridPane.requestFocus();
                inputTextField.clear();
            }
        });
    }

    /**
     * submitEvent
     * __________________________
     *
     * @param text the command that needs to be processed
     */
    private void submitEvent(String text) {

        text = text.strip(); //get rid of white space
        stopArticulation(); //if speaking, stop

        if (text.equalsIgnoreCase("LOOK") || text.equalsIgnoreCase("L")) {
            String roomDesc = this.model.getPlayer().getCurrentRoom().getRoomDescription();

            roomDescLabel.setText(roomDesc);
            articulateRoomDescription(); //all we want, if we are looking, is to repeat description.
            return;
        }
        else if (text.equalsIgnoreCase("HELP") || text.equalsIgnoreCase("H")) {
            showInstructions();
            return;
        }

        //try to move!
        String output = this.model.interpretAction(text); //process the command!

        if (output == null || (!output.equals("GAME OVER") && !output.equals("FORCED") && !output.equals("HELP") && !output.equals("CORRECT!") && !output.startsWith("INCORRECT"))) {
            updateScene(output);
            updateItems();
        }
        else if (output.equals("GAME OVER")) {
            updateScene("");
            updateItems();
            PauseTransition pause = new PauseTransition(Duration.seconds(10));
            pause.setOnFinished(event -> {
                Platform.exit();
            });
            pause.play();
        }
        else if (output.equals("FORCED")) {
            //write code here to handle "FORCED" events!
            //Your code will need to display the image in the
            //current room and pause, then transition to
            //the forced room.
            updateScene(this.model.getPlayer().getCurrentRoom().getRoomDescription());
            updateItems();
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(event -> {
                // Move and update the display as well
                this.submitEvent(output);
            });
            pause.play();
        }
        else if (output.equals("CORRECT!")) {
            this.objectsInInventory.getChildren().clear();

            String currPoints = String.valueOf(this.model.getPlayer().getPoints());

            Label pointsLabel = new Label(currPoints);
            Font font = new Font("Arial", 35);
            pointsLabel.setFont(font);

            Button pointsButton = new Button(String.valueOf(this.model.getPlayer().getPoints()));
            pointsButton.setPrefSize(100,100);
            pointsButton.setStyle("-fx-font-size: 40px;");

            makeButtonAccessible(pointsButton, "", "You have" + this.model.getPlayer().getPoints() + "points", "You have" +  this.model.getPlayer().getPoints() + "points");

            this.objectsInInventory.getChildren().add(pointsButton);
            this.roomDescLabel.setText(output);
        }
        else if (output.startsWith("INCORRECT")) {
            this.objectsInInventory.getChildren().clear();

            String currPoints = String.valueOf(this.model.getPlayer().getPoints());

            Label pointsLabel = new Label(currPoints);
            Font font = new Font("Arial", 35);
            pointsLabel.setFont(font);

            Button pointsButton = new Button(String.valueOf(this.model.getPlayer().getPoints()));
            pointsButton.setPrefSize(100,100);
            pointsButton.setStyle("-fx-font-size: 40px;");

            if (this.model.getPlayer().getPoints() < 0) {
                makeButtonAccessible(pointsButton, "", "You have" + this.model.getPlayer().getPoints() + "points", "You have minus" +  this.model.getPlayer().getPoints() + "points");
            }
            else {
                makeButtonAccessible(pointsButton, "", "You have" + this.model.getPlayer().getPoints() + "points", "You have" +  this.model.getPlayer().getPoints() + "points");
            }

            this.objectsInInventory.getChildren().add(pointsButton);

            this.roomDescLabel.setText(output);
        }
    }


    /**
     * showCommands
     * __________________________
     *
     * update the text in the GUI (within roomDescLabel)
     * to show all the moves that are possible from the
     * current room.
     */
    private void showCommands() {
        String res = this.model.getPlayer().getCurrentRoom().getCommands();
        roomDescLabel.setText("The possible moves from this room are: " + res);  // update the text in the GUI (within roomDescLabel
        roomDescLabel.setAlignment(Pos.BOTTOM_CENTER); // beneath room image
    }


    /**
     * updateScene
     * __________________________
     *
     * Show the current room, and print some text below it.
     * If the input parameter is not null, it will be displayed
     * below the image.
     * Otherwise, the current room description will be dispplayed
     * below the image.
     *
     * @param textToDisplay the text to display below the image.
     */
    public void updateScene(String textToDisplay) {

        getRoomImage(); //get the image of the current room
        formatText(textToDisplay); //format the text to display
        roomDescLabel.setPrefWidth(500);
        roomDescLabel.setPrefHeight(500);
        roomDescLabel.setTextOverrun(OverrunStyle.CLIP);
        roomDescLabel.setWrapText(true);
        VBox roomPane = new VBox(roomImageView,roomDescLabel);
        roomPane.setPadding(new Insets(10));
        roomPane.setAlignment(Pos.TOP_CENTER);
        roomPane.setStyle("-fx-background-color: #000000;");

        gridPane.add(roomPane, 1, 1);
        stage.sizeToScene();

        //finally, articulate the description
        if (textToDisplay == null || textToDisplay.isBlank()) articulateRoomDescription();
    }

    /**
     * formatText
     * __________________________
     *
     * Format text for display.
     *
     * @param textToDisplay the text to be formatted for display.
     */
    private void formatText(String textToDisplay) {
        if (textToDisplay == null || textToDisplay.isBlank()) {

            String roomDesc = this.model.getPlayer().getCurrentRoom().getRoomDescription() + "\n";

            roomDescLabel.setText(roomDesc);
        } else roomDescLabel.setText(textToDisplay);
        roomDescLabel.setStyle("-fx-text-fill: white;");
        roomDescLabel.setFont(new Font("Arial", 16));
        roomDescLabel.setAlignment(Pos.CENTER);
    }

    /**
     * getRoomImage
     * __________________________
     *
     * Get the image for the current room and place
     * it in the roomImageView
     */
    private void getRoomImage() {

        int roomNumber = this.model.getPlayer().getCurrentRoom().getRoomNumber();
        String roomImage = this.model.getDirectoryName() + "/room-images/" + roomNumber + ".png";

        Image roomImageFile = new Image(roomImage);
        roomImageView = new ImageView(roomImageFile);
        roomImageView.setPreserveRatio(true);
        roomImageView.setFitWidth(400);
        roomImageView.setFitHeight(400);

        //set accessible text
        roomImageView.setAccessibleRole(AccessibleRole.IMAGE_VIEW);
        roomImageView.setAccessibleText(this.model.getPlayer().getCurrentRoom().getRoomDescription());
        roomImageView.setFocusTraversable(true);
    }

    /**
     * updateItems
     * __________________________
     *
     * This method is partially completed, but you are asked to finish it off.
     *
     * The method should populate the objectsInRoom and objectsInInventory Vboxes.
     * Each Vbox should contain a collection of nodes (Buttons, ImageViews, you can decide)
     * Each node represents a different object.
     *
     * Images of each object are in the assets
     * folders of the given adventure game.
     */
    public void updateItems() {
        //write some code here to add images of objects in a given room to the objectsInRoom Vbox
        //write some code here to add images of objects in a player's inventory to the objectsInInventory Vbox

        this.objectsInRoom.getChildren().clear(); // clear the Vbox

//        for (AdventureObject obj :  // add images of objects in a given room to the objectsInRoom Vbox
//                this.model.getPlayer().getCurrentRoom().objectsInRoom) {

        Image image1 = new Image(this.model.getDirectoryName() + "/objectImages/" + "EASY" + ".JPG");  // gets objects name and puts into image
        ImageView imageView1 = new ImageView(image1);  // makes imageview of image
        imageView1.setAccessibleText("EASY");
        imageView1.setFitWidth(100);  // sets width to 100
        imageView1.setFitHeight(100);  // sets height to 100

        Button button1 = new Button("EASY", imageView1);  // makes a button of the image

        makeButtonAccessible(button1, button1.getText(), button1.getAccessibleText(), button1.getAccessibleHelp());

        this.objectsInRoom.getChildren().add(button1); // adds button1 to the Vbox

        Image image2 = new Image(this.model.getDirectoryName() + "/objectImages/" + "HARD" + ".JPG");  // gets objects name and puts into image
        ImageView imageView2 = new ImageView(image2);  // makes imageview of image
        imageView2.setAccessibleText("HARD");
        imageView2.setFitWidth(100);  // sets width to 100
        imageView2.setFitHeight(100);  // sets height to 100

        Button button2 = new Button("HARD", imageView2);  // makes a button of the image

        makeButtonAccessible(button2, button2.getText(), button2.getAccessibleText(), button2.getAccessibleHelp());

        this.objectsInRoom.getChildren().add(button2); // adds button2 to the Vbox

        // Adds event handler for EASY button
        button1.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            Random random = new Random();

            // Generate random integer between 0 and 2
            int index = random.nextInt(3);

            Question question = this.model.getPlayer().getCurrentRoom().questionsInRoom.get(index);
            this.model.getPlayer().getCurrentRoom().currentQuestion = question;  // Sets current question to question randomly generated

//            "Write Java code that sets the font of the text of a Button in JavaFX" ChatGPT, 1 December. version, OpenAI, 1 Dec. 2023,
//                    chat.openai.com/chat.

            Button questionButton = new Button(question.getQuestion());
            Font boldFont = Font.font("Arial", FontWeight.BOLD, 14);  // imported FontWeight
            questionButton.setFont(boldFont);

            questionButton.setPrefSize(600, 150);
            makeButtonAccessible(questionButton, question.getQuestion(), question.getQuestion(), question.getQuestion());


            gridPane.add(questionButton, 1, 1);
        });

        // Adds event handler for HARD button
        button2.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            Random random = new Random();

            // Generate random integer between 3 and 6
            int index = random.nextInt(3) + 3;

            Question question = this.model.getPlayer().getCurrentRoom().questionsInRoom.get(index);
            this.model.getPlayer().getCurrentRoom().currentQuestion = question;  // Sets current question to question randomly generated

            Button questionButton = new Button(question.getQuestion());
            Font boldFont = Font.font("Arial", FontWeight.BOLD, 14);
            questionButton.setFont(boldFont);

            // TODO cite GPT

            questionButton.setPrefSize(600, 150);
            makeButtonAccessible(questionButton, question.getQuestion(), question.getQuestion(), question.getQuestion());

            gridPane.add(questionButton, 1, 1);
        });

        ScrollPane scO = new ScrollPane(objectsInRoom);
        scO.setPadding(new Insets(10));
        scO.setStyle("-fx-background: #000000; -fx-background-color:transparent;");
        scO.setFitToWidth(true);
        gridPane.add(scO, 0, 1);

        ScrollPane scI = new ScrollPane(objectsInInventory);
        scI.setFitToWidth(true);
        scI.setStyle("-fx-background: #000000; -fx-background-color:transparent;");
        gridPane.add(scI, 2, 1);
    }


    private String getHelpText() {
        try {
            BufferedReader buff = new BufferedReader(new FileReader("Games/TinyGame/help.txt"));
            String line = buff.readLine(); // Points to 1st line
            String toReturn = "";
            while (line != null) {
                toReturn += line + "\n";
                line = buff.readLine();
            }
            return toReturn;
        }
        catch (IOException e) {
            return "Help Message not able to be displayed";
        }
    }

    /*
     * Show the game instructions.
     *
     * If helpToggle is FALSE:
     * -- display the help text in the CENTRE of the gridPane (i.e. within cell 1,1)
     * -- use whatever GUI elements to get the job done!
     * -- set the helpToggle to TRUE
     * -- REMOVE whatever nodes are within the cell beforehand!
     *
     * If helpToggle is TRUE:
     * -- redraw the room image in the CENTRE of the gridPane (i.e. within cell 1,1)
     * -- set the helpToggle to FALSE
     * -- Again, REMOVE whatever nodes are within the cell beforehand!
     */
    public void showInstructions() {

        if (!helpToggle) {  // if false
            gridPane.getChildren().remove(1, 1);

            String helpMessage = this.getHelpText();
            Label helpLabel = new Label(helpMessage);
            helpLabel.setStyle("-fx-text-fill: white");
            helpLabel.setStyle("-fx-text-fill: white");

            VBox instructionPane = new VBox(helpLabel);
            instructionPane.setPadding(new Insets(10));
            instructionPane.setAlignment(Pos.TOP_CENTER);
            instructionPane.setStyle("-fx-background-color: #000000;");


            gridPane.add(instructionPane, 1, 1);
            helpToggle = true;  // set the helpToggle to TRUE
        }

        else {
            gridPane.getChildren().remove(1,1);

            VBox roomPane = new VBox(roomImageView,roomDescLabel);
            roomPane.setPadding(new Insets(10));
            roomPane.setAlignment(Pos.TOP_CENTER);
            roomPane.setStyle("-fx-background-color: #000000;");

            gridPane.add(roomPane, 1, 1);
            helpToggle = false;  // set the helpToggle to FALSE
        }
    }

    /**
     * This method handles the event related to the
     * help button.
     */
    public void addInstructionEvent() {
        helpButton.setOnAction(e -> {
            stopArticulation(); //if speaking, stop
            showInstructions();
        });
    }

    /**
     * This method handles the event related to the
     * save button.
     */
    public void addSaveEvent() {
        saveButton.setOnAction(e -> {
            gridPane.requestFocus();
            SaveView saveView = new SaveView(this);
        });
    }

    /**
     * This method handles the event related to the
     * load button.
     */
    public void addLoadEvent() {
        loadButton.setOnAction(e -> {
            gridPane.requestFocus();
            LoadView loadView = new LoadView(this);
        });
    }


    /**
     * This method articulates Room Descriptions
     * gets it from the roomSounds folder which countains voice recordings using an online
     * text reader
     */
    public void articulateRoomDescription() {
        String musicFile;
        String adventureName = this.model.getDirectoryName();
        String roomName = this.model.getPlayer().getCurrentRoom().getRoomName();

        musicFile = "./" + adventureName + "/roomSound/" + roomName + ".mp3";

        Media sound = new Media(new File(musicFile).toURI().toString());

        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
        mediaPlaying = true;

    }

    /**
     * This method stops articulations
     * (useful when transitioning to a new room or loading a new game)
     */
    public void stopArticulation() {
        if (mediaPlaying) {
            mediaPlayer.stop(); //shush!
            mediaPlaying = false;
        }
    }
}

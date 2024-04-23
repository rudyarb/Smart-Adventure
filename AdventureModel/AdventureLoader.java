package AdventureModel;

import javax.management.AttributeList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class AdventureLoader. Loads an adventure from files.
 */
public class AdventureLoader {

    private AdventureGame game; //the game to return
    private String adventureName; //the name of the adventure

    /**
     * Adventure Loader Constructor
     * __________________________
     * Initializes attributes
     * @param game the game that is loaded
     * @param directoryName the directory in which game files live
     */
    public AdventureLoader(AdventureGame game, String directoryName) {
        this.game = game;
        this.adventureName = directoryName;
    }

     /**
     * Load game from directory
     */
    public void loadGame() throws IOException {
        parseRooms();
        parseQuestions(); //
        parseSynonyms(); //
        this.game.setHelpText(parseOtherFile("help"));
    }

     /**
     * Parse Rooms File
     */
    private void parseRooms() throws IOException {

        int roomNumber;

        String roomFileName = this.adventureName + "/rooms.txt";
        BufferedReader buff = new BufferedReader(new FileReader(roomFileName));

        while (buff.ready()) {

            String currRoom = buff.readLine(); // first line is the number of a room

            roomNumber = Integer.parseInt(currRoom); //current room number

            // now need to get room name
            String roomName = buff.readLine();

            // now we need to get the description
            String roomDescription = "";
            String line = buff.readLine();
            while (!line.equals("-----")) {
                roomDescription += line + "\n";
                line = buff.readLine();
            }
            roomDescription += "\n";

            // now we make the room object
            Room room = new Room(roomName, roomNumber, roomDescription, adventureName);

            // now we make the motion table
            line = buff.readLine(); // reads the line after "-----"
            while (line != null && !line.equals("")) {
                String[] part = line.split(" \s+"); // have to use regex \\s+ as we don't know how many spaces are between the direction and the room number
                String direction = part[0];
                String dest = part[1];
                if (dest.contains("/")) {
                    String[] blockedPath = dest.split("/");
                    String dest_part = blockedPath[0];
                    String object = blockedPath[1];
                    Passage entry = new Passage(direction, dest_part, object);
                    room.getMotionTable().addDirection(entry);
                }
                else {
                    Passage entry = new Passage(direction, dest);
                    room.getMotionTable().addDirection(entry);
                }
                line = buff.readLine();
            }
            this.game.getRooms().put(room.getRoomNumber(), room);
        }
    }

     /**
     * Parse Questions File
      * Read the first 8 lines of a question (separator included_
      * Store different lines into different arguments
      * Answer in questions.txt will contain * after the word or phrase
      * Reassign options in different positions (for it to be random)
      * Create a Question object and put it in the rooms dictionary with the room number
      * being the key
     */
    public void parseQuestions() throws IOException {

        String objectFileName = this.adventureName + "/questions.txt";
        BufferedReader buff = new BufferedReader(new FileReader(objectFileName));

        while (buff.ready()) {
            String questionType = buff.readLine();
            String questionLocation = buff.readLine();
            String questionText = buff.readLine();
            String option1 = buff.readLine();
            String option2 = buff.readLine();
            String option3 = buff.readLine();
            String option4 = buff.readLine();
            String separator = buff.readLine();

            if (separator != null && !separator.isEmpty())
                System.out.println("Formatting Error!");

            ArrayList<String> options = new ArrayList<>();
            options.add(option1);
            options.add(option2);
            options.add(option3);
            options.add(option4);

            String answer = "";
            for (String option:
                 options) {
                if (option.endsWith("*")) {
                    // Know this is the answer
                    answer = option;
                }
            }

            options.remove(answer);  // removes the answer from the arraylist
            answer = answer.substring(0,answer.length()-1);  // removes last char

            String op1 = "";
            String op2 = "";
            String op3 = "";

            for (int i = 1; i < 4; i++) {
                if (i == 1) {
                    op1 = options.get(i-1);
                }
                else if (i == 2) {
                    op2 = options.get(i-1);
                }
                else {
                    op3 = options.get(i-1);
                }
            }

            int i = Integer.parseInt(questionLocation);
            Room location = this.game.getRooms().get(i);
            Question question = new Question(questionType, questionText, answer, i, op1, op2, op3);
            location.addQuestion(question);

        }

    }

     /**
     * Parse Synonyms File
     */
    public void parseSynonyms() throws IOException {
        String synonymsFileName = this.adventureName + "/synonyms.txt";
        BufferedReader buff = new BufferedReader(new FileReader(synonymsFileName));
        String line = buff.readLine();
        while(line != null){
            String[] commandAndSynonym = line.split("=");
            String command1 = commandAndSynonym[0];
            String command2 = commandAndSynonym[1];
            this.game.getSynonyms().put(command1,command2);
            line = buff.readLine();
        }

    }

    /**
     * Parse Files other than Rooms, Objects and Synonyms
     *
     * @param fileName the file to parse
     */
    public String parseOtherFile(String fileName) throws IOException {
        String text = "";
        fileName = this.adventureName + "/" + fileName + ".txt";
        BufferedReader buff = new BufferedReader(new FileReader(fileName));
        String line = buff.readLine();
        while (line != null) { // while not EOF
            text += line+"\n";
            line = buff.readLine();
        }
        return text;
    }

}

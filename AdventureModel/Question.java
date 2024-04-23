package AdventureModel;

import java.io.Serializable;
import java.util.ArrayList;

public class Question {

    private final String type;
    private final String op1;
    private final String op2;
    private final String op3;
    private final String text; // the text of the question
    private final String answer; // the question's answer
    private final int roomNumber; // room that question is in

    public Question(String type, String text, String answer, int roomNumber, String op1, String op2, String op3) {
        this.op1 = op1;
        this.op2 = op2;
        this.op3 = op3;
        this.type = type;
        this.text = text;
        this.answer = answer;
        this.roomNumber = roomNumber;
    }

    /**
     * Returns the text of the question, the options of the question, as well as
     * a message stating what options the user can input into the textfield
     *
     * @return The string representation of the question
     */
    public String getQuestion() {
        String toReturn = "";

        if (this.roomNumber == 2 || this.roomNumber == 5) { // Answer is B
            toReturn += this.text + "\n" + "A)  " + this.op1 + "\n" + "B)  " + this.answer + "\n" + "C)  " + this.op2 + "\n" + "D)  " + this.op3 + "\n";
        }
        else if (this.roomNumber == 3 || this.roomNumber == 6) { // Answer is D
            toReturn += this.text + "\n" + "A)  " + this.op1 + "\n" + "B)  " + this.op3 + "\n" + "C)  " + this.op2 + "\n" + "D)  " + this.answer + "\n";
        }
        else {  // roomNumber is 4, answer s A
            toReturn += this.text + "\n" + "A)  " + this.answer + "\n" + "B)  " + this.op3 + "\n" + "C)  " + this.op2 + "\n" + "D)  " + this.op1 + "\n";
        }

        toReturn += "\n" + "Enter A, B, C, or D in the textfield.";

        return toReturn;
    }

    // Getters

    /**
     * Getter method for answer attribute of the question
     *
     * @return the answer attribute
     */
    public String getAnswer() {
        return this.answer;
    }

    /**
     * Getter method for type attribute of the question
     *
     * @return the type attribute
     */
    public String getType() {
        return this.type;
    }

    /**
     * Getter method for room number attribute of the question
     *
     * @return the room number attribute
     */
    public int getRoomNumber() {
        return this.roomNumber;
    }
}

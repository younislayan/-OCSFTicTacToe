package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class TicTacToeMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        JOIN,
        WAITING,
        GAME_START,
        MOVE,
        GAME_UPDATE,
        GAME_OVER
    }

    private Type type;
    private String symbol;
    private String[] board;
    private String currentTurn;
    private int moveIndex;
    private String result;

    public TicTacToeMessage(Type type) {
        this.type = type;
    }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String[] getBoard() { return board; }
    public void setBoard(String[] board) { this.board = board; }

    public String getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(String currentTurn) { this.currentTurn = currentTurn; }

    public int getMoveIndex() { return moveIndex; }
    public void setMoveIndex(int moveIndex) { this.moveIndex = moveIndex; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}

package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.TicTacToeMessage;

public class TicTacToeEvent {
    private final TicTacToeMessage message;

    public TicTacToeEvent(TicTacToeMessage message) {
        this.message = message;
    }

    public TicTacToeMessage getMessage() {
        return message;
    }
}

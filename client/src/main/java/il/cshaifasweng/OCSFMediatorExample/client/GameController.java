package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

import il.cshaifasweng.OCSFMediatorExample.entities.TicTacToeMessage;

public class GameController {

    @FXML private Label statusLabel;
    @FXML private Label symbolLabel;
    @FXML private Button btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8;

    private Button[] buttons;
    private String mySymbol;
    private boolean myTurn;
    private boolean gameOver;

    @FXML
    void initialize() {
        buttons = new Button[]{btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8};
        EventBus.getDefault().register(this);
    }

    public void initGame(TicTacToeMessage msg) {
        mySymbol = msg.getSymbol();
        myTurn = mySymbol.equals(msg.getCurrentTurn());
        gameOver = false;
        symbolLabel.setText("You are: " + mySymbol);
        updateBoard(msg.getBoard());
        updateStatus();
    }

    @FXML
    void onCellClick(ActionEvent event) {
        if (!myTurn || gameOver) return;
        Button clicked = (Button) event.getSource();
        int index = -1;
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] == clicked) { index = i; break; }
        }
        if (index == -1 || !buttons[index].getText().isEmpty()) return;

        myTurn = false;
        updateButtonStates();

        TicTacToeMessage moveMsg = new TicTacToeMessage(TicTacToeMessage.Type.MOVE);
        moveMsg.setMoveIndex(index);
        try {
            SimpleClient.getClient().sendToServer(moveMsg);
        } catch (IOException e) {
            e.printStackTrace();
            myTurn = true;
            updateButtonStates();
        }
    }

    @Subscribe
    public void onTicTacToeEvent(TicTacToeEvent event) {
        TicTacToeMessage msg = event.getMessage();
        Platform.runLater(() -> {
            switch (msg.getType()) {
                case GAME_UPDATE:
                    myTurn = mySymbol.equals(msg.getCurrentTurn());
                    updateBoard(msg.getBoard());
                    updateStatus();
                    break;
                case GAME_OVER:
                    gameOver = true;
                    myTurn = false;
                    if (msg.getBoard() != null) updateBoard(msg.getBoard());
                    disableAllButtons();
                    switch (msg.getResult()) {
                        case "WIN":  statusLabel.setText("You won!"); break;
                        case "LOSE": statusLabel.setText("You lost!"); break;
                        default:     statusLabel.setText("Draw!"); break;
                    }
                    EventBus.getDefault().unregister(this);
                    break;
                default:
                    break;
            }
        });
    }

    private void updateBoard(String[] board) {
        for (int i = 0; i < 9; i++) {
            buttons[i].setText(board[i]);
        }
        updateButtonStates();
    }

    private void updateStatus() {
        statusLabel.setText(myTurn ? "Your turn" : "Opponent's turn");
    }

    private void updateButtonStates() {
        for (Button btn : buttons) {
            btn.setDisable(!myTurn || !btn.getText().isEmpty());
        }
    }

    private void disableAllButtons() {
        for (Button btn : buttons) btn.setDisable(true);
    }
}

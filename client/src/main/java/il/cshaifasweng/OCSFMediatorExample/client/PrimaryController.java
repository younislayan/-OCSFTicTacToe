package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

import il.cshaifasweng.OCSFMediatorExample.entities.TicTacToeMessage;

public class PrimaryController {

    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private Label statusLabel;
    @FXML private Button connectButton;

    @FXML
    void connectToServer(ActionEvent event) {
        String host = hostField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            setStatus("Invalid port number.");
            return;
        }
        connectButton.setDisable(true);
        setStatus("Connecting...");
        try {
            SimpleClient client = SimpleClient.getClient(host, port);
            if (!client.isConnected()) {
                client.openConnection();
            }
            TicTacToeMessage joinMsg = new TicTacToeMessage(TicTacToeMessage.Type.JOIN);
            client.sendToServer(joinMsg);
        } catch (IOException e) {
            setStatus("Connection failed: " + e.getMessage());
            connectButton.setDisable(false);
            SimpleClient.resetClient();
        }
    }

    public void setStatus(String status) {
        statusLabel.setText(status);
    }
}

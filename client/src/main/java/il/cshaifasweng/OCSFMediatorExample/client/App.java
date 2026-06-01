package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

import il.cshaifasweng.OCSFMediatorExample.entities.TicTacToeMessage;

public class App extends Application {

    private static Scene scene;
    private PrimaryController primaryController;

    @Override
    public void start(Stage stage) throws IOException {
        EventBus.getDefault().register(this);
        FXMLLoader loader = new FXMLLoader(App.class.getResource("primary.fxml"));
        Parent root = loader.load();
        primaryController = loader.getController();
        scene = new Scene(root, 640, 520);
        stage.setTitle("Tic-Tac-Toe");
        stage.setScene(scene);
        stage.show();
    }

    @Subscribe
    public void onTicTacToeEvent(TicTacToeEvent event) {
        TicTacToeMessage msg = event.getMessage();
        switch (msg.getType()) {
            case WAITING:
                Platform.runLater(() -> {
                    if (primaryController != null) {
                        primaryController.setStatus("Waiting for opponent to join...");
                    }
                });
                break;
            case GAME_START:
                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(App.class.getResource("game.fxml"));
                        Parent root = loader.load();
                        GameController gc = loader.getController();
                        gc.initGame(msg);
                        primaryController = null;
                        scene.setRoot(root);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
            default:
                break;
        }
    }

    @Override
    public void stop() throws Exception {
        EventBus.getDefault().unregister(this);
        SimpleClient c = SimpleClient.getInstance();
        if (c != null && c.isConnected()) {
            c.closeConnection();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}

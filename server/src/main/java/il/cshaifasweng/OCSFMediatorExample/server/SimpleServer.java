package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.TicTacToeMessage;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class SimpleServer extends AbstractServer {

    private ConnectionToClient[] players = new ConnectionToClient[2];
    private String[] symbols = new String[2];
    private String[] board = new String[9];
    private int currentPlayerIndex = -1;
    private final Random random = new Random();

    public SimpleServer(int port) {
        super(port);
        resetBoard();
    }

    private void resetBoard() {
        Arrays.fill(board, "");
    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        if (!(msg instanceof TicTacToeMessage)) return;
        TicTacToeMessage message = (TicTacToeMessage) msg;
        switch (message.getType()) {
            case JOIN:
                handleJoin(client);
                break;
            case MOVE:
                handleMove(message.getMoveIndex(), client);
                break;
            default:
                break;
        }
    }

    private synchronized void handleJoin(ConnectionToClient client) {
        int playerIndex = -1;
        if (players[0] == null) {
            playerIndex = 0;
        } else if (players[1] == null) {
            playerIndex = 1;
        } else {
            return; // game full
        }
        players[playerIndex] = client;
        System.out.println("Player " + (playerIndex + 1) + " joined: " + client.getInetAddress().getHostAddress());

        if (playerIndex == 0) {
            TicTacToeMessage waiting = new TicTacToeMessage(TicTacToeMessage.Type.WAITING);
            send(client, waiting);
        } else {
            // Randomly assign symbols
            if (random.nextBoolean()) {
                symbols[0] = "X"; symbols[1] = "O";
            } else {
                symbols[0] = "O"; symbols[1] = "X";
            }
            // Randomly decide who goes first
            currentPlayerIndex = random.nextInt(2);
            resetBoard();

            System.out.println("Game starting! Player 1=" + symbols[0] + ", Player 2=" + symbols[1]);
            System.out.println("First turn: Player " + (currentPlayerIndex + 1) + " (" + symbols[currentPlayerIndex] + ")");

            for (int i = 0; i < 2; i++) {
                TicTacToeMessage start = new TicTacToeMessage(TicTacToeMessage.Type.GAME_START);
                start.setSymbol(symbols[i]);
                start.setBoard(board.clone());
                start.setCurrentTurn(symbols[currentPlayerIndex]);
                send(players[i], start);
            }
        }
    }

    private synchronized void handleMove(int index, ConnectionToClient client) {
        int senderIndex = findPlayerIndex(client);
        if (senderIndex == -1 || senderIndex != currentPlayerIndex) return;
        if (index < 0 || index > 8 || !board[index].isEmpty()) return;

        board[index] = symbols[senderIndex];
        System.out.println("Player " + (senderIndex + 1) + " (" + symbols[senderIndex] + ") played at " + index);

        String winner = checkWinner();
        boolean draw = (winner == null) && checkDraw();

        if (winner != null || draw) {
            for (int i = 0; i < 2; i++) {
                TicTacToeMessage gameOver = new TicTacToeMessage(TicTacToeMessage.Type.GAME_OVER);
                gameOver.setBoard(board.clone());
                if (winner != null) {
                    gameOver.setResult(symbols[i].equals(winner) ? "WIN" : "LOSE");
                } else {
                    gameOver.setResult("DRAW");
                }
                send(players[i], gameOver);
            }
            System.out.println("Game over! " + (winner != null ? winner + " wins" : "Draw"));
            resetGame();
        } else {
            currentPlayerIndex = 1 - currentPlayerIndex;
            for (int i = 0; i < 2; i++) {
                TicTacToeMessage update = new TicTacToeMessage(TicTacToeMessage.Type.GAME_UPDATE);
                update.setBoard(board.clone());
                update.setCurrentTurn(symbols[currentPlayerIndex]);
                send(players[i], update);
            }
        }
    }

    private int findPlayerIndex(ConnectionToClient client) {
        for (int i = 0; i < 2; i++) {
            if (players[i] != null && players[i].equals(client)) return i;
        }
        return -1;
    }

    private String checkWinner() {
        int[][] lines = {
            {0,1,2}, {3,4,5}, {6,7,8},
            {0,3,6}, {1,4,7}, {2,5,8},
            {0,4,8}, {2,4,6}
        };
        for (int[] line : lines) {
            String a = board[line[0]], b = board[line[1]], c = board[line[2]];
            if (!a.isEmpty() && a.equals(b) && b.equals(c)) return a;
        }
        return null;
    }

    private boolean checkDraw() {
        for (String cell : board) if (cell.isEmpty()) return false;
        return true;
    }

    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        int idx = findPlayerIndex(client);
        if (idx == -1) return;
        int other = 1 - idx;
        ConnectionToClient otherClient = players[other];
        resetGame();
        if (otherClient != null) {
            TicTacToeMessage msg = new TicTacToeMessage(TicTacToeMessage.Type.GAME_OVER);
            msg.setResult("WIN");
            send(otherClient, msg);
        }
        System.out.println("Player " + (idx + 1) + " disconnected. Game reset.");
    }

    private void resetGame() {
        players[0] = null;
        players[1] = null;
        symbols[0] = null;
        symbols[1] = null;
        currentPlayerIndex = -1;
        resetBoard();
    }

    private void send(ConnectionToClient client, Object msg) {
        try {
            client.sendToClient(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package projectTP.weichi.server;

import projectTP.weichi.server.exceptions.*;
import projectTP.weichi.server.game.Game;
import projectTP.weichi.server.support.GameConfig;
import projectTP.weichi.server.support.Point;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    private Game game;
    private ServerSocket server;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String line;

    public Server() {
        try {
            makeServer();
        }
        catch (DidntCreateServerException ex) {
            System.out.println("Could not create server on port " + ex.getPort() + ".");
        }

    }

    @Override
    public void run() {
        try {
            connect();
        } catch (DidntConnectException ex) {
                System.out.println("Server didn't connect.");
                interrupt();
        } catch (DidntConfigureCorrectlyException ex) {
            System.out.println("Buffers' configuration problems occurred (Server).");
        }
        game = createGame();
        play();
        // rematch?
    }

    private void play() {
        do {
            readInput();
            ServerParser parser = new ServerParserJson(line);
            Point x = parser.parsePoint();
            game.move(x);
        } while(!game.won());
    }

    private void readInput() {
        try { line = input.readLine(); }
        catch (IOException e) {
            System.out.println("Reading problem occurred.");
        }
    }

    private void makeServer() throws DidntCreateServerException {
        int port = 4999;
        try {
            server = new ServerSocket(port);
            System.out.println("server open on port: " + port);
        } catch (IOException e) {
            throw new DidntCreateServerException(port);
        }
    }

    private void connect() throws DidntConnectException, DidntConfigureCorrectlyException {
        try {
            socket = server.accept();
            System.out.println("client connected");
        }
        catch (Exception ex) { throw new DidntConnectException(); }

        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException e) {
            throw new DidntConfigureCorrectlyException();
        }
    }

    private Game createGame() {
        readInput();
        ServerParser parser = new ServerParserJson(line);
        GameConfig config = parser.parseGameConfig();
        return new Game(config.getBot(), config.getSize());
    }
}
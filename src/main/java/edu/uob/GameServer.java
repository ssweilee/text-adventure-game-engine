package edu.uob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

public final class GameServer {

    private static final char END_OF_TRANSMISSION = 4;
    private final GameState gameState;
    private  CommandProcessor commandProcessor;

    public static void main(String[] args) throws IOException {
        File entitiesFile = Paths.get("config","extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config","extended-actions.xml").toAbsolutePath().toFile();
        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Instanciates a new server instance, specifying a game with some configuration files
    *
    * @param entitiesFile The game configuration file containing all game entities to use in your game
    * @param actionsFile The game configuration file containing all game actions to use in your game
    */
    public GameServer(File entitiesFile, File actionsFile) {
        // TODO implement your server logic here
        EntityParser entityParser = new EntityParser(null);
        this.gameState = entityParser.parseStartLocation(entitiesFile);
        GameInitialiser initialiser = new GameInitialiser(this.gameState);
        initialiser.initialise(entitiesFile, actionsFile);
        this.commandProcessor = new CommandProcessor(this.gameState);
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * This method handles all incoming game commands and carries out the corresponding actions.</p>
    *
    * @param command The incoming command to be processed
    */

    public String handleCommand(String command) {
        return this.commandProcessor.processCommand(command);
    }


    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Starts a *blocking* socket server listening for new connections.
    *
    * @param portNumber The port to listen on.
    * @throws IOException If any IO related operation fails.
    */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.printf("Server listening on port %d", portNumber);
            while (!Thread.interrupted()) {
                try {
                    this.blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Handles an incoming connection from the socket server.
    *
    * @param serverSocket The client socket to read/write from.
    * @throws IOException If any IO related operation fails.
    */

    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
             BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                StringBuilder receivedMsg = new StringBuilder();
                receivedMsg.append("Received message from ");
                receivedMsg.append(incomingCommand);
                System.out.println(receivedMsg.toString());
                String result = this.handleCommand(incomingCommand);
                StringBuilder output = new StringBuilder();
                output.append(result);
                output.append("\n");
                output.append(END_OF_TRANSMISSION);
                output.append("\n");
                writer.write(output.toString());
                writer.flush();
            }
        }
    }
}

/**
 *
 *  @author Król Bartosz s31572
 *
 */

package zad1;

import zad1.exception.checked.ClientNotLoggedInException;
import zad1.exception.unchecked.ClientException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class ChatClient {
    private static final String LOGIN_REQUEST = "hi";
    private static final String LOGOUT_REQUEST = "bye";
    private static final String SEND_REQUEST = "send";

    private final String id;
    private final Socket socket;
    private final InetSocketAddress address;
    private PrintWriter out;
    private final Thread listeningThread;
    private final Log log;

    public ChatClient(String host, int serverPort, String id) {
        this.id = id;
        this.log = new Log();
        this.socket = new Socket();
        this.address = new InetSocketAddress(host, serverPort);
        this.listeningThread = Thread.ofVirtual()
                .unstarted(() -> {
                    try (var in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                        String line;
                        while ((line = in.readLine()) != null) {
                            log.log(line);
                        }
                    } catch (IOException ignored) {} finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            System.err.printf("Error while closing the socket. Cause: %s", e.getMessage());
                        }
                    }
                });
    }

    public void login() throws ClientNotLoggedInException {
        connectToServer(socket, address);
        this.out = createPrintWriter(socket);
        listeningThread.start();
        send("%s:%s".formatted(LOGIN_REQUEST, id));
    }

    public void logout() throws ClientNotLoggedInException {
        send("%s:%s".formatted(LOGOUT_REQUEST, id));
    }

    public void sendMessage(String message) throws ClientNotLoggedInException {
        send("%s:%s".formatted(SEND_REQUEST, message));
    }

    public void send(String req) throws ClientNotLoggedInException {
        if (out == null)
            throw new ClientNotLoggedInException("Client need to be connected to server in order to send messages");
        out.println(req);
    }

    private static void connectToServer(Socket socket, InetSocketAddress address) {
        try {
            socket.setSoTimeout(30000);
            socket.setReuseAddress(true);
        } catch (SocketException e) {
            System.out.printf("Exception while configuring the socket, cause %s. Continuing with default socket setting\n", e.getMessage());
        }
        try {
            socket.connect(address, 5000);
        } catch (IOException e) {
            throw new ClientException("Exception while connection to server", e);
        }
    }

    private static PrintWriter createPrintWriter(Socket socket) {
        try {
            return new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (IOException e) {
            throw new ClientException("Exception while creating PrintWriter for client", e);
        }
    }

    public String getChatView() {
        return log.getLog();
    }

    public String getId() {return id;}
}

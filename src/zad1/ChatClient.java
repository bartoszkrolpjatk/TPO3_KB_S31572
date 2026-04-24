/**
 *
 *  @author Król Bartosz s31572
 *
 */

package zad1;


public class ChatClient {
    private final String id;

    public ChatClient(String host, int serverPort, String id) {
        this.id = id;
    }

    public void login() {

    }

    public void logout() {

    }

    public void send(String req) {

    }

    public String getChatView() {
        return "mock client log";
    }

    public String getId() {return id;}
}

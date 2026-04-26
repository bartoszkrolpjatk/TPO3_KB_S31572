/**
 *
 *  @author Król Bartosz s31572
 *
 */

package zad1;

import java.util.List;
import java.util.concurrent.FutureTask;

public class ChatClientTask extends FutureTask<Void> {
    private final ChatClient client;

    private ChatClientTask(ChatClient client, List<String> messages, int waitBetweenRequests) {
        super(() -> {
            client.login();
            for(var m : messages) {
                client.sendMessage(m);
                Thread.sleep(waitBetweenRequests);
            }
            client.logout();
            return null;
        });
        this.client = client;
    }

    public static ChatClientTask create(ChatClient chatClient, List<String> messages, int waitBetweenRequests) {
        return new ChatClientTask(chatClient, messages, waitBetweenRequests);
    }

    public ChatClient getClient() {
        return client;
    }
}

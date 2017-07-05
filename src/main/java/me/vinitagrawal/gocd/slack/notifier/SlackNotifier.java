package me.vinitagrawal.gocd.slack.notifier;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;

import java.io.IOException;

public class SlackNotifier {

  private Slack slack = Slack.getInstance();

  private String token;
  private String channelName;
  private String userName;

  public SlackNotifier(String token, String channelName, String userName) {
    this.token = token;
    this.channelName = channelName;
    this.userName = userName;
  }

  public void postMessage(String message) {
    try {
      slack.methods().chatPostMessage(
        ChatPostMessageRequest.builder()
          .token(token)
          .channel(channelName)
          .text(message)
          .username(userName)
          .build()
      );
    } catch (IOException | SlackApiException e) {
      e.printStackTrace();
    }
  }
}

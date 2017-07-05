package me.vinitagrawal.gocd.slack.notifier;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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

  public void postMessage(Message message) {
    try {
      Attachment attachment = Attachment.builder()
        .text(message.getAttachmentTitle())
        .fields(new ArrayList<Field>())
        .build();

      for(Message.Field messageField : message.getFields()) {
        Field field = Field.builder()
          .title(messageField.getTitle())
          .value(messageField.getValue())
          .build();
        attachment.getFields().add(field);
      }

      slack.methods().chatPostMessage(
        ChatPostMessageRequest.builder()
          .token(token)
          .channel(channelName)
          .text(message.getTitle())
          .username(userName)
          .attachments(Arrays.asList(attachment))
          .build()
      );

    } catch (IOException | SlackApiException e) {
      e.printStackTrace();
    }
  }
}

package me.vinitagrawal.gocd.slack.notifier;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.users.UsersListRequest;
import com.github.seratch.jslack.api.methods.response.users.UsersListResponse;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;
import com.github.seratch.jslack.api.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    if(message.shouldPostMinimalisticMessage()) {
      postMinimalisticMessage(message);
    }
    else {
      postDetailedMessage(message);
    }

  }

  private void postMinimalisticMessage(Message message) {
    Attachment attachment = Attachment.builder()
      .text("Failing: " + message.getPipelineURL() +
        "\nOwners: " + getOwners(message.getOwnerList()) +
        "\n" + message.getChanges())
      .color("#F44336")
      .build();

    post(null, Arrays.asList(attachment));
  }

  private void postDetailedMessage(Message message) {
    Attachment attachment = Attachment.builder()
      .title(message.getPipelineURL())
      .fields(new ArrayList<Field>())
      .color("#F44336")
      .build();

    for (Message.Field messageField : message.getFields()) {
      Field field = Field.builder()
        .title(messageField.getTitle())
        .value(messageField.getValue())
        .valueShortEnough(messageField.isValueShort())
        .build();
      attachment.getFields().add(field);
    }

    attachment.getFields().add(getOwnersField(message));

    post(message.getText(), Arrays.asList(attachment, getChangesAttachment(message)));
  }

  private Attachment getChangesAttachment(Message message) {
    return Attachment.builder()
          .title("Changes")
          .text(message.getChanges())
          .color("#F44336")
          .build();
  }

  private Field getOwnersField(Message message) {
    return Field.builder()
      .title("Owners")
      .value(getOwners(message.getOwnerList()))
      .valueShortEnough(false)
      .build();
  }

  private String getOwners(List<String> ownerList) {
    String owners = "";
    for (String owner : ownerList) {
      String slackUser = getSlackUser(getEmail(owner));
      if(slackUser != null)
        owners = owners.concat("<@" + slackUser + "> ");
      else
        owners = owners.concat(owner + " ");
    }

    return owners.trim();
  }

  private String getEmail(String owner) {
    return owner.substring(owner.indexOf("<") + 1, owner.indexOf(">"));
  }

  private String getSlackUser(String userEmail) {
    try {
      UsersListResponse usersListResponse = slack.methods().usersList(UsersListRequest.builder().token(token).build());
      for(User user : usersListResponse.getMembers())
        if(user.getProfile().getEmail().equalsIgnoreCase(userEmail)) {
          return user.getName();
        }
    } catch (IOException | SlackApiException | NullPointerException e) {
      e.printStackTrace();
    }

    return null;
  }

  private void post(String text, List<Attachment> attachments) {
    try {
      slack.methods().chatPostMessage(
        ChatPostMessageRequest.builder()
          .token(token)
          .channel(channelName)
          .text(text)
          .username(userName)
          .attachments(attachments)
          .build()
      );
    } catch (IOException | SlackApiException e) {
      e.printStackTrace();
    }
  }

}

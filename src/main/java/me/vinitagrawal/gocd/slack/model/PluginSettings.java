package me.vinitagrawal.gocd.slack.model;

import com.google.gson.annotations.SerializedName;

public class PluginSettings {

  @SerializedName("server_base_url")
  private String serverBaseUrl;
  @SerializedName("server_api_username")
  private String serverApiUsername;
  @SerializedName("server_api_password")
  private String serverApiPassword;
  @SerializedName("slack_oauth_token")
  private String slackOAuthToken;
  @SerializedName("slack_channel")
  private String slackChannelName;
  @SerializedName("slack_bot_name")
  private String slackBotName;

  public String getServerBaseUrl() {
    return serverBaseUrl;
  }

  public String getServerApiUsername() {
    return serverApiUsername;
  }

  public String getServerApiPassword() {
    return serverApiPassword;
  }

  public String getSlackOAuthToken() {
    return slackOAuthToken;
  }

  public String getSlackChannelName() {
    return slackChannelName;
  }

  public String getSlackBotName() {
    return slackBotName;
  }
}

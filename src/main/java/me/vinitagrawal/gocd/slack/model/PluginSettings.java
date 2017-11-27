package me.vinitagrawal.gocd.slack.model;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
  @SerializedName("pipeline_names")
  private String pipelineNames;
  @SerializedName("is_minimalistic")
  private String isMinimalisticCheckEnabled;

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

  public List<String> getPipelineNames() {
    if (pipelineNames != null)
      return Arrays.asList(pipelineNames.split("\\s*,\\s*"));

    return Collections.emptyList();
  }

  public boolean isMinimalisticCheckEnabled() {
    if (isMinimalisticCheckEnabled != null)
      return isMinimalisticCheckEnabled.equalsIgnoreCase("on");

    return false;
  }

}

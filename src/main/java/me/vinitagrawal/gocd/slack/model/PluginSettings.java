package me.vinitagrawal.gocd.slack.model;

import com.google.gson.annotations.SerializedName;

public class PluginSettings {

  @SerializedName("server_base_url")
  private String serverBaseUrl;
  @SerializedName("server_api_username")
  private String serverApiUsername;
  @SerializedName("server_api_password")
  private String serverApiPassword;

  public String getServerBaseUrl() {
    return serverBaseUrl;
  }

  public String getServerApiUsername() {
    return serverApiUsername;
  }

  public String getServerApiPassword() {
    return serverApiPassword;
  }
}

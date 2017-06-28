package me.vinitagrawal.gocd.slack.model;

import com.google.gson.annotations.SerializedName;

public class Modification {

  @SerializedName("modified-time")
  private String modifiedTime;

  @SerializedName("revision")
  private String revision;

  @SerializedName("email_address")
  private String emailAddress;

  @SerializedName("user_name")
  private String userName;

  @SerializedName("comment")
  private String comment;

  public String getModifiedTime() {
    return modifiedTime;
  }

  @Override
  public String toString() {
    return "Modification{" +
      "modifiedTime='" + modifiedTime + '\'' +
      ", revision='" + revision + '\'' +
      '}';
  }
}
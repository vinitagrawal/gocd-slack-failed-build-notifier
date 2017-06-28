package me.vinitagrawal.gocd.slack.model;

import com.google.gson.annotations.SerializedName;

public class Modification {

  @SerializedName("modified-time")
  private String modifiedTime;

  @SerializedName("revision")
  private String revision;

  @SerializedName("email_address")
  public String emailAddress;

  @SerializedName("user_name")
  public String userName;

  @SerializedName("comment")
  public String comment;
}
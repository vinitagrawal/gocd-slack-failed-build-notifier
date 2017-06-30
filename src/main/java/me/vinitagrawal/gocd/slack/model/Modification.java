package me.vinitagrawal.gocd.slack.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Modification {

  @SerializedName("modified_time")
  private long modifiedTime;

  @SerializedName("revision")
  private String revision;

  @SerializedName("user_name")
  private String userName;

  @SerializedName("comment")
  private String comment;

  public Date getModifiedTime() {
    return new Date(modifiedTime);
  }

  public String getRevision() {
    return revision;
  }

  public String getMessage() {
    return "\n" + revision + "\n" + comment + " - " + userName;
  }
}
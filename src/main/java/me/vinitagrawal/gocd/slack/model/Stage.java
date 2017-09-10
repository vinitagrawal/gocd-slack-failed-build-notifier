package me.vinitagrawal.gocd.slack.model;

import com.google.gson.annotations.SerializedName;

public class Stage {

  @SerializedName("name")
  private String name;

  @SerializedName("state")
  private String state;

  @SerializedName("result")
  private String result;

  @SerializedName("counter")
  private int counter;

  public String getName() {
    return name;
  }

  public String getState() {
    return state;
  }

  public String getResult() {
    return result;
  }

  public String getPath() {
    return name + "/" + counter;
  }
}


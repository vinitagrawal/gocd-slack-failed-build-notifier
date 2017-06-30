package me.vinitagrawal.gocd.slack.model;

import com.google.gson.annotations.SerializedName;

public class Stage {

  @SerializedName("name")
  private String name;

  @SerializedName("result")
  private String result;

  @SerializedName("counter")
  private int counter;

  public String getResult() {
    return result;
  }

  public String getPath() {
    return name + "/" + counter;
  }
}


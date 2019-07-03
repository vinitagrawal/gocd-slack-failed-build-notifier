package me.vinitagrawal.gocd.slack.traceCommit.model;

import com.google.gson.annotations.SerializedName;

public class Stage {

  private static final String STAGE_FAILED = "Failed";
  private static final String STAGE_CANCELLED = "Cancelled";
  private static final String STAGE_BUILDING = "Building";

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

  public String getResult() {
    return result;
  }

  public String getPath() {
    return name + "/" + counter;
  }

  public boolean isFailed() {
    return getResult().equalsIgnoreCase(STAGE_FAILED);
  }

  public boolean isCancelled() {
    return getResult().equalsIgnoreCase(STAGE_CANCELLED);
  }

  public boolean isCompleted() {
   return !state.equalsIgnoreCase(STAGE_BUILDING) && !state.equalsIgnoreCase(STAGE_CANCELLED);
  }

  public boolean isRerun() {
    return counter > 1;
  }
}


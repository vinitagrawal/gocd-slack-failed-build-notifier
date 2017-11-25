package me.vinitagrawal.gocd.slack.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GoApiRequestBody {

  @SerializedName("pipeline")
  private Pipeline pipeline;

  public Pipeline getPipeline() {
    return pipeline;
  }

  public static class Pipeline {

    @SerializedName("name")
    private String name;
    @SerializedName("counter")
    private int counter;
    @SerializedName("label")
    private String label;
    @SerializedName("group")
    private String group;

    @SerializedName("build-cause")
    private List<MaterialRevision> revisions;

    @SerializedName("stage")
    private Stage stage;

    public String getName() {
      return name;
    }

    public int getCounter() {
      return counter;
    }

    public String getPath() {
      return name + "/" + counter + "/" + stage.getPath();
    }

    public boolean hasStageFailed() {
      return stage.isFailed();
    }

    public boolean isStageCompleted() {
      return stage.isCompleted();
    }

    public Stage getStage() {
      return stage;
    }

    public String getPassedStage() {
      return String.format("%s : %s", getName(), getStage().getName());
    }

    public boolean isRerun() {
      return stage.isRerun();
    }
  }

}

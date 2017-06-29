package me.vinitagrawal.gocd.slack.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PipelineInstance {

  @SerializedName("build_cause")
  private BuildCause buildCause;

  @SerializedName("stages")
  private List<Stage> stages;

  @SerializedName("label")
  private String label;

  public static class BuildCause {

    @SerializedName("material_revisions")
    private List<MaterialRevision> revisions;

    public List<MaterialRevision> getRevisions() {
      return revisions;
    }
  }

  public BuildCause getBuildCause() {
    return buildCause;
  }
}
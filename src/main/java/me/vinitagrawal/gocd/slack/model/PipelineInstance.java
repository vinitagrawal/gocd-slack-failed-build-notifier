package me.vinitagrawal.gocd.slack.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
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

  public MaterialRevision getBuildCauseRevision() {
    MaterialRevision buildRevision = getChangedMaterialRevision();
    if(buildRevision == null) {
      List<MaterialRevision> revisions = buildCause.getRevisions();
      buildRevision = revisions.get(0);
      for(int i=1; i<revisions.size(); i++) {
        if(!isLatestBuildCause(buildRevision, revisions.get(i)))
          buildRevision = revisions.get(i);
      }
    }

    return buildRevision;
  }

  private boolean isLatestBuildCause(MaterialRevision latestRevision, MaterialRevision revision) {
    Date latestModifiedTime = latestRevision.getLatestModifiedTime();
    Date modifiedTime = revision.getLatestModifiedTime();

    return latestModifiedTime.after(modifiedTime);
  }

  private MaterialRevision getChangedMaterialRevision() {
    for(MaterialRevision materialRevision : buildCause.getRevisions()) {
      if(materialRevision.isChanged())
        return materialRevision;
    }

    return null;
  }

}
package me.vinitagrawal.gocd.slack.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
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

  public boolean hasStageFailed(Stage currentStage) {
    for (Stage stage : stages) {
      if (stage.getName().equalsIgnoreCase(currentStage.getName()) &&
        stage.isFailed())
        return true;
    }
    return false;
  }

  public List<MaterialRevision> getBuildCauseRevision() {
    List<MaterialRevision> buildRevisionList = getChangedMaterialRevision();

    if(buildRevisionList.isEmpty()) {
      buildRevisionList = new ArrayList<>();
      buildRevisionList.add(getModifiedMaterialRevision());
    }

    return buildRevisionList;
  }

  private MaterialRevision getModifiedMaterialRevision() {
    MaterialRevision buildRevision;
    List<MaterialRevision> revisions = buildCause.getRevisions();
    buildRevision = revisions.get(0);
    for(int i=1; i<revisions.size(); i++) {
      if(!isLatestBuildCause(buildRevision, revisions.get(i)))
        buildRevision = revisions.get(i);
    }

    return buildRevision;
  }

  private boolean isLatestBuildCause(MaterialRevision latestRevision, MaterialRevision revision) {
    Date latestModifiedTime = latestRevision.getLatestModifiedTime();
    Date modifiedTime = revision.getLatestModifiedTime();

    return latestModifiedTime.after(modifiedTime);
  }

  private List<MaterialRevision> getChangedMaterialRevision() {
    List<MaterialRevision> materialRevisionList = new ArrayList<>();
    for(MaterialRevision materialRevision : buildCause.getRevisions()) {
      if(materialRevision.isChanged())
        materialRevisionList.add(materialRevision);
    }

    if(materialRevisionList.isEmpty())
      return Collections.emptyList();

    return materialRevisionList;
  }

  public boolean hasStateChanged(Stage currentStage) {
    for (Stage stage : stages) {
      try {
        if (stage.getName().equals(currentStage.getName()))
          if (stage.isCancelled() && currentStage.isFailed())
              return true;
          else if (!stage.getResult().equalsIgnoreCase(currentStage.getResult()))
              return true;
      } catch (NullPointerException e) {
        if (currentStage.isFailed())
          return true;
      }
    }

    return false;
  }
}
package me.vinitagrawal.gocd.slack.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class MaterialRevision {

  @SerializedName("modifications")
  private List<Modification> modifications;

  @SerializedName("material")
  private Map material;

  @SerializedName("changed")
  private boolean changed;

  public List<Modification> getModifications() {
    return modifications;
  }

  public Map getMaterial() {
    return material;
  }

  public boolean isChanged() {
    return changed;
  }

  public String getMaterialDescription() {
    return "\n" + getMaterial().get("description").toString();
  }

  public boolean isBuildCauseTypePipeline() {
    String materialType = getMaterial().get("type").toString();
    return materialType.equalsIgnoreCase("pipeline");
  }

  public boolean isBuildCauseTypeGit() {
    String materialType = getMaterial().get("type").toString();
    return materialType.equalsIgnoreCase("git");
  }

  public Modification getBuildCauseModification() {
    Modification modification = getModifications().get(0);
    for (int i=1; i<getModifications().size(); i++) {
      modification = getRecentModification(modification, getModifications().get(i));
    }
    return modification;
  }

  public Date getLatestModifiedTime() {
    Date recentTime = getModifications().get(0).getModifiedTime();
    for(int i=1; i<modifications.size(); i++) {
      recentTime = getRecentTime(recentTime, modifications.get(i).getModifiedTime());
    }

    return recentTime;
  }

  private Modification getRecentModification(Modification recentModification, Modification modification) {
    Date recentTime = getRecentTime(recentModification.getModifiedTime(), modification.getModifiedTime());
    if (modification.getModifiedTime().equals(recentTime))
      return modification;

    return recentModification;
  }

  private Date getRecentTime(Date recentTime, Date modifiedTime) {
    if (modifiedTime != null && recentTime != null && modifiedTime.after(recentTime))
      return modifiedTime;

    return recentTime;
  }

}
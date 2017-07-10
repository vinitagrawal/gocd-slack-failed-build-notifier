package me.vinitagrawal.gocd.slack.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static me.vinitagrawal.gocd.slack.utils.TextUtils.isNullOrEmpty;

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

  public String getMaterialFingerprint() {
    return getMaterial().get("fingerprint").toString();
  }

  public boolean isBuildCauseTypePipeline() {
    String materialType = getMaterial().get("type").toString();
    return materialType.equalsIgnoreCase("pipeline");
  }

  public boolean isBuildCauseTypeGit() {
    String materialType = getMaterial().get("type").toString();
    return materialType.equalsIgnoreCase("git");
  }

  public String getPipelineRevision() {
    Modification modification = getBuildCauseModification();
    return modification.getRevision();
  }

  private Modification getBuildCauseModification() {
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

  public List<String> getCommitOwners(List<String> ownerList) {
    for(Modification modification : getModifications()) {
      if(!isNullOrEmpty(modification.getUserName()) &&
        !ownerList.contains(modification.getUserName()))
        ownerList.add(modification.getUserName());
    }

    return ownerList;
  }

  public List<String> getMaterialChanges() {
    List<String> changes = new ArrayList<>();
    for(Modification modification : getModifications()) {
      changes.add(modification.getMessage());
    }

    return changes;
  }

}
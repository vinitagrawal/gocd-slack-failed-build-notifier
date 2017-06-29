package me.vinitagrawal.gocd.slack.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static me.vinitagrawal.gocd.slack.utils.DateUtils.convertStringToDate;

public class MaterialRevision {

  @SerializedName("modifications")
  private List<Modification> modifications;

  @SerializedName("material")
  private Map material;

  @SerializedName("changed")
  private boolean changed;

  private List<Modification> getModifications() {
    return modifications;
  }

  public Map getMaterial() {
    return material;
  }

  public boolean isChanged() {
    return changed;
  }

  public boolean isBuildCauseTypePipeline() {
    String materialType = getMaterial().get("type").toString();
    return materialType.equalsIgnoreCase("pipeline");
  }

  public Modification getBuildCauseModification() {
    Modification modification = getModifications().get(0);
    for (int i=1; i<getModifications().size(); i++) {
      modification = getRecentModification(modification, getModifications().get(i));
    }
    return modification;
  }

  public Date getLatestModifiedTime() {
    String recentTime = getModifications().get(0).getModifiedTime();
    for(int i=1; i<modifications.size(); i++) {
      recentTime = getRecentTime(recentTime, modifications.get(i).getModifiedTime());
    }

    return convertStringToDate(recentTime);
  }

  private Modification getRecentModification(Modification recentModification, Modification modification) {
    String recentTime = getRecentTime(recentModification.getModifiedTime(), modification.getModifiedTime());
    if (modification.getModifiedTime().equals(recentTime))
      return modification;

    return recentModification;
  }

  private String getRecentTime(String recentTime, String modifiedTime) {
    Date latestDate = convertStringToDate(recentTime);
    Date date = convertStringToDate(modifiedTime);

    if (date != null && latestDate != null && date.after(latestDate))
      return modifiedTime;

    return recentTime;
  }

}
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

  public List<Modification> getModifications() {
    return modifications;
  }

  public Date getLatestModifiedTime() {
    Date recentDateTime = convertStringToDate(getModifications().get(0).getModifiedTime());
    for(int i=1; i<modifications.size(); i++) {
      Date modifiedDateTime = convertStringToDate(modifications.get(i).getModifiedTime());
      recentDateTime = getRecentTime(recentDateTime, modifiedDateTime);
    }

    return recentDateTime;
  }

  private Date getRecentTime(Date recentDateTime, Date modifiedDateTime) {

    if (modifiedDateTime != null && recentDateTime != null &&
      modifiedDateTime.after(recentDateTime)) {
      return modifiedDateTime;
    }

    return recentDateTime;
  }

  @Override
  public String toString() {
    return "MaterialRevision{" +
      "modifications=" + modifications +
      ", material=" + material +
      '}';
  }
}
package me.vinitagrawal.gocd.slack.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class MaterialRevision {

  @SerializedName("modifications")
  private List<Modification> modifications;

  @SerializedName("material")
  private Map material;

}
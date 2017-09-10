package me.vinitagrawal.gocd.slack.notifier;

import java.util.ArrayList;
import java.util.List;

public class Message {

  private String text;
  private String pipelineURL;
  private String changes;
  private List<String> ownerList;
  private List<Field> fields = new ArrayList<>();
  private boolean isMinimalisticCheckEnabled;
  private boolean hasPassed;

  public class Field {
    private String title;
    private String value;
    private boolean isValueShort;

    public Field(String title, String value, boolean isValueShort) {
      this.title = title;
      this.value = value;
      this.isValueShort = isValueShort;
    }

    public Field(String title, List<String> valueList, boolean isValueShort) {
      this.title = title;
      this.isValueShort = isValueShort;

      for(String value :  valueList) {
        
      }
    }

    public String getTitle() {
      return title;
    }

    public String getValue() {
      return value;
    }

    public boolean isValueShort() {
      return isValueShort;
    }
  }

  public void addField(String title, String value, boolean isValueShort) {
    fields.add(new Field(title, value, isValueShort));
  }

  public List<Field> getFields() {
    return fields;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getPipelineURL() {
    return pipelineURL;
  }

  public void setPipelineURL(String pipelineURL) {
    this.pipelineURL = pipelineURL;
  }

  public String getChanges() {
    return changes;
  }

  public void setChanges(List<String> changesList) {
    changes = convertListToString(changesList);
  }

  public String getModifiedRepositories(List<String> repositoryList) {
    return convertListToString(repositoryList);
  }

  private String convertListToString(List<String> list) {
    String str = "";
    for(String item : list) {
      str = str.concat(item + "\n");
    }
    return str.trim();
  }

  public List<String> getOwnerList() {
    return ownerList;
  }

  public void setOwnerList(List<String> ownerList) {
    this.ownerList = ownerList;
  }

  public boolean shouldPostMinimalisticMessage() {
    return isMinimalisticCheckEnabled;
  }

  public void setMinimalisticCheckEnabled(boolean minimalisticCheckEnabled) {
    isMinimalisticCheckEnabled = minimalisticCheckEnabled;
  }

  public boolean shouldPostPassedMessage() {
    return hasPassed;
  }

  public void setHasPassed(boolean hasPassed) {
    this.hasPassed = hasPassed;
  }
}

package me.vinitagrawal.gocd.slack.notifier;

import java.util.ArrayList;
import java.util.List;

public class Message {

  private String title;
  private String attachmentTitle;
  private String changes;
  private List<String> ownerList;
  private List<Field> fields = new ArrayList<>();

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

    @Override
    public String toString() {
      return "Field{" +
        "title='" + title + '\'' +
        ", value='" + value + '\'' +
        ", isValueShort=" + isValueShort +
        '}';
    }
  }

  public void addField(String title, String value, boolean isValueShort) {
    fields.add(new Field(title, value, isValueShort));
  }

  public List<Field> getFields() {
    return fields;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAttachmentTitle() {
    return attachmentTitle;
  }

  public void setAttachmentTitle(String attachmentTitle) {
    this.attachmentTitle = attachmentTitle;
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

  @Override
  public String toString() {
    return "Message{" +
      "title='" + title + '\'' +
      ", attachmentTitle='" + attachmentTitle + '\'' +
      ", changes='" + changes + '\'' +
      ", ownerList=" + ownerList +
      ", fields=" + fields +
      '}';
  }
}

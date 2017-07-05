package me.vinitagrawal.gocd.slack.notifier;

import java.util.ArrayList;
import java.util.List;

public class Message {

  private String title;
  private String attachmentTitle;
  private List<Field> fields = new ArrayList<>();

  public class Field {
    private String title;
    private String value;

    public Field(String title, String value) {
      this.title = title;
      this.value = value;
    }

    public String getTitle() {
      return title;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return "Field{" +
        "title='" + title + '\'' +
        ", value='" + value + '\'' +
        '}';
    }
  }

  public void addField(String title, String value) {
    fields.add(new Field(title, value));
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

  @Override
  public String toString() {
    return "Message{" +
      "title='" + title + '\'' +
      ", attachmentTitle='" + attachmentTitle + '\'' +
      ", fields=" + fields +
      '}';
  }
}

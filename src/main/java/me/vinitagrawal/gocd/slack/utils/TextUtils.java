package me.vinitagrawal.gocd.slack.utils;

public class TextUtils {

  public static boolean isNullOrEmpty(String value) {
    return value == null || "".equals(value.trim());
  }
}

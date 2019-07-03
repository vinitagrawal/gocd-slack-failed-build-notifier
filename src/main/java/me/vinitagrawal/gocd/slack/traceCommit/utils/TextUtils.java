package me.vinitagrawal.gocd.slack.traceCommit.utils;

public class TextUtils {

  public static boolean isNullOrEmpty(String value) {
    return value == null || "".equals(value.trim());
  }
}

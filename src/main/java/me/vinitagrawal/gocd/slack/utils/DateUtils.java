package me.vinitagrawal.gocd.slack.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
  public static Date convertStringToDate(String date) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    try {
      return simpleDateFormat.parse(date);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    return null;
  }
}

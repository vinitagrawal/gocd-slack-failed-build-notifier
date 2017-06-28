package me.vinitagrawal.gocd.slack.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class FileUtilities {
  public static String readFrom(String fileName) throws IOException {
    return FileUtils.readFileToString(new File("src/test/resources/" + fileName));
  }

}

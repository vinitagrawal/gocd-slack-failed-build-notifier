package me.vinitagrawal.gocd.slack.apiclient;

import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.logging.Logger;
import me.vinitagrawal.gocd.slack.model.PipelineInstance;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static me.vinitagrawal.gocd.slack.utils.TextUtils.isNullOrEmpty;

public class APIClient {

  private static Logger LOGGER = Logger.getLoggerFor(APIClient.class);
  private String baseURL;
  private String username;
  private String password;

  public APIClient(String baseURL, String username, String password) {
    this.baseURL = baseURL;
    this.username = username;
    this.password = password;
  }

  public PipelineInstance getPipelineInstance(String pipelineName, int counter) {
    try {
      String url = String.format("%s/go/api/pipelines/%s/instance/%d", baseURL, pipelineName, counter);
      LOGGER.info("Connecting to : " + url);
      URLConnection connection = new URL(url).openConnection();
      connection = applyAPIAuthentication(connection);

      return new Gson().fromJson(new InputStreamReader(connection.getInputStream()), PipelineInstance.class);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private URLConnection applyAPIAuthentication(URLConnection connection) {
    if (!isNullOrEmpty(username) && !isNullOrEmpty(password)) {
      String userpass = username + ":" + password;
      String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
      connection.setRequestProperty("Authorization", basicAuth);
    }
    return connection;
  }
}

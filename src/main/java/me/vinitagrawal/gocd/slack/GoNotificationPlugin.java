package me.vinitagrawal.gocd.slack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.DefaultGoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import me.vinitagrawal.gocd.slack.model.*;
import me.vinitagrawal.gocd.slack.utils.PipelineUtilities;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import static java.util.Arrays.asList;

@Extension
public class GoNotificationPlugin implements GoPlugin {

  private static Logger LOGGER = Logger.getLoggerFor(GoNotificationPlugin.class);
  private static final String PLUGIN_ID = "slack-failed-build.notifier";
  private static final String EXTENSION_TYPE = "notification";

  private static final List<String> goSupportedVersions = asList("1.0");
  public static final String GET_PLUGIN_SETTINGS_API = "go.processor.plugin-settings.get";
  public static final String PLUGIN_SETTINGS_GET_CONFIGURATION = "go.plugin-settings.get-configuration";
  public static final String PLUGIN_SETTINGS_GET_VIEW = "go.plugin-settings.get-view";
  public static final String PLUGIN_SETTINGS_VALIDATE_CONFIGURATION = "go.plugin-settings.validate-configuration";
  public static final String REQUEST_NOTIFICATIONS_INTERESTED_IN = "notifications-interested-in";
  public static final String REQUEST_STAGE_STATUS = "stage-status";

  private static final int SUCCESS_RESPONSE_CODE = 200;
  private static final int INTERNAL_ERROR_RESPONSE_CODE = 500;

  private Gson gson = new Gson();
  private GoApplicationAccessor accessor;

  @Override
  public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
    accessor = goApplicationAccessor;
  }

  @Override
  public GoPluginIdentifier pluginIdentifier() {
    return new GoPluginIdentifier(EXTENSION_TYPE, goSupportedVersions);
  }

  @Override
  public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {
    LOGGER.info("GoPluginApiRequest : " + goPluginApiRequest.requestName() + " body : " + goPluginApiRequest.requestBody());
    String requestName = goPluginApiRequest.requestName();
    if (goPluginApiRequest.requestName().equals(PLUGIN_SETTINGS_GET_CONFIGURATION)) {
      return handleGetPluginSettingsConfiguration();
    } else if (goPluginApiRequest.requestName().equals(PLUGIN_SETTINGS_GET_VIEW)) {
      return handleGetPluginSettingsView();
    } else if (goPluginApiRequest.requestName().equals(PLUGIN_SETTINGS_VALIDATE_CONFIGURATION)) {
      return handleValidatePluginSettingsConfiguration(goPluginApiRequest);
    } else if (requestName.equals(REQUEST_NOTIFICATIONS_INTERESTED_IN)) {
      return handleNotificationsInterestedIn();
    } else if (requestName.equals(REQUEST_STAGE_STATUS)) {
      return handleStageStatus(goPluginApiRequest);
    }
    return null;
  }

  private GoPluginApiResponse handleGetPluginSettingsConfiguration() {
    Map<String, Object> response = new HashMap<String, Object>();
    response.put("server_base_url", createField("Server Base URL", null, true, false, "0"));
    response.put("server_api_username", createField("Server API Username", null, false, false, "0"));
    response.put("server_api_password", createField("Server API Password", null, false, true, "0"));
    return renderJSON(SUCCESS_RESPONSE_CODE, response);
  }

  private GoPluginApiResponse handleGetPluginSettingsView() {
    Map<String, Object> response = new HashMap<String, Object>();
    try {
      response.put("template", IOUtils.toString(getClass()
        .getResourceAsStream("/views/plugin-settings.template.html"), "UTF-8"));
    } catch (IOException e) {
      response.put("error", "Can't load view template.");
      return renderJSON(INTERNAL_ERROR_RESPONSE_CODE, response);
    }
    return renderJSON(SUCCESS_RESPONSE_CODE, response);
  }

  private GoPluginApiResponse handleValidatePluginSettingsConfiguration(GoPluginApiRequest goPluginApiRequest) {
    Map<String, Object> requestMap = gson.fromJson(goPluginApiRequest.requestBody(), Map.class);
    Map<String, String> configuration = keyValuePairs(requestMap, "plugin-settings");
    String server_base_url = configuration.get("server_base_url");
    LOGGER.info("\n\nPlugin Settings : " + requestMap + "\n\n");

    if(isNullOrEmpty(server_base_url)) {
      List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
      Map<String, Object> responseMap = new HashMap<>();
      responseMap.put("key", "server_base_url");
      responseMap.put("message", "Enter Server URL");
      response.add(responseMap);
      return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    return renderJSON(SUCCESS_RESPONSE_CODE, new JsonArray());
  }

  private GoPluginApiResponse handleNotificationsInterestedIn() {
    Map<String, Object> response = new HashMap<String, Object>();
    response.put("notifications", Arrays.asList(REQUEST_STAGE_STATUS));

    GoPluginApiResponse goPluginApiResponse = renderJSON(SUCCESS_RESPONSE_CODE, response);
    LOGGER.info("Response : " + goPluginApiResponse.responseBody());
    return goPluginApiResponse;
  }

  private GoPluginApiResponse handleStageStatus(GoPluginApiRequest goPluginApiRequest) {
    GoApiRequestBody goApiRequestBody = gson.fromJson(goPluginApiRequest.requestBody(), GoApiRequestBody.class);

    if (hasStageFailed(goApiRequestBody)) {
      GoApiRequestBody.Pipeline pipeline = goApiRequestBody.getPipeline();
      LOGGER.info(pipeline.getName() + " is failing.");
      determineFailingCommit(
        pipeline.getName(),
        pipeline.getCounter(),
        pipeline.getRevisions(),
        getPluginSettings()
      );
    }

    Map<String, Object> response = new HashMap<String, Object>();
    response.put("status", "success");
    GoPluginApiResponse goPluginApiResponse = renderJSON(SUCCESS_RESPONSE_CODE, response);
    LOGGER.info("\n\nResponse : " + goPluginApiResponse.responseBody());

    return goPluginApiResponse;
  }

  private void determineFailingCommit(String pipelineName, int pipelineCounter,
                                      List<MaterialRevision> materialRevisionList,
                                      PluginSettings pluginSettings) {
    MaterialRevision materialRevision = getChangedMaterialRevision(materialRevisionList);

    if(materialRevision == null) {
      materialRevision = PipelineUtilities.getBuildCauseRevision(materialRevisionList);
    }

    if (materialRevision.isBuildCauseTypePipeline()) {
      String revision = getPipelineRevision(materialRevision);
      LOGGER.info("Pipeline Revision: " + revision);

      String[] pipelineRevision = revision.split("/");
      pipelineName = pipelineRevision[0];
      pipelineCounter = Integer.parseInt(pipelineRevision[1]);
      PipelineInstance pipelineInstance = getPipelineInstance(pluginSettings, pipelineName, pipelineCounter);

      LOGGER.info("PipelineInstance : " + new Gson().toJson(pipelineInstance));
      determineFailingCommit(
        pipelineName,
        pipelineCounter,
        pipelineInstance.getBuildCause().getRevisions(),
        pluginSettings
      );
    }
  }

  private PipelineInstance getPipelineInstance(PluginSettings pluginSettings, String name, int counter) {
    try {
      String url = pluginSettings.getServerBaseUrl() + "/go/api/pipelines/" +
        name + "/instance/" + counter;
      LOGGER.info(" \nConnecting to : " + url);
      URLConnection connection = new URL(url).openConnection();
      applyAPIAuthentication(connection, pluginSettings);

      return gson.fromJson(new InputStreamReader(connection.getInputStream()), PipelineInstance.class);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private URLConnection applyAPIAuthentication(URLConnection connection, PluginSettings pluginSettings) {
    if (!isNullOrEmpty(pluginSettings.getServerApiUsername()) && !isNullOrEmpty(pluginSettings.getServerApiPassword())) {
      String userpass = pluginSettings.getServerApiUsername() + ":" + pluginSettings.getServerApiPassword();
      String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
      connection.setRequestProperty("Authorization", basicAuth);
    }
    return connection;
  }

  public PluginSettings getPluginSettings() {
    DefaultGoApiRequest request = new DefaultGoApiRequest(
      GET_PLUGIN_SETTINGS_API,
      "1.0",
      pluginIdentifier()
    );

    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("plugin-id", PLUGIN_ID);

    request.setRequestBody(gson.toJson(requestMap));
    GoApiResponse response = accessor.submit(request);

    LOGGER.info("getPluginSettings: " + response.responseBody());
    return gson.fromJson(response.responseBody(), PluginSettings.class);
  }

  private MaterialRevision getChangedMaterialRevision(List<MaterialRevision> revisions) {
    for(MaterialRevision materialRevision : revisions) {
      if(materialRevision.isChanged())
        return materialRevision;
    }

    return null;
  }

  private String getPipelineRevision(MaterialRevision materialRevision) {
    Modification modification = materialRevision.getBuildCauseModification();
    return modification.getRevision();
  }

  private boolean hasStageFailed(GoApiRequestBody goApiRequestBody) {
    String result = goApiRequestBody.getPipeline().getStage().getResult();
    return result.equals("Failed");
  }

  private boolean isNullOrEmpty(String value) {
    return value == null || "".equals(value.trim());
  }

  private Map<String, Object> createField(String displayName, String defaultValue, boolean isRequired, boolean isSecure, String displayOrder) {
    Map<String, Object> fieldProperties = new HashMap<String, Object>();
    fieldProperties.put("display-name", displayName);
    fieldProperties.put("default-value", defaultValue);
    fieldProperties.put("required", isRequired);
    fieldProperties.put("secure", isSecure);
    fieldProperties.put("display-order", displayOrder);
    return fieldProperties;
  }

  private Map<String, String> keyValuePairs(Map<String, Object> map, String mainKey) {
    Map<String, String> keyValuePairs = new HashMap<String, String>();
    Map<String, Object> fieldsMap = (Map<String, Object>) map.get(mainKey);
    for (String field : fieldsMap.keySet()) {
      Map<String, Object> fieldProperties = (Map<String, Object>) fieldsMap.get(field);
      String value = (String) fieldProperties.get("value");
      keyValuePairs.put(field, value);
    }
    return keyValuePairs;
  }

  private GoPluginApiResponse renderJSON(final int responseCode, Object response) {
    final String json = response == null ? null : new GsonBuilder().create().toJson(response);
    return new GoPluginApiResponse() {
      @Override
      public int responseCode() {
        return responseCode;
      }

      @Override
      public Map<String, String> responseHeaders() {
        return null;
      }

      @Override
      public String responseBody() {
        return json;
      }
    };
  }
}

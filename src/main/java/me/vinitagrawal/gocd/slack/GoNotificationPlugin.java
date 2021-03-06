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
import me.vinitagrawal.gocd.slack.apiclient.APIClient;
import me.vinitagrawal.gocd.slack.model.*;
import me.vinitagrawal.gocd.slack.notifier.Message;
import me.vinitagrawal.gocd.slack.notifier.SlackNotifier;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static me.vinitagrawal.gocd.slack.utils.TextUtils.isNullOrEmpty;

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
  private String pipelinePath;
  private APIClient apiClient;
  private List<MaterialRevision> materialRevisions;

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
    response.put("is_minimalistic", createField("Minimalistic Text", "false", false, false, "0"));
    response.put("pipeline_names", createField("Pipeline Names", null, false, false, "0"));
    response.put("slack_oauth_token", createField("Slack OAuth Token", null, false, false, "0"));
    response.put("slack_channel", createField("Slack Channel", null, false, false, "0"));
    response.put("slack_bot_name", createField("Slack Bot Name", null, false, false, "0"));
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

    return renderJSON(SUCCESS_RESPONSE_CODE, response);
  }

  private GoPluginApiResponse handleStageStatus(GoPluginApiRequest goPluginApiRequest) {
    GoApiRequestBody goApiRequestBody = gson.fromJson(goPluginApiRequest.requestBody(), GoApiRequestBody.class);
    Map<String, Object> response = new HashMap<String, Object>();
    PluginSettings pluginSettings = getPluginSettings();

    try {
      response.put("status", "success");
      setAPIClient(pluginSettings);
      GoApiRequestBody.Pipeline pipeline = goApiRequestBody.getPipeline();
      if (toBeNotified(pipeline, pluginSettings)) {
        if (pipeline.hasStageFailed())
          postFailedMessage(pluginSettings, pipeline);
        else
          postPassedMessage(pluginSettings, pipeline);
      }
    } catch (Exception e) {
      response.put("status", "failure");
      response.put("messages", e.getMessage());
    }

    return renderJSON(SUCCESS_RESPONSE_CODE, response);
  }

  private void postPassedMessage(PluginSettings pluginSettings, GoApiRequestBody.Pipeline pipeline) {
    postMessageToSlack(pluginSettings, createPassedMessage(pipeline.getPassedStage()));
  }

  private void postFailedMessage(PluginSettings pluginSettings, GoApiRequestBody.Pipeline pipeline) {
    setPipelinePath(pipeline.getPath());
    materialRevisions = new ArrayList<>();
    determineFailingPipeline(pipeline.getName(), pipeline.getCounter(), pipeline.getStage());
    Message message = createFailedMessage(pluginSettings, materialRevisions);
    postMessageToSlack(pluginSettings, message);
  }

  private boolean toBeNotified(GoApiRequestBody.Pipeline pipeline, PluginSettings pluginSettings) {
    List<String> pipelineNameList = pluginSettings.getPipelineNames();

    if ((pipelineNameList.isEmpty() || pipelineNameList.contains(pipeline.getName())) && pipeline.isStageCompleted()) {
      if (pipeline.isRerun())
        return !pipeline.hasStageFailed();
      else
        return hasPipelineStateChanged(pipeline);

    }
    return false;
  }

  private boolean hasPipelineStateChanged(GoApiRequestBody.Pipeline pipeline) {
    PipelineInstance pipelineInstance = apiClient.getPipelineInstance(pipeline.getName(), pipeline.getCounter()-1);
    return pipelineInstance.hasStateChanged(pipeline.getStage());
  }

  private void setAPIClient(PluginSettings pluginSettings) {
    apiClient = new APIClient(
      pluginSettings.getServerBaseUrl(),
      pluginSettings.getServerApiUsername(),
      pluginSettings.getServerApiPassword()
    );
  }

  private void determineFailingPipeline(String pipelineName, int pipelineCounter, Stage stage) {
    PipelineInstance pipelineInstance = apiClient.getPipelineInstance(pipelineName, pipelineCounter-1);
    if (pipelineInstance.hasStageFailed(stage))
      determineFailingPipeline(pipelineName, pipelineCounter-1, stage);
    else
      determineFailingCommit(pipelineName, pipelineCounter);
  }

  private void determineFailingCommit(String pipelineName, int pipelineCounter) {
    PipelineInstance pipelineInstance = apiClient.getPipelineInstance(pipelineName, pipelineCounter);
    List<MaterialRevision> materialRevisionList = pipelineInstance.getBuildCauseRevision();

    for(MaterialRevision materialRevision : materialRevisionList) {
      if (materialRevision.isBuildCauseTypePipeline()) {
        String revision = materialRevision.getPipelineRevision();

        String[] pipelineRevision = revision.split("/");
        pipelineName = pipelineRevision[0];
        pipelineCounter = Integer.parseInt(pipelineRevision[1]);

        determineFailingCommit(pipelineName, pipelineCounter);
      } else if (materialRevision.isBuildCauseTypeGit()) {
        if(!isMaterialPresent(materialRevision.getMaterialFingerprint()))
          materialRevisions.add(materialRevision);
      }
    }
  }

  private boolean isMaterialPresent(String fingerprint) {
    for(MaterialRevision materialRevision : materialRevisions) {
      if(materialRevision.getMaterialFingerprint().equals(fingerprint))
        return true;
    }
    return false;
  }

  private void postMessageToSlack(PluginSettings pluginSettings, Message message) {
    SlackNotifier slackNotifier = new SlackNotifier(
      pluginSettings.getSlackOAuthToken(),
      pluginSettings.getSlackChannelName(),
      pluginSettings.getSlackBotName());

    slackNotifier.postMessage(message);
  }

  public Message createFailedMessage(PluginSettings pluginSettings, List<MaterialRevision> materialRevisions) {
    Message message = new Message();
    message.setText(String.format("The Pipeline %s is failing.", pipelinePath));
    message.setPipelineURL(String.format("%s/go/pipelines/%s", pluginSettings.getServerBaseUrl(), pipelinePath));
    message.addField("Pipeline", pipelinePath, true);
    message.addField("Status", "Failed", true);

    List<String> repositoryList = new ArrayList<>();
    List<String> changes = new ArrayList<>();
    List<String> owners = new ArrayList<>();
    for(MaterialRevision materialRevision : materialRevisions) {
      if(!repositoryList.contains(materialRevision.getMaterialDescription())) {
        repositoryList.add(materialRevision.getMaterialDescription());
        changes.add("\n" + materialRevision.getMaterialDescription());
      }
      changes.addAll(materialRevision.getMaterialChanges());
      owners = materialRevision.getCommitOwners(owners);
    }

    message.addField("Modified Repository", message.getModifiedRepositories(repositoryList), false);
    message.setChanges(changes);
    message.setOwnerList(owners);
    message.setMinimalisticCheckEnabled(pluginSettings.isMinimalisticCheckEnabled());

    return message;
  }

  private Message createPassedMessage(String passedStage) {
    Message message = new Message();
    message.setText(String.format("%s is green now!", passedStage));
    message.setHasPassed(true);
    return message;
  }

  public void setPipelinePath(String path) {
    pipelinePath = path;
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

    return gson.fromJson(response.responseBody(), PluginSettings.class);
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

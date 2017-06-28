package me.vinitagrawal.gocd.slack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import me.vinitagrawal.gocd.slack.model.GoApiRequestBody;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

@Extension
public class GoNotificationPlugin implements GoPlugin {

  private static Logger LOGGER = Logger.getLoggerFor(GoNotificationPlugin.class);
  private static final String PLUGIN_ID = "slack-failed-build.notifier";
  private static final String EXTENSION_TYPE = "notification";

  private static final List<String> goSupportedVersions = asList("1.0");
  public static final String REQUEST_NOTIFICATIONS_INTERESTED_IN = "notifications-interested-in";
  public static final String REQUEST_STAGE_STATUS = "stage-status";

  private static final int SUCCESS_RESPONSE_CODE = 200;

  private Gson gson = new Gson();

  @Override
  public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {

  }

  @Override
  public GoPluginIdentifier pluginIdentifier() {
    return new GoPluginIdentifier(EXTENSION_TYPE, goSupportedVersions);
  }

  @Override
  public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {
    LOGGER.info("GoPluginApiRequest : " + goPluginApiRequest.requestName() + " body : " + goPluginApiRequest.requestBody());
    String requestName = goPluginApiRequest.requestName();
    if (requestName.equals(REQUEST_NOTIFICATIONS_INTERESTED_IN)) {
      return handleNotificationsInterestedIn();
    } else if (requestName.equals(REQUEST_STAGE_STATUS)) {
      return handleStageStatus(goPluginApiRequest);
    }
    return null;
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
      LOGGER.info(goApiRequestBody.getPipeline().getName() + " is failing.");
    }

    Map<String, Object> response = new HashMap<String, Object>();
    response.put("status", "success");
    GoPluginApiResponse goPluginApiResponse = renderJSON(SUCCESS_RESPONSE_CODE, response);
    LOGGER.info("\n\nResponse : " + goPluginApiResponse.responseBody());

    return goPluginApiResponse;
  }

  private boolean hasStageFailed(GoApiRequestBody goApiRequestBody) {
    String result = goApiRequestBody.getPipeline().getStage().getResult();
    return result.equals("Failed");
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

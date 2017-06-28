package me.vinitagrawal.gocd.slack;

import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import me.vinitagrawal.gocd.slack.utils.FileUtilities;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static me.vinitagrawal.gocd.slack.GoNotificationPlugin.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GoNotificationPluginTest {

  private static final String INTERESTED_NOTIFICATION_RESPONSE = "{\"notifications\":[\"stage-status\"]}";
  private GoNotificationPlugin plugin;
  private GoPluginApiRequest request;

  @Before
  public void setUp() throws Exception {
    plugin = new GoNotificationPlugin();
    request = mock(GoPluginApiRequest.class);
  }

  private GoPluginApiResponse handlePluginRequest(String requestType, String fileName) {
    when(request.requestName()).thenReturn(requestType);
    try {
      if(fileName != null)
        when(request.requestBody()).thenReturn(FileUtilities.readFrom(fileName));
    } catch (IOException e) {
      e.printStackTrace();
    }

    return plugin.handle(request);
  }

  @Test
  public void shouldReturnPluginIdentifier() {
    GoPluginIdentifier goPluginIdentifier = plugin.pluginIdentifier();

    assertThat(goPluginIdentifier.getExtension(), equalTo("notification"));
    assertThat(goPluginIdentifier.getSupportedExtensionVersions(), equalTo(Arrays.asList("1.0")));
  }

  @Test
  public void shouldHandleNotificationInterestedInRequest() {
    GoPluginApiResponse apiResponse = handlePluginRequest(REQUEST_NOTIFICATIONS_INTERESTED_IN, null);

    assertThat(apiResponse, is(notNullValue()));
    assertThat(apiResponse.responseCode(), equalTo(200));
    assertThat(apiResponse.responseHeaders(), equalTo(null));
    assertThat(apiResponse.responseBody(), equalTo(INTERESTED_NOTIFICATION_RESPONSE));
  }

  @Test
  public void shouldHandleStageStatusAndReturnSuccess() throws Exception {
    GoPluginApiResponse apiResponse = handlePluginRequest(REQUEST_STAGE_STATUS, "go_api_request_body.json");

    assertThat(apiResponse, is(notNullValue()));
    assertThat(apiResponse.responseBody(), equalTo("{\"status\":\"success\"}"));
  }

  @Test
  public void shouldGetPluginSettingsViewWhenRequested() {
    GoPluginApiResponse apiResponse = handlePluginRequest(PLUGIN_SETTINGS_GET_VIEW, null);

    assertThat(apiResponse, is(notNullValue()));
    assertThat(apiResponse.responseBody(), containsString("div class"));
  }

  @Test
  public void shouldGetConfigurationForPluginSettings() {
    GoPluginApiResponse apiResponse = handlePluginRequest(PLUGIN_SETTINGS_GET_CONFIGURATION, null);

    assertThat(apiResponse, is(notNullValue()));
    Map<String, Object> responseMap = new Gson().fromJson(apiResponse.responseBody(), Map.class);
    assertTrue(responseMap.containsKey("server_base_url"));
    assertTrue(responseMap.containsKey("server_api_password"));
    assertTrue(responseMap.containsKey("server_api_username"));
  }

  @Test
  public void shouldValidatePluginConfiguration() throws Exception {
    GoPluginApiResponse apiResponse = handlePluginRequest(PLUGIN_SETTINGS_VALIDATE_CONFIGURATION, "validate_plugin_configuration.json");

    assertThat(apiResponse, is(notNullValue()));
    assertThat(apiResponse.responseCode(), equalTo(200));
  }

  @Test
  public void shouldValidateServerBaseURLFromPluginConfiguration() throws Exception {
    GoPluginApiResponse apiResponse = handlePluginRequest(PLUGIN_SETTINGS_VALIDATE_CONFIGURATION, "validate_plugin_configuration_empty_server_url.json");

    assertThat(apiResponse, is(notNullValue()));
    assertThat(apiResponse.responseCode(), equalTo(200));
    assertThat(apiResponse.responseBody(), containsString("Enter Server URL"));
  }

  @Test
  public void shouldReturnNullWhenRequestIsInvalid() {
    GoPluginApiResponse apiResponse = handlePluginRequest("Invalid", null);

    assertThat(apiResponse, equalTo(null));
  }

}
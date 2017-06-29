package me.vinitagrawal.gocd.slack;

import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import me.vinitagrawal.gocd.slack.model.PluginSettings;
import me.vinitagrawal.gocd.slack.testUtils.FileUtilities;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Map;

import static me.vinitagrawal.gocd.slack.GoNotificationPlugin.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GoNotificationPlugin.class)
public class GoNotificationPluginTest {

  private static final String INTERESTED_NOTIFICATION_RESPONSE = "{\"notifications\":[\"stage-status\"]}";
  private GoNotificationPlugin plugin;
  private GoPluginApiRequest request;
  private GoApplicationAccessor accessor;

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

  private void setupPluginSettings() {
    accessor = mock(GoApplicationAccessor.class);
    when(accessor.submit(Matchers.any(GoApiRequest.class))).thenReturn(getGoApiResponseForPlugin());

    plugin.initializeGoApplicationAccessor(accessor);
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
    setupPluginSettings();
    setupURLConnection();

    GoPluginApiResponse apiResponse = handlePluginRequest(REQUEST_STAGE_STATUS, "go_api_request_body.json");

    assertThat(apiResponse, is(notNullValue()));
    assertThat(apiResponse.responseBody(), equalTo("{\"status\":\"success\"}"));
  }

  private void setupURLConnection() throws Exception {
    URL url = PowerMockito.mock(URL.class);
    URLConnection connection = mock(URLConnection.class);
    String pipelineInstance = FileUtilities.readFrom("pipeline_instance.json");
    InputStream inputStream = IOUtils.toInputStream(pipelineInstance);

    PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(url);
    when(url.openConnection()).thenReturn(connection);
    when(connection.getInputStream()).thenReturn(inputStream);
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
  public void shouldValidatePluginConfiguration() {
    GoPluginApiResponse apiResponse = handlePluginRequest(PLUGIN_SETTINGS_VALIDATE_CONFIGURATION, "validate_plugin_configuration.json");

    assertThat(apiResponse, is(notNullValue()));
    assertThat(apiResponse.responseCode(), equalTo(200));
  }

  @Test
  public void shouldValidateServerBaseURLFromPluginConfiguration() {
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

  @Test
  public void shouldGetPluginSettings() throws Exception {
    setupPluginSettings();

    PluginSettings pluginSettings = plugin.getPluginSettings();
    assertThat(pluginSettings.getServerBaseUrl(), equalTo("http://localhost:8153"));
  }

  private GoApiResponse getGoApiResponseForPlugin() {
    return new GoApiResponse() {
      @Override
      public int responseCode() {
        return 200;
      }

      @Override
      public Map<String, String> responseHeaders() {
        return null;
      }

      @Override
      public String responseBody() {
        return "{\"server_base_url\":\"http://localhost:8153\",\"server_api_password\":\"\",\"server_api_username\":\"\"}";
      }
    };
  }
}
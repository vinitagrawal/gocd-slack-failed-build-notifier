package me.vinitagrawal.gocd.slack;

import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import me.vinitagrawal.gocd.slack.utils.FileUtilities;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static me.vinitagrawal.gocd.slack.GoNotificationPlugin.REQUEST_NOTIFICATIONS_INTERESTED_IN;
import static me.vinitagrawal.gocd.slack.GoNotificationPlugin.REQUEST_STAGE_STATUS;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
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

  private GoPluginApiResponse handlePluginRequest(String requestType) {
    when(request.requestName()).thenReturn(requestType);
    try {
      when(request.requestBody()).thenReturn(FileUtilities.readFrom("go_api_request_body.json"));
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
    GoPluginApiResponse apiResponse = handlePluginRequest(REQUEST_NOTIFICATIONS_INTERESTED_IN);

    assertThat(apiResponse, is(notNullValue()));
    assertThat(apiResponse.responseCode(), equalTo(200));
    assertThat(apiResponse.responseHeaders(), equalTo(null));
    assertThat(apiResponse.responseBody(), equalTo(INTERESTED_NOTIFICATION_RESPONSE));
  }

  @Test
  public void shouldHandleStageStatusAndReturnSuccess() throws Exception {
    GoPluginApiResponse apiResponse = handlePluginRequest(REQUEST_STAGE_STATUS);

    assertThat(apiResponse, is(notNullValue()));
    assertThat(apiResponse.responseBody(), equalTo("{\"status\":\"success\"}"));
  }

  @Test
  public void shouldReturnNullWhenRequestIsInvalid() {
    GoPluginApiResponse apiResponse = handlePluginRequest("Invalid");

    assertThat(apiResponse, equalTo(null));
  }

}
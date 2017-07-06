package me.vinitagrawal.gocd.slack.apiclient;

import me.vinitagrawal.gocd.slack.model.MaterialRevision;
import me.vinitagrawal.gocd.slack.model.PipelineInstance;
import me.vinitagrawal.gocd.slack.testUtils.FileUtilities;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(APIClient.class)
public class APIClientTest {

  private APIClient apiClient;
  private URLConnection connection;

  @Before
  public void setUp() throws Exception {
    apiClient = new APIClient("http://localhost:8153", "username", "password");

    URL url = PowerMockito.mock(URL.class);
    connection = mock(URLConnection.class);

    PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(url);
    when(url.openConnection()).thenReturn(connection);
  }

  @Test
  public void shouldReturnPipelineInstance() throws Exception {
    String pipeline = FileUtilities.readFrom("pipeline_instance.json");
    InputStream inputStream = IOUtils.toInputStream(pipeline);
    when(connection.getInputStream()).thenReturn(inputStream);

    PipelineInstance pipelineInstance = apiClient.getPipelineInstance("Droid", 3);

    MaterialRevision materialRevision = pipelineInstance.getBuildCauseRevision();
    assertThat(materialRevision.getMaterialDescription(), equalTo("\nURL: https://github.com/gocd/gocd, Branch: master"));
    assertThat(materialRevision.getPipelineRevision(), equalTo("a788f1876e2e1f6e5a1e91006e75cd1d467a0edb"));
  }
}
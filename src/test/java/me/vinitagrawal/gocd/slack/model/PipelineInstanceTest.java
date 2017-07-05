package me.vinitagrawal.gocd.slack.model;

import com.google.gson.Gson;
import me.vinitagrawal.gocd.slack.testUtils.FileUtilities;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class PipelineInstanceTest {

  private PipelineInstance pipelineInstance;

  @Before
  public void setUp() throws Exception {
    String pipelineJson = FileUtilities.readFrom("pipeline_instance.json");
    pipelineInstance = new Gson().fromJson(pipelineJson, PipelineInstance.class);
  }

  @Test
  public void shouldReturnBuildCauseRevision() throws Exception {
    MaterialRevision materialRevision = pipelineInstance.getBuildCauseRevision();

    assertThat(materialRevision.getPipelineRevision(), equalTo("a788f1876e2e1f6e5a1e91006e75cd1d467a0edb"));
    assertThat(materialRevision.getMaterialDescription(), equalTo("\nURL: https://github.com/gocd/gocd, Branch: master"));
  }
}
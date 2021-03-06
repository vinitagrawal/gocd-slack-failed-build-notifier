package me.vinitagrawal.gocd.slack.notifier;

import com.oracle.tools.packager.Log;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class SlackNotifierTest {

  private SlackNotifier slackNotifier;

  @Before
  public void setUp() throws Exception {
    String token = System.getenv("SLACK_API_TOKEN");

    Log.info("token" + token);
    slackNotifier = new SlackNotifier(token, "#pipelinestatusbot", "Pipeline");
  }

  @Test
  public void shouldPostMessageOnSlack() throws Exception {
    Message message = new Message();
    message.setText("Pipeline Failing");
    message.setPipelineURL("pipeline link");
    message.setChanges(Arrays.asList("Few commits changed"));
    message.setOwnerList(Arrays.asList("Pick E Reader <pick.e.reader@example.com>"));
    message.addField("Status", "Failed", true);
    message.setMinimalisticCheckEnabled(false);

    slackNotifier.postMessage(message);
  }
}
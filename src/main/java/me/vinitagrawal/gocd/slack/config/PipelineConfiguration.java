package me.vinitagrawal.gocd.slack.config;

import java.util.ArrayList;

public class PipelineConfiguration {
  private ArrayList<String> pipelines;

  public ArrayList<String> getPipelines() {
    return pipelines;
  }

  public void setPipelines(ArrayList<String> pipelines) {
    this.pipelines = pipelines;
  }
}

package me.vinitagrawal.gocd.slack.traceCommit;

import me.vinitagrawal.gocd.slack.traceCommit.apiclient.APIClient;
import me.vinitagrawal.gocd.slack.traceCommit.model.MaterialRevision;
import me.vinitagrawal.gocd.slack.traceCommit.model.PipelineInstance;
import me.vinitagrawal.gocd.slack.traceCommit.model.Stage;

import java.util.ArrayList;
import java.util.List;

public class TraceCommit {

  private APIClient apiClient;
  private List<MaterialRevision> materialRevisions;

  public APIClient setAPIClient(String serverBaseUrl, String username, String password) {
    apiClient = new APIClient(
      serverBaseUrl,
      username,
      password
    );

    return apiClient;
  }

  public List<MaterialRevision> determineFailureMaterial(String pipelineName, int pipelineCounter, Stage stage) {
    materialRevisions = new ArrayList<>();
    determineFailingPipeline(pipelineName, pipelineCounter, stage);
    return materialRevisions;
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
}

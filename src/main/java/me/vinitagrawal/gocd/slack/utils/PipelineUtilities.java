package me.vinitagrawal.gocd.slack.utils;


import me.vinitagrawal.gocd.slack.model.MaterialRevision;

import java.util.Date;
import java.util.List;

public class PipelineUtilities {

  public static MaterialRevision getBuildCauseRevision(List<MaterialRevision> revisions) {
    MaterialRevision latestRevision = revisions.get(0);
    for(int i=1; i<revisions.size(); i++) {
      if(!isLatestBuildCause(latestRevision, revisions.get(i)))
        latestRevision = revisions.get(i);
    }
    return latestRevision;
  }

  private static boolean isLatestBuildCause(MaterialRevision latestRevision, MaterialRevision revision) {
    Date latestModifiedTime = latestRevision.getLatestModifiedTime();
    Date modifiedTime = revision.getLatestModifiedTime();

    return latestModifiedTime.after(modifiedTime);
  }

}

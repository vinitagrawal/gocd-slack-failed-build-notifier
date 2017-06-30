package me.vinitagrawal.gocd.slack.model;

import com.google.gson.Gson;
import me.vinitagrawal.gocd.slack.testUtils.FileUtilities;
import org.exparity.hamcrest.date.DateMatchers;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MaterialRevisionTest {

  private MaterialRevision materialRevision;

  @Before
  public void setUp() throws Exception {
    String revisionJson = FileUtilities.readFrom("material_revision.json");
    materialRevision = new Gson().fromJson(revisionJson, MaterialRevision.class);
  }

  @Test
  public void shouldReturnLatestModifiedTimeWhichCausedTheBuild() throws Exception {
    Date latestDate = materialRevision.getLatestModifiedTime();

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    Date date = simpleDateFormat.parse("2017-06-20T16:50:30.619Z");

    assertThat(latestDate, DateMatchers.sameInstant(date));
  }

  @Test
  public void shouldReturnModificationWhichCausedTheBuild() throws Exception {
    Modification buildCauseModification = materialRevision.getBuildCauseModification();

    assertThat(buildCauseModification.getRevision(), equalTo("mocks/2/MockServer/3"));
    assertThat(buildCauseModification.getModifiedTime().toString(), equalTo("Tue Jun 20 16:50:30 IST 2017"));
  }

  @Test
  public void shouldReturnTrueIfBuildCauseTypeIsPipeline() throws Exception {
    boolean isBuildCausePipeline = materialRevision.isBuildCauseTypePipeline();

    assertTrue(isBuildCausePipeline);
  }

  @Test
  public void shouldReturnFalseIfBuildCauseTypeIsGit() throws Exception {
    boolean isBuildCausePipeline = materialRevision.isBuildCauseTypeGit();

    assertFalse(isBuildCausePipeline);
  }
}
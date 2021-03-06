package me.vinitagrawal.gocd.slack.model;

import com.google.gson.Gson;
import me.vinitagrawal.gocd.slack.testUtils.FileUtilities;
import org.exparity.hamcrest.date.DateMatchers;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    Date date = simpleDateFormat.parse("2017-06-20T22:40:30.619Z");

    assertThat(latestDate, DateMatchers.sameInstant(date));
  }

  @Test
  public void shouldReturnModificationRevisionWhichCausedTheBuild() throws Exception {
    String revision = materialRevision.getPipelineRevision();

    assertThat(revision, equalTo("a788f1876e26e5a1e91006e75cd1d467a0edb"));
  }

  @Test
  public void shouldReturnTrueIfBuildCauseTypeIsPipeline() throws Exception {
    boolean isBuildCausePipeline = materialRevision.isBuildCauseTypePipeline();

    assertFalse(isBuildCausePipeline);
  }

  @Test
  public void shouldReturnFalseIfBuildCauseTypeIsGit() throws Exception {
    boolean isBuildCausePipeline = materialRevision.isBuildCauseTypeGit();

    assertTrue(isBuildCausePipeline);
  }

  @Test
  public void shouldReturnDescriptionOfTheMaterial() throws Exception {
    String materialDescription = materialRevision.getMaterialDescription();

    assertThat(materialDescription, equalTo("\nURL: https://github.com/gocd/gocd, Branch: master"));
  }

  @Test
  public void shouldReturnCommitOwners() throws Exception {
    List<String> commitOwnerList = materialRevision.getCommitOwners(new ArrayList<String>());

    assertThat(commitOwnerList.size(), equalTo(1));
    assertThat(commitOwnerList.get(0), equalTo("Pick E Reader <pick.e.reader@example.com>"));
  }

  @Test
  public void shouldReturnMaterialChanges() throws Exception {
    List<String> changes = materialRevision.getMaterialChanges();

    assertThat(changes.size(), equalTo(3));
  }
}
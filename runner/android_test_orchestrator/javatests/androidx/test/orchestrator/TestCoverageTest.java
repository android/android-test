package androidx.test.orchestrator;

import static androidx.test.orchestrator.OrchestratorConstants.AJUR_COVERAGE;
import static androidx.test.orchestrator.OrchestratorConstants.AJUR_COVERAGE_FILE;
import static androidx.test.orchestrator.OrchestratorConstants.COVERAGE_FILE_PATH;
import static androidx.test.orchestrator.OrchestratorConstants.ISOLATED_ARGUMENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TestCoverageTest {

  @Test
  public void returnsNullWhenCoverageIsDisabled() {
    Bundle b = new Bundle();
    b.putString(AJUR_COVERAGE, "false");
    assertThat(AndroidTestOrchestrator.addTestCoverageSupport(b, "coverage"), is(nullValue()));
  }

  @Test
  public void returnsNullWhenIsolatedModeDisabled() {
    Bundle b = new Bundle();
    b.putString(AJUR_COVERAGE, "true");
    b.putString(ISOLATED_ARGUMENT, "false");
    assertThat(AndroidTestOrchestrator.addTestCoverageSupport(b, "ignore"), is(nullValue()));
  }

  @Test
  public void ignoreWhenNoFilePathSet() {
    Bundle b = new Bundle();
    b.putString(AJUR_COVERAGE, "true");
    assertThat(AndroidTestOrchestrator.addTestCoverageSupport(b, "ignore"), is(nullValue()));
  }

  @Test
  public void ignoreWhenNoFilePathIsEmpty() {
    Bundle b = new Bundle();
    b.putString(AJUR_COVERAGE, "true");
    b.putString(COVERAGE_FILE_PATH, "");
    assertThat(AndroidTestOrchestrator.addTestCoverageSupport(b, "ignore"), is(nullValue()));
  }

  @Test
  public void throwWhenConflictingArgsAreUsed() {
    Bundle b = new Bundle();
    b.putString(AJUR_COVERAGE, "true");
    b.putString(COVERAGE_FILE_PATH, "/foo/bar/");
    b.putString(AJUR_COVERAGE_FILE, "/baz/coverage.ec");
    try {
      AndroidTestOrchestrator.addTestCoverageSupport(b, "name");
      fail("IllegalStateException not thrown");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void verifySuccessfulConstructionOfTheCoveragePath() {
    Bundle b = new Bundle();
    b.putString(AJUR_COVERAGE, "true");
    String path = "/foo/bar/";
    String filename = "com.pkg.Class#method1";
    b.putString(COVERAGE_FILE_PATH, path);
    assertThat(
        AndroidTestOrchestrator.addTestCoverageSupport(b, filename), is(path + filename + ".ec"));
  }
}

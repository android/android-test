package androidx.test.internal.events.client;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class JUnitDescriptionParserTest {

  @Test
  public void getAllTestCaseDescriptions_getsAllLeafNodes() {
    Description rootDesc = Description.createSuiteDescription("root");
    Description nestedRunnerDesc = Description.createSuiteDescription("nested runner");
    Description testClass1Desc = Description.createSuiteDescription(Object.class);
    Description testClass2Desc = Description.createSuiteDescription(Test.class);
    String method1 = "method1";
    String method2 = "method2";
    String method3 = "method3";
    Description testMethod1Desc =
        Description.createTestDescription(testClass1Desc.getClassName(), method1, method1);
    Description testMethod2Desc =
        Description.createTestDescription(testClass1Desc.getClassName(), method2, method2);
    Description testMethod3Desc =
        Description.createTestDescription(testClass1Desc.getClassName(), method3, method3);

    // "root"
    //  |--> "nested runner"
    //       |--> Test
    //            | --> Test.method3
    //       |--> Test
    //            | --> Test.method3
    //  |--> MyTestClass
    //       |--> Object.method1
    //       |--> Object.method2
    rootDesc.addChild(testClass1Desc);
    rootDesc.addChild(nestedRunnerDesc);
    rootDesc.addChild(nestedRunnerDesc);
    nestedRunnerDesc.addChild(testClass2Desc);
    testClass1Desc.addChild(testMethod1Desc);
    testClass1Desc.addChild(testMethod2Desc);
    testClass2Desc.addChild(testMethod3Desc);

    assertThat(JUnitDescriptionParser.getAllTestCaseDescriptions(rootDesc))
        .containsExactly(testMethod1Desc, testMethod2Desc, testMethod3Desc, testMethod3Desc);
  }
}

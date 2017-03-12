package com.satspeedy.bpm;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.junit.Assert.*;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
public class InMemoryH2Test {

  @ClassRule
  @Rule
  public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

  private static final String PROCESS_DEFINITION_KEY = "Process_ApproveOrder";

  static {
    LogFactory.useSlf4jLogging(); // MyBatis
  }

  @Before
  public void setup() {
    init(rule.getProcessEngine());
  }

  /**
   * Just tests if the process definition is deployable.
   */
  @Test
  @Deployment(resources = "approveOrder.bpmn")
  public void testParsingAndDeployment() {
    // nothing is done here, as we just want to check for exceptions during deployment
  }

  @Test
  @Deployment(resources = "approveOrder.bpmn")
  public void testHappyPath() {
    Map startParams = new HashMap();
    startParams.put("valid", true);
    
    ProcessInstance processInstance = processEngine().getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY, startParams);
    assertThat(processInstance).isWaitingAt("StartEvent_1");
    execute(job());

    ProcessDefinition processDefinition = repositoryService().createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
    if ("approveOrder_v3".equals(processDefinition.getVersionTag())) {
      assertThat(processInstance).isWaitingAt("Task_0pyjcdd");
      execute(job());
    }

    assertThat(processInstance).isWaitingAt("Task_117ilnv");
    Map params = new HashMap();
    params.put("approved", true);
    complete(task(), params);

    assertThat(processInstance).isEnded();
  }

}

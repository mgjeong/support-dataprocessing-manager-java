package org.edgexfoundry.support.dataprocessing.runtime;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.ComponentUISpecification;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.UIField;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.UIField.UIFieldType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.WorkflowComponentBundleType;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class Bootstrap {

  private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

  private final WorkflowTableManager workflowTableManager;

  public Bootstrap(String jdbcUrl) {
    workflowTableManager = new WorkflowTableManager(jdbcUrl);
  }

  private void addBuiltinWorkflowComponentBundles() {
    addBuiltinWorkflowWorkflowComponentBundles();
    addBuiltinWorkflowSourceComponentBundles();
    addBuiltinWorkflowSinkComponentBundles();
  }

  private void addBuiltinWorkflowSinkComponentBundles() {
    WorkflowComponentBundle dpfwSink = new WorkflowComponentBundle();
    dpfwSink.setName("DPFW-SINK");
    dpfwSink.setType(WorkflowComponentBundleType.SINK);
    dpfwSink.setStreamingEngine("FLINK");
    dpfwSink.setSubType("DPFW");
    dpfwSink.setBundleJar("");

    ComponentUISpecification componentUISpecification = new ComponentUISpecification();
    addUIField(componentUISpecification, "Data Type", "dataType", "Enter data type");
    addUIField(componentUISpecification, "Data Sink", "dataSink", "Enter data sink");
    dpfwSink.setWorkflowComponentUISpecification(componentUISpecification);

    dpfwSink.setTransformationClass("");
    dpfwSink.setBuiltin(true);

    WorkflowComponentBundle existingBundle =
        workflowTableManager.getWorkflowComponentBundle(dpfwSink.getName(), dpfwSink.getType(),
            dpfwSink.getSubType());
    if (existingBundle == null) {
      dpfwSink = workflowTableManager.addWorkflowComponentBundle(dpfwSink);
    } else {
      dpfwSink.setId(existingBundle.getId());
      dpfwSink = workflowTableManager.addOrUpdateWorkflowComponentBundle(dpfwSink);
    }
    LOGGER.info("Sink id={}/name={} added.", dpfwSink.getId(), dpfwSink.getName());
  }

  private void addBuiltinWorkflowSourceComponentBundles() {
    WorkflowComponentBundle dpfwSource = new WorkflowComponentBundle();
    dpfwSource.setName("DPFW-SOURCE");
    dpfwSource.setType(WorkflowComponentBundleType.SOURCE);
    dpfwSource.setStreamingEngine("FLINK");
    dpfwSource.setSubType("DPFW");
    dpfwSource.setBundleJar("");
    dpfwSource.setTransformationClass("");

    ComponentUISpecification componentUISpecification = new ComponentUISpecification();
    addUIField(componentUISpecification, "Data Type", "dataType", "Enter data type");
    addUIField(componentUISpecification, "Data Source", "dataSource", "Enter data source");
    dpfwSource.setWorkflowComponentUISpecification(componentUISpecification);

    dpfwSource.setTransformationClass("");
    dpfwSource.setBuiltin(true);

    WorkflowComponentBundle existingBundle =
        workflowTableManager.getWorkflowComponentBundle(dpfwSource.getName(), dpfwSource.getType(),
            dpfwSource.getSubType());
    if (existingBundle == null) {
      dpfwSource = workflowTableManager.addWorkflowComponentBundle(dpfwSource);
    } else {
      dpfwSource.setId(existingBundle.getId());
      dpfwSource = workflowTableManager.addOrUpdateWorkflowComponentBundle(dpfwSource);
    }
    LOGGER.info("Source id={}/name={} added.", dpfwSource.getId(), dpfwSource.getName());
  }

  private void addBuiltinWorkflowWorkflowComponentBundles() {
    WorkflowComponentBundle runtimeWorkflow = new WorkflowComponentBundle();
    runtimeWorkflow.setName("Runtime workflow");
    runtimeWorkflow.setType(WorkflowComponentBundleType.WORKFLOW);
    runtimeWorkflow.setStreamingEngine("FLINK");
    runtimeWorkflow.setSubType("WORKFLOW");
    runtimeWorkflow.setBundleJar("");

    ComponentUISpecification componentUISpecification = new ComponentUISpecification();
    UIField runtimeHost = new UIField();
    runtimeHost.setUiName("Runtime host");
    runtimeHost.setFieldName("runtimeHost");
    runtimeHost.setUserInput(true);
    runtimeHost.setTooltip("Enter hostname of runtime edge.");
    runtimeHost.setOptional(false);
    runtimeHost.setType(UIFieldType.STRING);
    runtimeHost.setDefaultValue("localhost:8082");
    componentUISpecification.addUIField(runtimeHost);
    UIField targetHost = new UIField();
    targetHost.setUiName("Target host");
    targetHost.setFieldName("targetHost");
    targetHost.setUserInput(true);
    targetHost.setTooltip("Enter hostname of target edge.");
    targetHost.setOptional(false);
    targetHost.setType(UIFieldType.STRING);
    targetHost.setDefaultValue("localhost:9092");
    componentUISpecification.addUIField(targetHost);
    runtimeWorkflow.setWorkflowComponentUISpecification(componentUISpecification);

    runtimeWorkflow.setTransformationClass("dummy");
    runtimeWorkflow.setBuiltin(true);

    WorkflowComponentBundle existingBundle =
        workflowTableManager
            .getWorkflowComponentBundle(runtimeWorkflow.getName(), runtimeWorkflow.getType(),
                runtimeWorkflow.getSubType());
    if (existingBundle == null) {
      runtimeWorkflow = workflowTableManager.addWorkflowComponentBundle(runtimeWorkflow);
    } else {
      runtimeWorkflow.setId(existingBundle.getId());
      runtimeWorkflow = workflowTableManager.addOrUpdateWorkflowComponentBundle(runtimeWorkflow);
    }
    LOGGER.info("Workflow id={}/name={} added.",
        runtimeWorkflow.getId(), runtimeWorkflow.getName());
  }

  private void addUIField(ComponentUISpecification componentUISpecification, String uiName,
      String fieldName, String tooltip) {
    UIField field = new UIField();
    field.setUiName(uiName);
    field.setFieldName(fieldName);
    field.setUserInput(true);
    field.setTooltip(tooltip);
    field.setOptional(false);
    field.setType(UIFieldType.STRING);
    componentUISpecification.addUIField(field);
  }

  public void execute() throws Exception {
    createTablesIfNotExist();

    //addBuiltinWorkflowComponentBundles();
  }

  private void createTablesIfNotExist() {
    ResourceLoader resourceLoader = new DefaultResourceLoader(getClass().getClassLoader());
    Resource resource = resourceLoader.getResource("db/sqlite/create_tables.sql");
    workflowTableManager.executeSqlScript(resource);
  }
}

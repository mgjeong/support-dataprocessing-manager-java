package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.io.File;
import java.util.Collection;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.Workflow;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponent;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.ComponentUISpecification;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.UIField;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.UIField.UIFieldType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.WorkflowComponentBundleType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEditorMetadata;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSource;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowStream;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowStream.Field;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowStream.SchemaType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WorkflowTableManager.class)
public class WorkflowTableManagerTest {

  private static WorkflowTableManager storageManager = WorkflowTableManager.getInstance();
  private static File sqliteFile = new File("./" + Settings.DB_TEST_PATH);

  @BeforeClass
  public static void setup() throws Exception {
    if (sqliteFile.exists() && !sqliteFile.delete()) {
      throw new RuntimeException("Failed to clean " + sqliteFile.getPath());
    }

    storageManager.initialize("jdbc:sqlite:" + sqliteFile.getPath());
    ResourceLoader loader = new DefaultResourceLoader(ClassLoader.getSystemClassLoader());
    Resource resource = loader.getResource("db/sqlite/create_tables.sql");
    storageManager.executeSqlScript(resource);
  }

  @Test
  public void testSelectInvalidWorkflow() throws Exception {
    Workflow workflow = storageManager.getWorkflow(-1L);
    Assert.assertNull(workflow);
  }

  @Test
  public void testListWorkflows() throws Exception {
    Collection<Workflow> workflows = storageManager.listWorkflows();
    Assert.assertTrue(workflows != null); // may not be empty due to concurrent tests
  }

  @Test
  public void testAddWorkflow() throws Exception {
    Workflow workflow = insertSampleWorkflow();

    Long workflowId = workflow.getId();
    try {
      Workflow dbWorkflow = storageManager.getWorkflow(workflowId);
      Assert.assertEquals(dbWorkflow.getName(), workflow.getName());
      Assert.assertEquals(dbWorkflow.getConfig("targetHost").toString(),
          workflow.getConfig("targetHost").toString());
    } finally {
      storageManager.removeWorkflow(workflowId); // delete test workflow
    }
  }

  @Test
  public void testUpdateWorkflow() throws Exception {
    Workflow workflow = insertSampleWorkflow();

    Long workflowId = workflow.getId();
    try {
      workflow.addConfig("payload", "sample");
      workflow.addConfig("targetHost", "192.168.0.3");
      Workflow dbWorkflow = storageManager.updateWorkflow(workflowId, workflow);

      Assert.assertEquals(dbWorkflow.getName(), workflow.getName());
      Assert.assertEquals(dbWorkflow.getConfig("targetHost").toString(),
          workflow.getConfig("targetHost").toString());
      Assert.assertEquals(dbWorkflow.getConfig("payload").toString(),
          workflow.getConfig("payload").toString());
    } finally {
      storageManager.removeWorkflow(workflowId); // delete test workflow
    }
  }

  private Workflow insertSampleWorkflow() {
    Workflow workflow = new Workflow();
    workflow.setId(null);
    workflow.setName("FirstWorkflow");
    workflow.addConfig("targetHost", "192.168.0.1");
    workflow = storageManager.addWorkflow(workflow);
    Assert.assertTrue(workflow.getId() != null); // added successfully
    return workflow;
  }

  @Test
  public void testWorkflowEditorMetadata() throws Exception {
    Workflow workflow = insertSampleWorkflow();

    try {
      WorkflowEditorMetadata editorMetadata = new WorkflowEditorMetadata();
      editorMetadata.setWorkflowId(workflow.getId());
      editorMetadata.setData("{}");

      // Test for insert
      storageManager.addWorkflowEditorMetadata(editorMetadata);

      // Test for select
      WorkflowEditorMetadata added = storageManager.getWorkflowEditorMetadata(workflow.getId());
      Assert.assertEquals(added.getWorkflowId(), editorMetadata.getWorkflowId());
      Assert.assertEquals(added.getData(), editorMetadata.getData());

      // Test for update
      editorMetadata.setData("{\"abc\":\"xyz\"}");
      storageManager.addOrUpdateWorkflowEditorMetadata(workflow.getId(), editorMetadata);
      added = storageManager.getWorkflowEditorMetadata(workflow.getId());
      Assert.assertEquals(added.getWorkflowId(), editorMetadata.getWorkflowId());
      Assert.assertEquals(added.getData(), editorMetadata.getData());
    } finally {
      // Test for delete
      storageManager.removeWorkflowEditorMetadata(workflow.getId());
    }
  }

  @Test
  public void testWorkflowComponentBundle() {
    // Sample component bundle
    WorkflowComponentBundle dpfwSource = insertSampleSourceBundle();
    try {
      // Test select
      Collection<WorkflowComponentBundle> bundles = storageManager.listWorkflowComponentBundles();
      Assert.assertTrue(!bundles.isEmpty());
      bundles = storageManager.listWorkflowComponentBundles(WorkflowComponentBundleType.SOURCE);
      Assert.assertTrue(!bundles.isEmpty());
      WorkflowComponentBundle added = bundles.iterator().next();

      Assert.assertEquals(added.getName(), dpfwSource.getName());
      Assert.assertEquals(added.getWorkflowComponentUISpecification().getFields().size(),
          dpfwSource.getWorkflowComponentUISpecification().getFields().size());

      // Test update
      dpfwSource.setName("New name");
      storageManager.addOrUpdateWorkflowComponentBundle(dpfwSource);
      added = storageManager.listWorkflowComponentBundles().iterator().next();

      Assert.assertEquals(added.getName(), dpfwSource.getName());
      Assert.assertEquals(added.getWorkflowComponentUISpecification().getFields().size(),
          dpfwSource.getWorkflowComponentUISpecification().getFields().size());
    } finally {
      storageManager.removeWorkflowComponentBundle(dpfwSource.getId());
    }
  }

  private WorkflowComponentBundle insertSampleSourceBundle() {
    WorkflowComponentBundle dpfwSource = new WorkflowComponentBundle();
    dpfwSource.setName("DPFW-SOURCE");
    dpfwSource.setType(WorkflowComponentBundleType.SOURCE);
    dpfwSource.setStreamingEngine("FLINK");
    dpfwSource.setSubType("DPFW");
    dpfwSource.setBundleJar("a");
    dpfwSource.setTransformationClass("a");

    ComponentUISpecification componentUISpecification = new ComponentUISpecification();
    addUIField(componentUISpecification, "Data Type", "dataType", "Enter data type");
    addUIField(componentUISpecification, "Data Source", "dataSource", "Enter data source");
    dpfwSource.setWorkflowComponentUISpecification(componentUISpecification);

    dpfwSource.setTransformationClass("a");
    dpfwSource.setBuiltin(true);

    // Test insert
    WorkflowComponentBundle bundle = storageManager
        .addWorkflowComponentBundle(dpfwSource);
    Assert.assertNotNull(bundle.getId());

    return bundle;
  }

  @Test
  public void testWorkflowComponent() throws Exception {
    Workflow workflow = insertSampleWorkflow();
    WorkflowComponentBundle source = insertSampleSourceBundle();
    WorkflowStream stream = new WorkflowStream();
    WorkflowComponent component = new WorkflowSource();
    try {
      component.setName("SourceComponent");
      component.setWorkflowComponentBundleId(source.getId());
      component.setWorkflowId(workflow.getId());
      component.addConfig("dataSource", "EZMQ");

      // insert
      component = storageManager.addWorkflowComponent(workflow.getId(), component);

      // select
      Collection<WorkflowComponent> components = storageManager
          .listWorkflowComponents(workflow.getId());
      Assert.assertTrue(components.size() == 1);
      WorkflowComponent added = components.iterator().next();
      Assert.assertEquals(added.getName(), component.getName());
      Assert.assertEquals(added.getConfig("dataSource").toString(),
          component.getConfig("dataSource").toString());

      // update
      // add stream
      stream.setWorkflowId(workflow.getId());
      stream.setComponentId(component.getId());
      stream.setStreamId("streamA");
      Field field = new Field();
      field.setName("fieldName");
      field.setOptional(false);
      field.setType(SchemaType.STRING);
      stream.addField(field);
      stream = storageManager.addWorkflowStream(stream);
      ((WorkflowSource) component).addOutputStream(stream);
      component.setName("SourceUpdated");
      component = storageManager
          .addOrUpdateWorkflowComponent(workflow.getId(), component.getId(), component);

      // select
      added = storageManager.listWorkflowComponents(workflow.getId()).iterator().next();
      Assert.assertTrue(added instanceof WorkflowSource);
      Assert.assertEquals(((WorkflowSource) added).getOutputStreams().size(),
          ((WorkflowSource) component).getOutputStreams().size());

    } finally {
      if (stream.getId() != null) {
        storageManager.removeWorkflowStream(workflow.getId(), stream.getId());
      }
      if (component.getId() != null) {
        storageManager.removeWorkflowComponent(workflow.getId(), component.getId());
      }
      storageManager.removeWorkflowComponentBundle(source.getId());
      storageManager.removeWorkflow(workflow.getId());
    }
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

  @AfterClass
  public static void cleanup() throws Exception {
    if (sqliteFile.exists()) {
      sqliteFile.delete();
    }
  }
}

package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.util.Collection;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.Workflow;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponent;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.ComponentUISpecification;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.UIField;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.WorkflowComponentBundleType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEdge;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEditorMetadata;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEditorToolbar;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSink;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSource;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowStream;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowStream.Field;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowStream.SchemaType;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam.UiFieldType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class WorkflowTableManagerTest extends DatabaseTest {

  private static WorkflowTableManager workflowTable;

  @BeforeClass
  public static void setup() throws Exception {
    workflowTable = WorkflowTableManager.getInstance();
    java.lang.reflect.Field databaseField = AbstractStorageManager.class
        .getDeclaredField("database");
    databaseField.setAccessible(true);
    databaseField.set(workflowTable,
        DatabaseManager.getInstance().getDatabase("jdbc:sqlite:" + testDB.getAbsolutePath()));

    ResourceLoader loader = new DefaultResourceLoader(ClassLoader.getSystemClassLoader());
    Resource resource = loader.getResource("db/sqlite/create_tables.sql");
    workflowTable.executeSqlScript(resource);
  }

  @Test
  public void testSelectInvalidWorkflow() throws Exception {
    Workflow workflow = workflowTable.getWorkflow(-1L);
    Assert.assertNull(workflow);
  }

  @Test
  public void testListWorkflows() throws Exception {
    Collection<Workflow> workflows = workflowTable.listWorkflows();
    Assert.assertTrue(workflows != null); // may not be empty due to concurrent tests
  }

  @Test
  public void testAddWorkflow() throws Exception {
    Workflow workflow = insertSampleWorkflow();

    Long workflowId = workflow.getId();
    try {
      Workflow dbWorkflow = workflowTable.getWorkflow(workflowId);
      Assert.assertEquals(dbWorkflow.getName(), workflow.getName());
      Assert.assertEquals(dbWorkflow.getConfig("targetHost").toString(),
          workflow.getConfig("targetHost").toString());

      Assert.assertTrue(workflowTable.listWorkflows().size() > 0);
    } finally {
      workflowTable.removeWorkflow(workflowId); // delete test workflow
    }
  }

  @Test
  public void testUpdateWorkflow() throws Exception {
    Workflow workflow = insertSampleWorkflow();

    Long workflowId = workflow.getId();
    try {
      workflow.addConfig("payload", "sample");
      workflow.addConfig("targetHost", "192.168.0.3");
      Workflow dbWorkflow = workflowTable.addOrUpdateWorkflow(workflowId, workflow);

      Assert.assertEquals(dbWorkflow.getName(), workflow.getName());
      Assert.assertEquals(dbWorkflow.getConfig("targetHost").toString(),
          workflow.getConfig("targetHost").toString());
      Assert.assertEquals(dbWorkflow.getConfig("payload").toString(),
          workflow.getConfig("payload").toString());
    } finally {
      workflowTable.removeWorkflow(workflowId); // delete test workflow
    }
  }

  private Workflow insertSampleWorkflow() {
    Workflow workflow = new Workflow();
    workflow.setId(null);
    workflow.setName("FirstWorkflow");
    workflow.addConfig("targetHost", "192.168.0.1");
    workflow = workflowTable.addWorkflow(workflow);
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
      workflowTable.addWorkflowEditorMetadata(editorMetadata);

      // Test for select
      WorkflowEditorMetadata added = workflowTable.getWorkflowEditorMetadata(workflow.getId());
      Assert.assertEquals(added.getWorkflowId(), editorMetadata.getWorkflowId());
      Assert.assertEquals(added.getData(), editorMetadata.getData());

      // Test for update
      editorMetadata.setData("{\"abc\":\"xyz\"}");
      workflowTable.addOrUpdateWorkflowEditorMetadata(workflow.getId(), editorMetadata);
      added = workflowTable.getWorkflowEditorMetadata(workflow.getId());
      Assert.assertEquals(added.getWorkflowId(), editorMetadata.getWorkflowId());
      Assert.assertEquals(added.getData(), editorMetadata.getData());
    } finally {
      // Test for delete
      workflowTable.removeWorkflowEditorMetadata(workflow.getId());
    }
  }

  @Test
  public void testWorkflowComponentBundle() {
    // Sample component bundle
    WorkflowComponentBundle dpfwSource = insertSampleSourceBundle();
    try {
      // Test select
      Collection<WorkflowComponentBundle> bundles = workflowTable.listWorkflowComponentBundles();
      Assert.assertTrue(!bundles.isEmpty());
      bundles = workflowTable.listWorkflowComponentBundles(WorkflowComponentBundleType.SOURCE);
      Assert.assertTrue(!bundles.isEmpty());
      WorkflowComponentBundle added = bundles.iterator().next();

      Assert.assertEquals(added.getName(), dpfwSource.getName());
      Assert.assertEquals(added.getWorkflowComponentUISpecification().getFields().size(),
          dpfwSource.getWorkflowComponentUISpecification().getFields().size());

      // Test update
      dpfwSource.setName("New name");
      workflowTable.addOrUpdateWorkflowComponentBundle(dpfwSource);
      added = workflowTable.getWorkflowComponentBundle(dpfwSource.getName(), dpfwSource.getType(),
          dpfwSource.getSubType());

      Assert.assertEquals(added.getName(), dpfwSource.getName());
      Assert.assertEquals(added.getWorkflowComponentUISpecification().getFields().size(),
          dpfwSource.getWorkflowComponentUISpecification().getFields().size());

      // Test get
      WorkflowComponentBundle bundle = workflowTable
          .getWorkflowComponentBundle(dpfwSource.getName(), dpfwSource.getType(),
              dpfwSource.getSubType());
      Assert.assertNotNull(bundle);
      Assert.assertEquals(dpfwSource.getName(), bundle.getName());
    } finally {
      workflowTable.removeWorkflowComponentBundle(dpfwSource.getId());
    }
  }

  @Test
  public void testWorkflowStream() {
    WorkflowStream stream = new WorkflowStream();
    stream.setStreamId("test-stream");
    stream.setComponentId(1L);
    stream.setWorkflowId(1L);
    stream.setDescription("");
    stream = workflowTable.addWorkflowStream(stream);

    Assert.assertNotNull(stream.getId());
    try {
      Collection<WorkflowStream> streams = workflowTable.listWorkflowStreams(1L);
      Assert.assertEquals(1L, streams.size());

      stream.setStreamId("test-update-stream");
      workflowTable.addOrUpdateWorkflowStream(1L, stream);

      WorkflowStream selected = workflowTable.getWorkflowStream(1L, stream.getId());
      Assert.assertNotNull(selected);
      Assert.assertEquals(selected.getStreamId(), stream.getStreamId());
    } finally {
      workflowTable.removeWorkflowStream(1L, stream.getId());
    }
  }

  @Test
  public void testWorkflowEdge() {
    WorkflowEdge edge = new WorkflowEdge();
    edge.setWorkflowId(1L);
    edge.setToId(1L);
    edge.setFromId(2L);
    edge.setStreamGroupingsStr("[]");

    edge = workflowTable.addWorkflowEdge(1L, edge);
    Assert.assertNotNull(edge.getId());

    try {
      Collection<WorkflowEdge> edges = workflowTable.listWorkflowEdges(1L);
      Assert.assertTrue(edges.size() > 0);

      edge.setToId(5L);
      workflowTable.addOrUpdateWorkflowEdge(1L, edge.getId(), edge);

      WorkflowEdge selected = workflowTable.getWorkflowEdge(1L, edge.getId());
      Assert.assertEquals(edge.getToId(), selected.getToId());

    } finally {
      workflowTable.removeWorkflowEdge(1L, edge.getId());
    }
  }

  private WorkflowComponentBundle insertSampleProcessorBundle() {
    WorkflowComponentBundle dpfwProcessor = new WorkflowComponentBundle();
    dpfwProcessor.setName("DPFW-PROCESSOR");
    dpfwProcessor.setType(WorkflowComponentBundleType.PROCESSOR);
    dpfwProcessor.setStreamingEngine("FLINK");
    dpfwProcessor.setSubType("DPFW");
    dpfwProcessor.setBundleJar("a");
    dpfwProcessor.setTransformationClass("a");

    ComponentUISpecification componentUISpecification = new ComponentUISpecification();
    addUIField(componentUISpecification, "Data Type", "dataType", "Enter data type");
    addUIField(componentUISpecification, "Data Source", "dataSource", "Enter data source");
    dpfwProcessor.setWorkflowComponentUISpecification(componentUISpecification);

    dpfwProcessor.setTransformationClass("a");
    dpfwProcessor.setBuiltin(true);

    // Test insert
    WorkflowComponentBundle bundle = workflowTable
        .addWorkflowComponentBundle(dpfwProcessor);
    Assert.assertNotNull(bundle.getId());

    return bundle;
  }

  private WorkflowComponentBundle insertSampleSinkBundle() {
    WorkflowComponentBundle dpfwSink = new WorkflowComponentBundle();
    dpfwSink.setName("DPFW-SINK");
    dpfwSink.setType(WorkflowComponentBundleType.SINK);
    dpfwSink.setStreamingEngine("FLINK");
    dpfwSink.setSubType("DPFW");
    dpfwSink.setBundleJar("a");
    dpfwSink.setTransformationClass("a");

    ComponentUISpecification componentUISpecification = new ComponentUISpecification();
    addUIField(componentUISpecification, "Data Type", "dataType", "Enter data type");
    addUIField(componentUISpecification, "Data Source", "dataSource", "Enter data source");
    dpfwSink.setWorkflowComponentUISpecification(componentUISpecification);

    dpfwSink.setTransformationClass("a");
    dpfwSink.setBuiltin(true);

    // Test insert
    WorkflowComponentBundle bundle = workflowTable
        .addWorkflowComponentBundle(dpfwSink);
    Assert.assertNotNull(bundle.getId());

    return bundle;
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
    WorkflowComponentBundle bundle = workflowTable
        .addWorkflowComponentBundle(dpfwSource);
    Assert.assertNotNull(bundle.getId());

    return bundle;
  }

  @Test
  public void testListComponents() {
    Assert.assertNotNull(workflowTable.listWorkflows());
    Assert.assertNotNull(workflowTable.listSources(1L));
    Assert.assertNotNull(workflowTable.listSinks(1L));
    Assert.assertNotNull(workflowTable.listProcessors(1L));
  }

  @Deprecated
  @Test
  public void testWorkflowEditorToolbar() {
    WorkflowComponentBundle sourceBundle = insertSampleSourceBundle();
    WorkflowComponentBundle sinkBundle = insertSampleSinkBundle();
    WorkflowComponentBundle processorBundle = insertSampleProcessorBundle();

    try {
      WorkflowEditorToolbar toolbar = workflowTable.getWorkflowEditorToolbar();
      Assert.assertNotNull(toolbar);

      workflowTable.addOrUpdateWorkflowEditorToolbar(toolbar);
    } finally {
      workflowTable.removeWorkflowComponentBundle(sourceBundle.getId());
      workflowTable.removeWorkflowComponentBundle(sinkBundle.getId());
      workflowTable.removeWorkflowComponentBundle(processorBundle.getId());
    }
  }

  @Test
  public void testWorkflowExportAndImport() {
    Workflow imported = null;
    Workflow workflow = insertSampleWorkflow();
    WorkflowEditorMetadata metadata = new WorkflowEditorMetadata();
    WorkflowComponentBundle sourceBundle = insertSampleSourceBundle();
    WorkflowComponentBundle sinkBundle = insertSampleSinkBundle();
    WorkflowComponentBundle processorBundle = insertSampleProcessorBundle();

    // add component
    WorkflowSource source = new WorkflowSource();
    source.setWorkflowId(workflow.getId());
    source.setName("SourceComponent");
    source.setWorkflowComponentBundleId(sourceBundle.getId());
    source.setWorkflowId(workflow.getId());
    source.addConfig("dataSource", "EZMQ");
    source = workflowTable.addWorkflowComponent(workflow.getId(), source);

    WorkflowStream stream = new WorkflowStream();
    stream.setStreamId("stream-a");
    stream.setDescription("");
    stream.setWorkflowId(workflow.getId());
    stream.setComponentId(source.getId());
    stream = workflowTable.addWorkflowStream(stream);
    source.addOutputStream(stream);
    source = workflowTable.addOrUpdateWorkflowComponent(workflow.getId(), source.getId(), source);

    WorkflowProcessor processor = new WorkflowProcessor();
    processor.setWorkflowId(workflow.getId());
    processor.setName("ProcessorComponent");
    processor.setWorkflowComponentBundleId(processorBundle.getId());
    processor.setWorkflowId(workflow.getId());
    processor.addConfig("dataSource", "EZMQ");
    processor = workflowTable.addWorkflowComponent(workflow.getId(), processor);

    WorkflowSink sink = new WorkflowSink();
    sink.setWorkflowId(workflow.getId());
    sink.setName("SinkComponent");
    sink.setWorkflowComponentBundleId(sinkBundle.getId());
    sink.setWorkflowId(workflow.getId());
    sink.addConfig("dataSource", "EZMQ");
    sink = workflowTable.addWorkflowComponent(workflow.getId(), sink);

    WorkflowEdge edge = new WorkflowEdge();
    edge.setWorkflowId(workflow.getId());
    edge.setFromId(source.getId());
    edge.setToId(processor.getId());
    edge = workflowTable.addWorkflowEdge(workflow.getId(), edge);

    metadata.setWorkflowId(workflow.getId());
    metadata.setData("{}");
    metadata.setTimestamp(System.currentTimeMillis());
    try {
      workflowTable.addWorkflowEditorMetadata(metadata);
      WorkflowData workflowData = workflowTable.doExportWorkflow(workflow);
      Assert.assertNotNull(workflowData);

      String s = workflowTable.exportWorkflow(workflow);
      Assert.assertNotNull(s);

      imported = workflowTable.importWorkflow("imported", workflowData);
      Assert.assertTrue(imported.getId() != workflow.getId());

    } catch (Exception e) {
      Assert.fail(e.getMessage());
      e.printStackTrace();
    } finally {
      workflowTable.removeWorkflowComponent(workflow.getId(), source.getId());
      workflowTable.removeWorkflowComponent(workflow.getId(), processor.getId());
      workflowTable.removeWorkflowComponent(workflow.getId(), sink.getId());
      workflowTable.removeWorkflowEditorMetadata(metadata.getWorkflowId());
      workflowTable.removeWorkflow(workflow.getId()); // delete test workflow
      if (imported != null) {
        Collection<WorkflowComponent> importedComponents = workflowTable
            .listWorkflowComponents(imported.getId());
        for (WorkflowComponent component : importedComponents) {
          workflowTable.removeWorkflowComponent(imported.getId(), component.getId());
        }
        workflowTable.removeWorkflowEditorMetadata(imported.getId());
        workflowTable.removeWorkflow(imported.getId());
      }
      workflowTable.removeWorkflowComponentBundle(sourceBundle.getId());
      workflowTable.removeWorkflowComponentBundle(sinkBundle.getId());
      workflowTable.removeWorkflowComponentBundle(processorBundle.getId());
    }

    try {
      workflowTable.importWorkflow("imported", null);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }

  }

  @Test
  public void testWorkflowComponent() throws Exception {
    Workflow workflow = insertSampleWorkflow();
    WorkflowComponentBundle source = insertSampleSourceBundle();
    WorkflowStream stream = new WorkflowStream();
    WorkflowSource component = new WorkflowSource();
    try {
      component.setName("SourceComponent");
      component.setWorkflowComponentBundleId(source.getId());
      component.setWorkflowId(workflow.getId());
      component.addConfig("dataSource", "EZMQ");
      component.addOutputStream(stream);
      stream.setWorkflowId(workflow.getId());
      stream.setComponentId(component.getId());
      stream.setStreamId("streamA");

      // insert
      component = workflowTable.addWorkflowComponent(workflow.getId(), component);

      // select
      Collection<WorkflowComponent> components = workflowTable
          .listWorkflowComponents(workflow.getId());
      Assert.assertTrue(components.size() == 1);
      WorkflowComponent added = components.iterator().next();
      Assert.assertEquals(added.getName(), component.getName());
      Assert.assertEquals(added.getConfig("dataSource").toString(),
          component.getConfig("dataSource").toString());

      // update
      // update stream
      stream.setWorkflowId(workflow.getId());
      stream.setComponentId(component.getId());
      stream.setStreamId("streamB");
      Field field = new Field();
      field.setName("fieldName");
      field.setOptional(false);
      field.setType(SchemaType.STRING);
      stream.addField(field);
      stream = workflowTable.addWorkflowStream(stream);
      component.addOutputStream(stream);
      component.setName("SourceUpdated");
      component = workflowTable
          .addOrUpdateWorkflowComponent(workflow.getId(), component.getId(), component);

      // select
      added = workflowTable.listWorkflowComponents(workflow.getId()).iterator().next();
      Assert.assertTrue(added instanceof WorkflowSource);
      Assert.assertEquals(((WorkflowSource) added).getOutputStreams().size(),
          component.getOutputStreams().size());

    } finally {
      if (stream.getId() != null) {
        workflowTable.removeWorkflowStream(workflow.getId(), stream.getId());
      }
      if (component.getId() != null) {
        workflowTable.removeWorkflowComponent(workflow.getId(), component.getId());
      }
      workflowTable.removeWorkflowComponentBundle(source.getId());
      workflowTable.removeWorkflow(workflow.getId());
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
    field.setType(UiFieldType.STRING);
    componentUISpecification.addUIField(field);
  }

}

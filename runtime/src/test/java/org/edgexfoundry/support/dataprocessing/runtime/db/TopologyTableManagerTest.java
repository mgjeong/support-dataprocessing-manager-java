package org.edgexfoundry.support.dataprocessing.runtime.db;

import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.File;
import java.util.Collection;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.ComponentUISpecification;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.ComponentUISpecification.UIField.UIFieldType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Schema.Type;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Topology;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponent;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle.TopologyComponentType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEditorMetadata;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologySource;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyStream;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyStream.Field;
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
@PrepareForTest(TopologyTableManager.class)
public class TopologyTableManagerTest {

  private static TopologyTableManager storageManager = TopologyTableManager.getInstance();
  private static File sqliteFile = new File(Settings.DOCKER_PATH + Settings.DB_TEST_PATH);

  @BeforeClass
  public static void setup() throws Exception {
    if (sqliteFile.exists() && !sqliteFile.delete()) {
      throw new RuntimeException("Failed to clean " + sqliteFile.getPath());
    }

    storageManager = spy(TopologyTableManager.getInstance());
    when(storageManager, "getJdbcUrl")
        .thenReturn("jdbc:sqlite:" + sqliteFile.getPath());
    ResourceLoader loader = new DefaultResourceLoader(ClassLoader.getSystemClassLoader());
    Resource resource = loader.getResource("db/sqlite/create_tables.sql");
    storageManager.executeSqlScript(resource);
  }

  @Test
  public void testSelectInvalidTopology() throws Exception {
    Topology topology = storageManager.getTopology(-1L);
    Assert.assertNull(topology);
  }

  @Test
  public void testListTopologies() throws Exception {
    Collection<Topology> topologies = storageManager.listTopologies();
    Assert.assertTrue(topologies != null); // may not be empty due to concurrent tests
  }

  @Test
  public void testAddTopology() throws Exception {
    Topology topology = insertSampleTopology();

    Long topologyId = topology.getId();
    try {
      Topology dbTopology = storageManager.getTopology(topologyId);
      Assert.assertEquals(dbTopology.getName(), topology.getName());
      Assert.assertEquals(dbTopology.getConfig("targetHost").toString(),
          topology.getConfig("targetHost").toString());
    } finally {
      storageManager.removeTopology(topologyId); // delete test topology
    }
  }

  @Test
  public void testUpdateTopology() throws Exception {
    Topology topology = insertSampleTopology();

    Long topologyId = topology.getId();
    try {
      topology.addConfig("payload", "sample");
      topology.addConfig("targetHost", "192.168.0.3");
      Topology dbTopology = storageManager.updateTopology(topologyId, topology);

      Assert.assertEquals(dbTopology.getName(), topology.getName());
      Assert.assertEquals(dbTopology.getConfig("targetHost").toString(),
          topology.getConfig("targetHost").toString());
      Assert.assertEquals(dbTopology.getConfig("payload").toString(),
          topology.getConfig("payload").toString());
    } finally {
      storageManager.removeTopology(topologyId); // delete test topology
    }
  }

  private Topology insertSampleTopology() {
    Topology topology = new Topology();
    topology.setId(null);
    topology.setName("FirstTopology");
    topology.addConfig("targetHost", "192.168.0.1");
    topology = storageManager.addTopology(topology);
    Assert.assertTrue(topology.getId() != null); // added successfully
    return topology;
  }

  @Test
  public void testTopologyEditorMetadata() throws Exception {
    Topology topology = insertSampleTopology();

    try {
      TopologyEditorMetadata editorMetadata = new TopologyEditorMetadata();
      editorMetadata.setTopologyId(topology.getId());
      editorMetadata.setData("{}");

      // Test for insert
      storageManager.addTopologyEditorMetadata(editorMetadata);

      // Test for select
      TopologyEditorMetadata added = storageManager.getTopologyEditorMetadata(topology.getId());
      Assert.assertEquals(added.getTopologyId(), editorMetadata.getTopologyId());
      Assert.assertEquals(added.getData(), editorMetadata.getData());

      // Test for update
      editorMetadata.setData("{\"abc\":\"xyz\"}");
      storageManager.addOrUpdateTopologyEditorMetadata(topology.getId(), editorMetadata);
      added = storageManager.getTopologyEditorMetadata(topology.getId());
      Assert.assertEquals(added.getTopologyId(), editorMetadata.getTopologyId());
      Assert.assertEquals(added.getData(), editorMetadata.getData());
    } finally {
      // Test for delete
      storageManager.removeTopologyEditorMetadata(topology.getId());
    }
  }

  @Test
  public void testTopologyComponentBundle() {
    // Sample component bundle
    TopologyComponentBundle dpfwSource = insertSampleSourceBundle();
    try {
      // Test select
      Collection<TopologyComponentBundle> bundles = storageManager.listTopologyComponentBundles();
      Assert.assertTrue(!bundles.isEmpty());
      bundles = storageManager.listTopologyComponentBundles(TopologyComponentType.SOURCE);
      Assert.assertTrue(!bundles.isEmpty());
      TopologyComponentBundle added = bundles.iterator().next();

      Assert.assertEquals(added.getName(), dpfwSource.getName());
      Assert.assertEquals(added.getTopologyComponentUISpecification().getFields().size(),
          dpfwSource.getTopologyComponentUISpecification().getFields().size());

      // Test update
      dpfwSource.setName("New name");
      storageManager.addOrUpdateTopologyComponentBundle(dpfwSource);
      added = storageManager.listTopologyComponentBundles().iterator().next();

      Assert.assertEquals(added.getName(), dpfwSource.getName());
      Assert.assertEquals(added.getTopologyComponentUISpecification().getFields().size(),
          dpfwSource.getTopologyComponentUISpecification().getFields().size());
    } finally {
      storageManager.removeTopologyComponentBundle(dpfwSource.getId());
    }
  }

  private TopologyComponentBundle insertSampleSourceBundle() {
    TopologyComponentBundle dpfwSource = new TopologyComponentBundle();
    dpfwSource.setName("DPFW-SOURCE");
    dpfwSource.setType(TopologyComponentType.SOURCE);
    dpfwSource.setTimestamp(System.currentTimeMillis());
    dpfwSource.setStreamingEngine("STORM");
    dpfwSource.setSubType("DPFW");
    dpfwSource.setBundleJar("a");
    dpfwSource.setTransformationClass("a");

    ComponentUISpecification componentUISpecification = new ComponentUISpecification();
    addUIField(componentUISpecification, "Data Type", "dataType", "Enter data type");
    addUIField(componentUISpecification, "Data Source", "dataSource", "Enter data source");
    dpfwSource.setTopologyComponentUISpecification(componentUISpecification);

    dpfwSource.setFieldHintProviderClass("a");
    dpfwSource.setTransformationClass("a");
    dpfwSource.setBuiltin(true);
    dpfwSource.setMavenDeps(" ");

    // Test insert
    TopologyComponentBundle bundle = storageManager
        .addTopologyComponentBundle(dpfwSource);
    Assert.assertNotNull(bundle.getId());

    return bundle;
  }

  @Test
  public void testTopologyComponent() throws Exception {
    Topology topology = insertSampleTopology();
    TopologyComponentBundle source = insertSampleSourceBundle();
    TopologyStream stream = new TopologyStream();
    TopologyComponent component = new TopologySource();
    try {
      component.setUiName("SourceComponent");
      component.setTopologyComponentBundleId(source.getId());
      component.setTopologyId(topology.getId());
      component.addConfig("dataSource", "EZMQ");

      // insert
      component = storageManager.addTopologyComponent(topology.getId(), component);

      // select
      Collection<TopologyComponent> components = storageManager
          .listTopologyComponents(topology.getId());
      Assert.assertTrue(components.size() == 1);
      TopologyComponent added = components.iterator().next();
      Assert.assertEquals(added.getUiName(), component.getUiName());
      Assert.assertEquals(added.getConfig("dataSource").toString(),
          component.getConfig("dataSource").toString());

      // update
      // add stream
      stream.setTopologyId(topology.getId());
      stream.setComponentId(component.getId());
      stream.setStreamId("streamA");
      Field field = new Field();
      field.setName("fieldName");
      field.setOptional(false);
      field.setType(Type.STRING);
      stream.addField(field);
      stream = storageManager.addTopologyStream(stream);
      ((TopologySource) component).addOutputStream(stream);
      component.setUiName("SourceUpdated");
      component = storageManager
          .addOrUpdateTopologyComponent(topology.getId(), component.getId(), component);

      // select
      added = storageManager.listTopologyComponents(topology.getId()).iterator().next();
      Assert.assertTrue(added instanceof TopologySource);
      Assert.assertEquals(((TopologySource) added).getOutputStreams().size(),
          ((TopologySource) component).getOutputStreams().size());

    } finally {
      if (stream.getId() != null) {
        storageManager.removeTopologyStream(topology.getId(), stream.getId());
      }
      if (component.getId() != null) {
        storageManager.removeTopologyComponent(topology.getId(), component.getId());
      }
      storageManager.removeTopologyComponentBundle(source.getId());
      storageManager.removeTopology(topology.getId());
    }
  }

  private void addUIField(ComponentUISpecification componentUISpecification, String uiName,
      String fieldName, String tooltip) {
    ComponentUISpecification.UIField field = new ComponentUISpecification.UIField();
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
    storageManager.terminate();
    if (sqliteFile.exists()) {
      sqliteFile.delete();
    }
  }
}

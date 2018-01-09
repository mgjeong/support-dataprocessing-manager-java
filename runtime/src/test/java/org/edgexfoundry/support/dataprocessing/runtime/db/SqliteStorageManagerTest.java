package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.util.Collection;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.ComponentUISpecification;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Topology;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle.TopologyComponentType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEditorMetadata;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SqliteStorageManagerTest {

  private static SqliteStorageManager storageManager = new SqliteStorageManager();

  @BeforeClass
  public static void setup() throws Exception {
    storageManager.initialize();
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
      Assert.assertEquals(dbTopology.getConfig("targetHost"), topology.getConfig("targetHost"));
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
      Assert.assertEquals(dbTopology.getConfig("targetHost"), topology.getConfig("targetHost"));
      Assert.assertEquals(dbTopology.getConfig("payload"), topology.getConfig("payload"));
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
      storageManager.addOrUpdateTopologyEditorMetadata(editorMetadata);
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
    TopologyComponentBundle dpfwSource = new TopologyComponentBundle();
    dpfwSource.setName("DPFW-SOURCE");
    dpfwSource.setType(TopologyComponentBundle.TopologyComponentType.SOURCE);
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
      storageManager.removeTopologyComponentBundle(bundle.getId());
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
    field.setType("string");
    componentUISpecification.addUIField(field);
  }

  @AfterClass
  public static void cleanup() throws Exception {
    storageManager.terminate();
  }
}

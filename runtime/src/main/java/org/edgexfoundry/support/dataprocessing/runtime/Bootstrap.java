package org.edgexfoundry.support.dataprocessing.runtime;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.ComponentUISpecification;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.db.TopologyTableManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class Bootstrap {

  private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

  private final TopologyTableManager storageManager = TopologyTableManager.getInstance();

  public Bootstrap() {

  }

  private void addBuiltinTopologyComponentBundles() {
    addBuiltinTopologyTopologyComponentBundles();
    addBuiltinTopologySourceComponentBundles();
    addBuiltinTopologySinkComponentBundles();
  }

  private void addBuiltinTopologySinkComponentBundles() {
    TopologyComponentBundle dpfwSink = new TopologyComponentBundle();
    dpfwSink.setName("DPFW-SINK");
    dpfwSink.setType(TopologyComponentBundle.TopologyComponentType.SINK);
    dpfwSink.setTimestamp(System.currentTimeMillis());
    dpfwSink.setStreamingEngine("STORM");
    dpfwSink.setSubType("DPFW");
    dpfwSink.setBundleJar("");

    ComponentUISpecification componentUISpecification = new ComponentUISpecification();
    addUIField(componentUISpecification, "Data Type", "dataType", "Enter data type");
    addUIField(componentUISpecification, "Data Sink", "dataSink", "Enter data sink");
    dpfwSink.setTopologyComponentUISpecification(componentUISpecification);

    dpfwSink.setFieldHintProviderClass("");
    dpfwSink.setTransformationClass("");
    dpfwSink.setBuiltin(true);
    dpfwSink.setMavenDeps("");

    TopologyComponentBundle existingBundle =
        storageManager.getTopologyComponentBundle(dpfwSink.getName(), dpfwSink.getType(),
            dpfwSink.getSubType());
    if (existingBundle == null) {
      dpfwSink = storageManager.addTopologyComponentBundle(dpfwSink);
    } else {
      dpfwSink.setId(existingBundle.getId());
      dpfwSink = storageManager.addOrUpdateTopologyComponentBundle(dpfwSink);
    }
    LOGGER.info("Sink id={}/name={} added.", dpfwSink.getId(), dpfwSink.getName());
  }

  private void addBuiltinTopologySourceComponentBundles() {
    TopologyComponentBundle dpfwSource = new TopologyComponentBundle();
    dpfwSource.setName("DPFW-SOURCE");
    dpfwSource.setType(TopologyComponentBundle.TopologyComponentType.SOURCE);
    dpfwSource.setTimestamp(System.currentTimeMillis());
    dpfwSource.setStreamingEngine("STORM");
    dpfwSource.setSubType("DPFW");
    dpfwSource.setBundleJar("");
    dpfwSource.setTransformationClass("");

    ComponentUISpecification componentUISpecification = new ComponentUISpecification();
    addUIField(componentUISpecification, "Data Type", "dataType", "Enter data type");
    addUIField(componentUISpecification, "Data Source", "dataSource", "Enter data source");
    dpfwSource.setTopologyComponentUISpecification(componentUISpecification);

    dpfwSource.setFieldHintProviderClass("");
    dpfwSource.setTransformationClass("");
    dpfwSource.setBuiltin(true);
    dpfwSource.setMavenDeps("");

    TopologyComponentBundle existingBundle =
        storageManager.getTopologyComponentBundle(dpfwSource.getName(), dpfwSource.getType(),
            dpfwSource.getSubType());
    if (existingBundle == null) {
      dpfwSource = storageManager.addTopologyComponentBundle(dpfwSource);
    } else {
      dpfwSource.setId(existingBundle.getId());
      dpfwSource = storageManager.addOrUpdateTopologyComponentBundle(dpfwSource);
    }
    LOGGER.info("Source id={}/name={} added.", dpfwSource.getId(), dpfwSource.getName());
  }

  private void addBuiltinTopologyTopologyComponentBundles() {
    TopologyComponentBundle runtimeTopology = new TopologyComponentBundle();
    runtimeTopology.setName("Runtime topology");
    runtimeTopology.setType(TopologyComponentBundle.TopologyComponentType.TOPOLOGY);
    runtimeTopology.setTimestamp(System.currentTimeMillis());
    runtimeTopology.setStreamingEngine("STORM");
    runtimeTopology.setSubType("TOPOLOGY");
    runtimeTopology.setBundleJar("");

    ComponentUISpecification componentUISpecification = new ComponentUISpecification();
    ComponentUISpecification.UIField runtimeHost = new ComponentUISpecification.UIField();
    runtimeHost.setUiName("Runtime host");
    runtimeHost.setFieldName("runtimeHost");
    runtimeHost.setUserInput(true);
    runtimeHost.setTooltip("Enter hostname of runtime edge.");
    runtimeHost.setOptional(false);
    runtimeHost.setType("string");
    runtimeHost.setDefaultValue("localhost:8082");
    componentUISpecification.addUIField(runtimeHost);
    ComponentUISpecification.UIField targetHost = new ComponentUISpecification.UIField();
    targetHost.setUiName("Target host");
    targetHost.setFieldName("targetHost");
    targetHost.setUserInput(true);
    targetHost.setTooltip("Enter hostname of target edge.");
    targetHost.setOptional(false);
    targetHost.setType("string");
    targetHost.setDefaultValue("localhost:9092");
    componentUISpecification.addUIField(targetHost);
    runtimeTopology.setTopologyComponentUISpecification(componentUISpecification);

    runtimeTopology.setFieldHintProviderClass("");
    runtimeTopology.setTransformationClass("dummy");
    runtimeTopology.setBuiltin(true);
    runtimeTopology.setMavenDeps("");

    TopologyComponentBundle existingBundle =
        storageManager
            .getTopologyComponentBundle(runtimeTopology.getName(), runtimeTopology.getType(),
                runtimeTopology.getSubType());
    if (existingBundle == null) {
      runtimeTopology = storageManager.addTopologyComponentBundle(runtimeTopology);
    } else {
      runtimeTopology.setId(existingBundle.getId());
      runtimeTopology = storageManager.addOrUpdateTopologyComponentBundle(runtimeTopology);
    }
    LOGGER.info("Topology id={}/name={} added.",
        runtimeTopology.getId(), runtimeTopology.getName());
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

  public void execute() throws Exception {
    createTablesIfNotExist();

    addBuiltinTopologyComponentBundles();
  }

  private void createTablesIfNotExist() {
    ResourceLoader resourceLoader = new DefaultResourceLoader(getClass().getClassLoader());
    Resource resource = resourceLoader.getResource("db/sqlite/create_tables.sql");
    storageManager.executeSqlScript(resource);
  }

  public void terminate() {
    storageManager.terminate();
  }

  public static void main(String[] args) throws Exception {
    LOGGER.info("Starting bootstrap...");

    final Bootstrap bootstrap = new Bootstrap();

    // Shutdown hook
    Runtime.getRuntime().addShutdownHook(new Thread(() -> bootstrap.terminate()));

    bootstrap.execute();
  }
}

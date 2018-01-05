package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

public class TopologyDag {
  private final Set<OutputComponent> outputComponents = new LinkedHashSet<>();
  private final Set<InputComponent> inputComponents = new LinkedHashSet<>();

  private final Map<OutputComponent, List<Edge>> dag = new LinkedHashMap<>();
}

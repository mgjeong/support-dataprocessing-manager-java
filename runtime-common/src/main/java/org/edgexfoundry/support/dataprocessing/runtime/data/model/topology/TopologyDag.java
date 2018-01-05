package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Stream.Grouping;

public class TopologyDag {

  private final Set<OutputComponent> outputComponents = new LinkedHashSet<>();
  private final Set<InputComponent> inputComponents = new LinkedHashSet<>();

  private final Map<OutputComponent, List<Edge>> dag = new LinkedHashMap<>();

  public TopologyDag add(Component component) {
    if (component instanceof OutputComponent) {
      this.outputComponents.add((OutputComponent) component);
    }
    if (component instanceof InputComponent) {
      this.inputComponents.add((InputComponent) component);
    }
    return this;
  }

  public Set<OutputComponent> getOutputComponents() {
    return outputComponents;
  }

  public Set<InputComponent> getInputComponents() {
    return inputComponents;
  }

  public void addEdge(OutputComponent from, InputComponent to) {
    addEdge(UUID.randomUUID().toString(), from, to, getDefaultStreamId(from));
  }

  // specify stream, shuffle grouping
  public void addEdge(String id, OutputComponent from, InputComponent to, String streamId) {
    addEdge(id, from, to, streamId, Stream.Grouping.SHUFFLE);
  }

  // specify stream and grouping
  public void addEdge(String id, OutputComponent from, InputComponent to, String streamId,
      Stream.Grouping grouping) {
    addEdge(id, from, to, new StreamGrouping(from.getOutputStream(streamId), grouping));
  }

  public void addEdge(Edge edge) {
    for (StreamGrouping streamGrouping : edge.getStreamGroupings()) {
      addEdge(edge.getId(), edge.getFrom(), edge.getTo(), streamGrouping);
    }
  }

  // specify stream grouping
  public void addEdge(String id, OutputComponent from, InputComponent to,
      StreamGrouping streamGrouping) {
    ensureValid(from, to);
    doAddEdge(id, from, to, streamGrouping);
  }

  public void removeEdge(OutputComponent from, InputComponent to) {
    Iterator<Edge> it = dag.get(from).iterator();
    while (it.hasNext()) {
      if (it.next().getTo().equals(to)) {
        it.remove();
      }
    }
  }

  private void ensureValid(OutputComponent from, InputComponent to) {
    if (!outputComponents.contains(from)) {
      throw new IllegalArgumentException("Invalid from");
    } else if (!inputComponents.contains(to)) {
      throw new IllegalArgumentException("Invalid to");
    }
  }

  private void doAddEdge(String id, OutputComponent from, InputComponent to,
      StreamGrouping streamGrouping) {
    List<Edge> edges = dag.get(from);
    if (edges == null) {
      edges = new ArrayList<>();
      dag.put(from, edges);
    }
    // output component is already connected to input component, just add the stream grouping
    for (Edge e : edges) {
      if (e.getTo().equals(to)) {
        e.addStreamGrouping(streamGrouping);
        return;
      }
    }
    edges.add(new Edge(id, from, to, streamGrouping));
  }

  private String getDefaultStreamId(OutputComponent source) {
    return source.getOutputStreams().iterator().next().getId();
  }

  public interface Component {

    String getId();

    String getName();

    Config getConfig();
  }

  public interface OutputComponent extends Component {

    Set<Stream> getOutputStreams();

    Stream getOutputStream(String streamId);
  }

  public interface InputComponent extends Component {

  }

  public static class Edge {

    private String id;
    private OutputComponent from;
    private InputComponent to;
    private final Set<StreamGrouping> streamGroupings;

    public Edge() {
      this(null, null, null, Collections.<StreamGrouping>emptySet());
    }

    public Edge(String id, OutputComponent from, InputComponent to, String streamId,
        Stream.Grouping grouping) {
      this(id, from, to, new StreamGrouping(from.getOutputStream(streamId), grouping));
    }

    public Edge(String id, OutputComponent from, InputComponent to, StreamGrouping streamGrouping) {
      this(id, from, to, Collections.singleton(streamGrouping));
    }

    public Edge(String id, OutputComponent from, InputComponent to,
        Set<StreamGrouping> streamGroupings) {
      this.id = id;
      this.from = from;
      this.to = to;
      this.streamGroupings = new HashSet<>(streamGroupings);
    }

    public Set<StreamGrouping> getStreamGroupings() {
      return streamGroupings;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public OutputComponent getFrom() {
      return from;
    }

    public void setFrom(
        OutputComponent from) {
      this.from = from;
    }

    public InputComponent getTo() {
      return to;
    }

    public void setTo(
        InputComponent to) {
      this.to = to;
    }

    public void addStreamGrouping(StreamGrouping streamGrouping) {
      this.streamGroupings.add(streamGrouping);
    }

    public void addStreamGroupings(Set<StreamGrouping> streamGroupings) {
      this.streamGroupings.addAll(streamGroupings);
    }
  }

  public static class StreamGrouping {

    private final Stream stream;
    private final Stream.Grouping grouping;
    private final List<String> fields;

    public StreamGrouping(Stream stream, Stream.Grouping grouping) {
      this(stream, grouping, null);
    }

    public StreamGrouping(Stream stream, Stream.Grouping grouping, List<String> fields) {
      this.stream = stream;
      this.grouping = grouping;
      this.fields = fields;
    }

    public Stream getStream() {
      return stream;
    }

    public Grouping getGrouping() {
      return grouping;
    }

    public List<String> getFields() {
      return fields;
    }
  }

  public static abstract class AbstractComponent implements Component {

    private String id;
    private String topologyComponentBundleId;
    private String topologyComponentBundleName;
    private String name;
    private Config config;

    public void setId(String id) {
      this.id = id;
    }

    public String getTopologyComponentBundleId() {
      return topologyComponentBundleId;
    }

    public void setTopologyComponentBundleId(String topologyComponentBundleId) {
      this.topologyComponentBundleId = topologyComponentBundleId;
    }

    public String getTopologyComponentBundleName() {
      return topologyComponentBundleName;
    }

    public void setTopologyComponentBundleName(String topologyComponentBundleName) {
      this.topologyComponentBundleName = topologyComponentBundleName;
    }

    public void setName(String name) {
      this.name = name;
    }

    public void setConfig(Config config) {
      this.config = config;
    }

    @Override
    public String getId() {
      return this.id;
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public Config getConfig() {
      return this.config;
    }
  }

  public static class SourceComponent extends AbstractComponent implements OutputComponent {

    private final Set<Stream> outputStreams = new HashSet<>();

    public SourceComponent() {
      this(Collections.EMPTY_SET);
    }

    public SourceComponent(Set<Stream> outputStreams) {
      addOutputStreams(outputStreams);
    }

    @Override
    public Set<Stream> getOutputStreams() {
      return this.outputStreams;
    }

    @Override
    public Stream getOutputStream(String streamId) {
      for (Stream stream : this.getOutputStreams()) {
        if (stream.getId().equals(streamId)) {
          return stream;
        }
      }
      throw new IllegalArgumentException("Invalid streamId " + streamId);
    }

    public void addOutputStreams(Set<Stream> streams) {
      outputStreams.addAll(streams);
    }
  }

  public static class SinkComponent extends AbstractComponent implements InputComponent {

  }

  public static class ProcessorComponent extends AbstractComponent implements InputComponent,
      OutputComponent {

    private final Set<Stream> outputStreams = new HashSet<>();

    public ProcessorComponent() {
      this(Collections.EMPTY_SET);
    }

    public ProcessorComponent(Set<Stream> outputStreams) {
      addOutputStreams(outputStreams);
    }

    @Override
    public Set<Stream> getOutputStreams() {
      return this.outputStreams;
    }

    @Override
    public Stream getOutputStream(String streamId) {
      for (Stream stream : this.getOutputStreams()) {
        if (stream.getId().equals(streamId)) {
          return stream;
        }
      }
      throw new IllegalArgumentException("Invalid streamId " + streamId);
    }

    public void addOutputStreams(Set<Stream> streams) {
      outputStreams.addAll(streams);
    }
  }

}

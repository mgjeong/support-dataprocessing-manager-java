package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.vertex;

import static org.mockito.ArgumentMatchers.any;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class FlatMapTaskVertexTest {

  @Test
  public void testGetId() {
    WorkflowProcessor processor = new WorkflowProcessor();
    processor.setId(1L);
    FlatMapTaskVertex vertex = new FlatMapTaskVertex(processor);
    Assert.assertEquals(vertex.getId(), (Long) 1L);
  }

  @Test(expected = NullPointerException.class)
  public void testServeWithNullInflux() {
    WorkflowProcessor processor = new WorkflowProcessor();
    Map<String, Object> properties = Collections.emptyMap();
    processor.getConfig().setProperties(properties);

    FlatMapTaskVertex vertex = new FlatMapTaskVertex(processor);

    vertex.serve();
  }

  @Test(expected = NullPointerException.class)
  public void testServeWithNullTaskInfo() {
    WorkflowProcessor processor = null;

    FlatMapTaskVertex vertex = new FlatMapTaskVertex(processor);

    vertex.serve();
  }

  @Test
  public void testServe() {
    WorkflowProcessor processor = new WorkflowProcessor();
    Map<String, Object> properties = Collections.emptyMap();
    processor.getConfig().setProperties(properties);

    DataStream dataStream = Mockito.mock(DataStream.class);
    Mockito.when(dataStream.flatMap(any())).thenReturn(null);

    FlatMapTaskVertex vertex = new FlatMapTaskVertex(processor);
    vertex.setInflux(dataStream);
    vertex.serve();
  }

  @Test
  public void testSetInflux() throws Exception {
    FlatMapTaskVertex vertex = new FlatMapTaskVertex(null);
    DataStream firstStream = Mockito.mock(DataStream.class);
    Mockito.when(firstStream.union()).thenReturn(firstStream);
    DataStream secondStream = Mockito.mock(DataStream.class);

    vertex.setInflux(firstStream);
    vertex.setInflux(firstStream);
    Field f = FlatMapTaskVertex.class.getDeclaredField("influx");
    f.setAccessible(true);
    Assert.assertEquals(firstStream, f.get(vertex));
    vertex.setInflux(secondStream);
  }
}

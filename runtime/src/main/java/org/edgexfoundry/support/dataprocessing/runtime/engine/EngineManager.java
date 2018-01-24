package org.edgexfoundry.support.dataprocessing.runtime.engine;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EngineManager {

  private static Semaphore semaphore = new Semaphore(1);

  private static final Logger LOGGER = LoggerFactory.getLogger(EngineManager.class);
  //  HashMap<IP, Engine>
  private static HashMap<String, Engine> engines = new HashMap<String, Engine>();


  public static Engine getEngine(String host, WorkflowData.EngineType engineType) {
    Engine engine = engines.get(host);

    if (null == engine) {
      engine = EngineManager.createEngine(engineType, host);
    }

    return engine;
  }


  private static Engine createEngine(WorkflowData.EngineType engineType, String host) {

    Engine engine = null;

    try {
      String ip = host.substring(0, host.indexOf(":"));
      int port = Integer.parseInt(host.substring(host.indexOf(":") + 1, host.length()));

      engine = EngineFactory.createEngine(engineType, ip, port);
      semaphore.acquire();
      engines.put(host, engine);
    } catch (NumberFormatException e) {
      LOGGER.error(e.getMessage(), e);
      engine = null;
    } catch (InterruptedException e) {
      LOGGER.error(e.getMessage(), e);
      engine = null;
    } finally {
      semaphore.release();
    }

    return engine;
  }

  public static HashMap<String, Engine> getEngineList() {
    HashMap<String, Engine> engineList = null;
    try {
      semaphore.acquire();
      engineList = (HashMap<String, Engine>) engines.clone();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      semaphore.release();
    }

    return engineList;
  }
}

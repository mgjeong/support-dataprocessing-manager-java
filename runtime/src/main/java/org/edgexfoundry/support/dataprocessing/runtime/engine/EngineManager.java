package org.edgexfoundry.support.dataprocessing.runtime.engine;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EngineManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(EngineManager.class);
  //  HashMap<IP, Engine>
  private static HashMap<String, Engine> engines = new HashMap<String, Engine>();


  public static Engine getEngine(String host, EngineType engineType) {
    Engine engine = engines.get(host);

    if (null == engine) {
      engine = EngineManager.createEngine(engineType, host);
    }

    return engine;
  }

  private static Engine createEngine(EngineType engineType, String host) {

    Engine engine = null;

    try {
      String ip = host.substring(0, host.indexOf(":"));
      int port = Integer.parseInt(host.substring(host.indexOf(":") + 1, host.length()));

      engine = EngineFactory.createEngine(engineType, ip, port);
      engines.put(host, engine);
    } catch (NumberFormatException e) {
      LOGGER.error(e.getMessage(), e);
      engine = null;
    }

    return engine;
  }
}

-- -----------------------------------------------------
-- Table `topology`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `topology` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `name` TEXT NOT NULL,
  `config` TEXT NOT NULL
);

-- -----------------------------------------------------
-- Table `topology_component_bundle`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `topology_component_bundle` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `name` TEXT NOT NULL,
  `type` TEXT NOT NULL,
  `subType` TEXT NOT NULL,
  `streamingEngine` TEXT NOT NULL,
  `path` TEXT NOT NULL,
  `classname` TEXT NOT NULL,
  `param` TEXT NOT NULL,
  `componentUISpecification` TEXT NOT NULL,
  `removable` CHAR(1) NOT NULL DEFAULT '0'
);

-- -----------------------------------------------------
-- Table `topology_component`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `topology_component` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `topologyId` INTEGER NOT NULL,
  `componentBundleId` INTEGER NOT NULL,
  `name` TEXT NOT NULL,
  `config` TEXT NOT NULL,
  UNIQUE (`id`, `topologyId`),
  FOREIGN KEY (`topologyId`) REFERENCES `topology` (`id`),
  FOREIGN KEY (`componentBundleId`) REFERENCES `topology_component_bundle` (`id`)
);

-- -----------------------------------------------------
-- Table `topology_editor_metadata`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `topology_editor_metadata` (
  `topologyId` INTEGER NOT NULL,
  `data` TEXT NOT NULL,
  PRIMARY KEY (`topologyId`),
  FOREIGN KEY (`topologyId`) REFERENCES `topology` (`id`)
);

-- -----------------------------------------------------
-- Table `topology_edge`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `topology_edge` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `topologyId` INTEGER NOT NULL,
  `fromId` INTEGER NOT NULL,
  `toId` INTEGER NOT NULL,
  `streamGroupings` TEXT NOT NULL,
  FOREIGN KEY (`topologyId`) REFERENCES `topology` (`id`),
  FOREIGN KEY (`fromId`) REFERENCES `topology_component` (`id`),
  FOREIGN KEY (`toId`) REFERENCES `topology_component` (`id`)
);

-- -----------------------------------------------------
-- Table `job_group`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `job_group` (
  `id` TEXT NOT NULL,
  `topologyId` INT NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`topologyId`) REFERENCES `topology` (`id`)
);

-- -----------------------------------------------------
-- Table `job`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `job` (
  `id` TEXT NOT NULL,
  `groupId` TEXT NOT NULL,
  `engineId` TEXT,
  `data` TEXT,
  `input` TEXT,
  `output` TEXT,
  `taskinfo` TEXT,
  `state` TEXT,
  `targetHost` TEXT,
  `runtimeHost` TEXT,
  `engineType` TEXT,
  PRIMARY KEY (`id`, `groupId`),
  FOREIGN KEY (`groupId`) REFERENCES `job_group` (`id`)
);

-- -----------------------------------------------------
-- Table `job_state`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `job_state` (
  `groupId` TEXT NOT NULL,
  `jobId` TEXT NOT NULL,
  `state` TEXT,
  `startTime` LONG,
  PRIMARY KEY (`groupId`, `jobId`),
  FOREIGN KEY (`groupId`) REFERENCES `job_group` (`id`),
  FOREIGN KEY (`jobId`) REFERENCES `job` (`id`)
);

-- -----------------------------------------------------
-- Table `topology_stream`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `topology_stream` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `topologyId` INTEGER NOT NULL,
  `componentId` INTEGER NOT NULL,
  `streamName` TEXT NOT NULL,
  `fields` TEXT NOT NULL,
  FOREIGN KEY (`topologyId`) REFERENCES `topology` (`id`),
  FOREIGN KEY (`componentId`) REFERENCES `topology_component` (`id`)
);


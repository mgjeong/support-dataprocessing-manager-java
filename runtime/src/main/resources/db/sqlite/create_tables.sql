-- -----------------------------------------------------
-- Table `workflow`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `workflow` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `name` TEXT NOT NULL,
  `config` TEXT NOT NULL
);

-- -----------------------------------------------------
-- Table `workflow_component_bundle`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `workflow_component_bundle` (
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
-- Table `workflow_component`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `workflow_component` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `workflowId` INTEGER NOT NULL,
  `componentBundleId` INTEGER NOT NULL,
  `name` TEXT NOT NULL,
  `config` TEXT NOT NULL,
  UNIQUE (`id`, `workflowId`),
  FOREIGN KEY (`workflowId`) REFERENCES `workflow` (`id`),
  FOREIGN KEY (`componentBundleId`) REFERENCES `workflow_component_bundle` (`id`)
);

-- -----------------------------------------------------
-- Table `workflow_editor_metadata`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `workflow_editor_metadata` (
  `workflowId` INTEGER NOT NULL,
  `data` TEXT NOT NULL,
  PRIMARY KEY (`workflowId`),
  FOREIGN KEY (`workflowId`) REFERENCES `workflow` (`id`)
);

-- -----------------------------------------------------
-- Table `workflow_edge`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `workflow_edge` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `workflowId` INTEGER NOT NULL,
  `fromId` INTEGER NOT NULL,
  `toId` INTEGER NOT NULL,
  `streamGroupings` TEXT NOT NULL,
  FOREIGN KEY (`workflowId`) REFERENCES `workflow` (`id`),
  FOREIGN KEY (`fromId`) REFERENCES `workflow_component` (`id`),
  FOREIGN KEY (`toId`) REFERENCES `workflow_component` (`id`)
);

-- -----------------------------------------------------
-- Table `job`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `job` (
  `id` TEXT NOT NULL,
  `workflowId` INTEGER NOT NULL,
  `config` TEXT,
  PRIMARY KEY (`id`, `workflowId`),
  FOREIGN KEY (`workflowId`) REFERENCES `workflow` (`id`)
);

-- -----------------------------------------------------
-- Table `job_state`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `job_state` (
  `jobId` TEXT NOT NULL,
  `state` TEXT,
  `startTime` LONG,
  `engineId` TEXT,
  `engineType` TEXT,
  PRIMARY KEY (`jobId`),
  FOREIGN KEY (`jobId`) REFERENCES `job` (`id`)
);

-- -----------------------------------------------------
-- Table `workflow_stream`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `workflow_stream` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `workflowId` INTEGER NOT NULL,
  `componentId` INTEGER NOT NULL,
  `streamName` TEXT NOT NULL,
  `fields` TEXT NOT NULL,
  FOREIGN KEY (`workflowId`) REFERENCES `workflow` (`id`),
  FOREIGN KEY (`componentId`) REFERENCES `workflow_component` (`id`)
);


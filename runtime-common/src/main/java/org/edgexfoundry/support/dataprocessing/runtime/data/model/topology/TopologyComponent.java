package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopologyComponent extends Format {
    private Long id;
    private Long topologyId;
    private Long versionId;
    private Long topologyComponentBundleId;
    private String name = StringUtils.EMPTY;
    private String description = StringUtils.EMPTY;
    private Config config;

    public TopologyComponent() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTopologyId() {
        return topologyId;
    }

    public void setTopologyId(Long topologyId) {
        this.topologyId = topologyId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public Long getTopologyComponentBundleId() {
        return topologyComponentBundleId;
    }

    public void setTopologyComponentBundleId(Long topologyComponentBundleId) {
        this.topologyComponentBundleId = topologyComponentBundleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    static class Config {
        private Map<String, Object> properties = new HashMap<>();

        public Config() {

        }
    }
}

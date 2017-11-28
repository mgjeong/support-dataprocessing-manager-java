package org.edgexfoundry.support.dataprocessing.runtime.data.model.response;

public class QueryResponseFormat extends JobGroupResponseFormat {

    public QueryResponseFormat( JobGroupResponseFormat format) {
        this.setError(format.getError());
        this.setJobGroups(format.getJobGroups());
    }
}

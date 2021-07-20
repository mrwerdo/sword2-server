package org.swordapp.server;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Statement {
    // common elements with some default values
    protected String contentType = null;
    protected List<OriginalDeposit> originalDeposits = new ArrayList<>();
    protected Map<String, String> states = new HashMap<>();
    protected List<ResourcePart> resources = new ArrayList<>();
    protected Date lastModified = new Date();

    public abstract void writeTo(Writer out) throws IOException;

    public String getContentType() {
        return contentType;
    }

    public void setOriginalDeposits(final List<OriginalDeposit> originalDeposits) {
        this.originalDeposits = originalDeposits;
    }

    public void addOriginalDeposit(final OriginalDeposit originalDeposit) {
        this.originalDeposits.add(originalDeposit);
    }

    public void setResources(final List<ResourcePart> resources) {
        this.resources = resources;
    }

    public void addResource(final ResourcePart resource) {
        this.resources.add(resource);
    }

    public void setStates(final Map<String, String> states) {
        this.states = states;
    }

    public void setState(final String state, final String description) {
        this.states.clear();
        this.states.put(state, description);
    }

    public void addState(final String state, final String description) {
        this.states.put(state, description);
    }

    public Date getLastModified() {
        return lastModified == null ? null : new Date(lastModified.getTime());
    }

    public void setLastModified(final Date lastModified) {
        this.lastModified = new Date(lastModified.getTime());
    }
}

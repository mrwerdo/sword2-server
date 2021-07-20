package org.swordapp.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ResourcePart {
    protected final String uri;
    protected String mediaType;
    protected Map<String, String> properties = new HashMap<>();
    protected List<String> selfLinks = new ArrayList<>();

    public ResourcePart(final String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(final String mediaType) {
        this.mediaType = mediaType;
    }

    public void setProperties(final Map<String, String> properties) {
        this.properties = properties;
    }

    public void addProperty(final String predicate, final String object) {
        this.properties.put(predicate, object);
    }

    public List<String> getSelfLinks() {
        return selfLinks;
    }

    public void addSelfLink(final String link) {
        selfLinks.add(link);
    }

}

package org.swordapp.server;

import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SwordEntry {
    private final Entry entry;

    public SwordEntry(final Entry entry) {
        this.entry = entry;
    }

    public String getTitle() {
        return this.entry.getTitle();
    }

    public String getSummary() {
        return this.entry.getSummary();
    }

    public Entry getEntry() {
        return this.entry;
    }

    public String toString() {
        try {
            StringWriter stringWriter = new StringWriter();
            this.entry.writeTo(stringWriter);
            return stringWriter.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // put the code in for getting a dublin core record out
    public Map<String, List<String>> getDublinCore() {
        Map<String, List<String>> dc = new HashMap<String, List<String>>();
        List<Element> extensions = this.entry.getExtensions();
        for (Element element : extensions) {
            if (UriRegistry.DC_NAMESPACE.equals(element.getQName().getNamespaceURI())) {
                // we have a dublin core extension
                String field = element.getQName().getLocalPart();
                String value = element.getText();

                if (dc.containsKey(field)) {
                    dc.get(field).add(value);
                } else {
                    ArrayList<String> values = new ArrayList<String>();
                    values.add(value);
                    dc.put(field, values);
                }
            }
        }
        return dc;
    }
}

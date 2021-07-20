package org.swordapp.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OriginalDeposit extends ResourcePart {
    private List<String> packaging;
    private Date depositedOn;
    private String depositedBy;
    private String depositedOnBehalfOf;

    public OriginalDeposit(final String uri) {
        this(uri, new ArrayList<String>(), null, null, null);
    }

    public OriginalDeposit(final String uri, final List<String> packaging, final Date depositedOn, final String depositedBy, final String depositedOnBehalfOf) {
        super(uri);
        this.packaging = packaging;
        this.depositedOn = depositedOn;
        this.depositedBy = depositedBy;
        this.depositedOnBehalfOf = depositedOnBehalfOf;
    }

    public String getUri() {
        return uri;
    }

    public List<String> getPackaging() {
        return packaging;
    }

    public void setPackaging(final List<String> packaging) {
        this.packaging = packaging;
    }

    public Date getDepositedOn() {
        return depositedOn;
    }

    public void setDepositedOn(final Date depositedOn) {
        this.depositedOn = depositedOn;
    }

    public String getDepositedBy() {
        return depositedBy;
    }

    public void setDepositedBy(final String depositedBy) {
        this.depositedBy = depositedBy;
    }

    public String getDepositedOnBehalfOf() {
        return depositedOnBehalfOf;
    }

    public void setDepositedOnBehalfOf(final String depositedOnBehalfOf) {
        this.depositedOnBehalfOf = depositedOnBehalfOf;
    }
}

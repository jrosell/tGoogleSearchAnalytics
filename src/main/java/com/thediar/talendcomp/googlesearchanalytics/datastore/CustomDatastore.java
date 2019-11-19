package com.thediar.talendcomp.googlesearchanalytics.datastore;

import java.io.Serializable;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.type.DataStore;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

@DataStore("CustomDatastore")
@GridLayout({
    // the generated layout put one configuration entry per line,
    // customize it as much as needed
    @GridLayout.Row({ "service_account_email" }),
    @GridLayout.Row({ "keyfile" }),
    @GridLayout.Row({ "siteUrl" }),
    @GridLayout.Row({ "startDate" }),
    @GridLayout.Row({ "endDate" }),
    @GridLayout.Row({ "dimensions" }),
    @GridLayout.Row({ "searchType" }),
    @GridLayout.Row({ "dimensionsFilterGroups" }),
    @GridLayout.Row({ "aggregationType" }),
    @GridLayout.Row({ "rowLimit" }),
    @GridLayout.Row({ "startRow" }),
    @GridLayout.Row({ "debug" })

})
@Documentation("Configure all you need to use Search Analytics data to the Google Search Console API")
public class CustomDatastore implements Serializable {
    @Option
    @Required
    @Documentation("")
    private String service_account_email;

    @Option
    @Required
    @Documentation("")
    private String keyfile;

    @Option
    @Required
    @Documentation("")
    private String siteUrl;

    @Option
    @Documentation("")
    private String startDate;

    @Option
    @Documentation("")
    private String endDate;

    @Option
    @Documentation("")
    private String dimensions;

    @Option
    @Documentation("")
    private String searchType;

    @Option
    @Documentation("")
    private String dimensionsFilterGroups;

    @Option
    @Documentation("")
    private String aggregationType;

    @Option
    @Documentation("")
    private String rowLimit;

    @Option
    @Documentation("")
    private String startRow;

    @Option
    @Documentation("")
    private Boolean debug;

    public String getService_account_email() {
        return service_account_email;
    }

    public CustomDatastore setService_account_email(String service_account_email) {
        this.service_account_email = service_account_email;
        return this;
    }

    public String getKeyfile() {
        return keyfile;
    }

    public CustomDatastore setKeyfile(String keyfile) {
        this.keyfile = keyfile;
        return this;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public CustomDatastore setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
        return this;
    }

    public String getStartDate() {
        return startDate;
    }

    public CustomDatastore setStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public String getEndDate() {
        return endDate;
    }

    public CustomDatastore setEndDate(String endDate) {
        this.endDate = endDate;
        return this;
    }

    public String getDimensions() {
        return dimensions;
    }

    public CustomDatastore setDimensions(String dimensions) {
        this.dimensions = dimensions;
        return this;
    }

    public String getSearchType() {
        return searchType;
    }

    public CustomDatastore setSearchType(String searchType) {
        this.searchType = searchType;
        return this;
    }

    public String getDimensionsFilterGroups() {
        return dimensionsFilterGroups;
    }

    public CustomDatastore setDimensionsFilterGroups(String dimensionsFilterGroups) {
        this.dimensionsFilterGroups = dimensionsFilterGroups;
        return this;
    }

    public String getAggregationType() {
        return aggregationType;
    }

    public CustomDatastore setAggregationType(String aggregationType) {
        this.aggregationType = aggregationType;
        return this;
    }

    public String getRowLimit() {
        return rowLimit;
    }

    public CustomDatastore setRowLimit(String rowLimit) {
        this.rowLimit = rowLimit;
        return this;
    }

    public String getStartRow() {
        return startRow;
    }

    public CustomDatastore setStartRow(String startRow) {
        this.startRow = startRow;
        return this;
    }

    public Boolean getDebug() {
        return debug;
    }

    public CustomDatastore setDebug(Boolean debug) {
        this.debug = debug;
        return this;
    }
}
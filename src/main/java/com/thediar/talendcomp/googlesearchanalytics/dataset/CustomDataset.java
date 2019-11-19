package com.thediar.talendcomp.googlesearchanalytics.dataset;

import java.io.Serializable;

import com.thediar.talendcomp.googlesearchanalytics.datastore.CustomDatastore;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.type.DataSet;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

@DataSet("CustomDataset")
@GridLayout({
    // the generated layout put one configuration entry per line,
    // customize it as much as needed
    @GridLayout.Row({ "config" })
})
@Documentation("TODO fill the documentation for this configuration")
public class CustomDataset implements Serializable {
    @Option
    @Documentation("Configuration settings")
    private CustomDatastore config;

    public CustomDatastore getDatastore() {
        return config;
    }

    public CustomDataset setDatastore(CustomDatastore config) {
        this.config = config;
        return this;
    }
}
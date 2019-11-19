package com.thediar.talendcomp.googlesearchanalytics.source;

import java.io.Serializable;

import com.thediar.talendcomp.googlesearchanalytics.dataset.CustomDataset;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

@GridLayout({
    // the generated layout put one configuration entry per line,
    // customize it as much as needed
    @GridLayout.Row({ "dataset" })
})
@Documentation("TODO fill the documentation for this configuration")
public class InputMapperConfiguration implements Serializable {
    @Option
    @Documentation("TODO fill the documentation for this parameter")
    private CustomDataset dataset;

    public CustomDataset getDataset() {
        return dataset;
    }

    public InputMapperConfiguration setDataset(CustomDataset dataset) {
        this.dataset = dataset;
        return this;
    }
}
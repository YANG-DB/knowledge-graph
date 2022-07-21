package org.opensearch.graph.unipop.controller.utils.labelProvider;


public class PrefixedLabelProvider implements LabelProvider<String> {
    //region Constructors
    public PrefixedLabelProvider(String splitString) {
        this.splitString = splitString;
    }
    //endregion

    //region LabelProvider Implementation
    @Override
    public String get(String data) {
        return data.split(splitString)[0];
    }
    //endregion

    //region Fields
    private String splitString;
    //endregion
}

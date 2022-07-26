package org.opensearch.graph.unipop.controller.search.translation;





public class M1QueryTranslator extends CompositeQueryTranslator {
    //region Static
    public static M1QueryTranslator instance = new M1QueryTranslator();
    //endregion

    //region Constructors
    public M1QueryTranslator() {
        super(
                new HiddenQueryTranslator(
                        new CompareQueryTranslator(true),
                        new ExclusiveChainTranslator(
                                new ContainsGeoBoundsQueryTranslator("geoValue"),
                                new ContainsGeoDistanceQueryTranslator("geoValue"),
                                new ContainsQueryTranslator()),
                        new ExistsQueryTranslator(),
                        new CountFilterQueryTranslator(),
                        new TextQueryTranslator(),
//                        new NestedQueryTranslator(),
                        new AndPQueryTranslator(
                                new CompareQueryTranslator(true),
                                new ExclusiveChainTranslator(
                                        new ContainsGeoBoundsQueryTranslator("geoValue"),
                                        new ContainsQueryTranslator()),
                                new ExistsQueryTranslator(),
                                new CountFilterQueryTranslator(),
                                new TextQueryTranslator()
                        ),
                        new OrPQueryTranslator(
                                new CompareQueryTranslator(false),
                                new ExclusiveChainTranslator(
                                        new ContainsGeoBoundsQueryTranslator("geoValue"),
                                        new ContainsGeoDistanceQueryTranslator("geoValue"),
                                        new ContainsQueryTranslator()),
                                new ExistsQueryTranslator(),
                                new CountFilterQueryTranslator(),
                                new TextQueryTranslator()
                        )
                )
        );
    }
    //endregion
}

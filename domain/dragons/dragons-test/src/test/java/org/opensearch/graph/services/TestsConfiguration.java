package org.opensearch.graph.services;

import org.opensearch.graph.services.engine2.data.*;
import org.opensearch.graph.services.mockEngine.*;
import org.opensearch.graph.services.engine2.CursorIT;
import org.opensearch.graph.services.engine2.PageIT;
import org.opensearch.graph.services.engine2.QueryIT;
import org.opensearch.graph.services.engine2.data.*;
import org.opensearch.graph.services.mockEngine.ApiDescriptorIT;
import org.opensearch.graph.services.mockEngine.CatalogIT;
import org.opensearch.graph.services.mockEngine.DataIT;
import org.opensearch.graph.services.mockEngine.PlanIT;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Roman on 05/04/2017.
 */
public class TestsConfiguration {
    //region Static
    public static TestsConfiguration instance = new TestsConfiguration();
    //endregion

    //region Constructors
    public TestsConfiguration() {
        this.testClassesToRun = new HashSet<>();

        //mockEngine tests
        this.testClassesToRun.add(ApiDescriptorIT.class);
        this.testClassesToRun.add(CatalogIT.class);
        this.testClassesToRun.add(org.opensearch.graph.services.mockEngine.CursorIT.class);
        this.testClassesToRun.add(DataIT.class);
        this.testClassesToRun.add(org.opensearch.graph.services.mockEngine.PageIT.class);
        this.testClassesToRun.add(PlanIT.class);
        this.testClassesToRun.add(org.opensearch.graph.services.mockEngine.QueryIT.class);
        //this.testClassesToRun.add(org.opensearch.graph.services.mockEngine.SearchTest.class);

        //engine1 tests
        //this.testClassesToRun.add(org.opensearch.graph.services.engine1.CursorTest.class);
        //this.testClassesToRun.add(org.opensearch.graph.services.engine1.DataTest.class);
        //this.testClassesToRun.add(org.opensearch.graph.services.engine1.PageTest.class);
        //this.testClassesToRun.add(org.opensearch.graph.services.engine1.QueryTest.class);

        //engine2 tests
        this.testClassesToRun.add(CursorIT.class);
        this.testClassesToRun.add(PageIT.class);
        this.testClassesToRun.add(QueryIT.class);

        this.testClassesToRun.add(PromiseEdgeIT.class);
        this.testClassesToRun.add(SingleEntityIT.class);
        this.testClassesToRun.add(DfsRedundantEntityRelationEntityIT.class);
        this.testClassesToRun.add(DfsNonRedundantEntityRelationEntityIT.class);
        this.testClassesToRun.add(CsvCursorIT.class);
        this.testClassesToRun.add(SmartEpbRedundantEntityRelationEntityIT.class);
        this.testClassesToRun.add(SmartEpbM2RedundantEntityRelationEntityIT.class);

        this.testClassesToRun.add(JoinE2EEpbMockIT.class);
        this.testClassesToRun.add(JoinE2EIT.class);
        this.testClassesToRun.add(SmartEpbCountIT.class);
    }
    //endregion

    //region Public Methods
    public boolean shouldRunTestClass(Class testClass) {
        return testClassesToRun.contains(testClass);
    }
    //endregion

    //region Fields
    private Set<Class> testClassesToRun;
    //endregion
}

package org.opensearch.graph.services.test;

import javaslang.collection.Stream;
import org.opensearch.graph.test.TestSetupBase;
import org.opensearch.graph.utils.EngineManager;

import java.util.ArrayList;
import java.util.List;

public class TestRunner {

    public static final int TIMES_TO_RUN = 2;

    public static void run(TestCase testCase, TestSetupBase testSetup, String profileA, String profileB) throws Exception {

        testSetup.init();

        EngineManager profileA_Fuse = new EngineManager("application.engine2.dev.M2.discrete.conf", profileA);
        List<Long> times = new ArrayList<>();
        List<Long> queryTimes = new ArrayList<>();
        profileA_Fuse.init();
        for(int i = 0; i< TIMES_TO_RUN; i++) {
            testCase.run(profileA_Fuse.getGraphClient());
            if(i != 0) {
                times.add(testCase.getTotalTime());
                queryTimes.add(testCase.getPlanTime());
            }
        }
        profileA_Fuse.cleanup();

        long profileA_assignments = testCase.getAssignments();
        long profileATimes =  Stream.ofAll(times).reduce((a, b) -> a+b) / times.size();
        long profileAQueryTime = Stream.ofAll(queryTimes).reduce((a,b) -> a+b) / queryTimes.size();

        times.clear();
        queryTimes.clear();
        EngineManager profileB_Fuse= new EngineManager("application.engine2.dev.M2.discrete.conf", profileB);
        profileB_Fuse.init();
        for(int i = 0;i<TIMES_TO_RUN;i++) {
            testCase.run(profileB_Fuse.getGraphClient());
            if(i != 0) {
                times.add(testCase.getTotalTime());
                queryTimes.add(testCase.getPlanTime());
            }
        }
        long profileB_assignments = testCase.getAssignments();

        profileB_Fuse.cleanup();

        long profileBTimes = Stream.ofAll(times).reduce((a, b) -> a + b)/ times.size();
        long profileBQueryTime = Stream.ofAll(queryTimes).reduce((a, b) -> a + b) / queryTimes.size();

        System.out.println(profileA+" average time:" + profileATimes + ", query time: " + profileAQueryTime);
        System.out.println(profileA+" assignments:" + profileA_assignments);
        System.out.println(profileB+" average time:" + profileBTimes + ", query time: " + profileBQueryTime);
        System.out.println(profileB+" assignments:" + profileB_assignments);

        testSetup.cleanup();
    }
}

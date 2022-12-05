package org.opensearch.graph.generator.util;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RandomUtilTest extends TestCase {


    @Test
    public void testGetExpDistArray() {
        double[] expDistArray = RandomUtil.getExpDistArray(100, 1.0, 0.5);

        List<Double> list = Arrays.stream(expDistArray).boxed().collect(Collectors.toList());
        double sum = list.stream().mapToDouble(d -> d).sum();
        //System.out.println(sum);
        assertEquals(1.0, sum, 0.1);
    }

    @Test
    public void testGetCumulativeDistArray() {
        double[] expDistArray = RandomUtil.getExpDistArray(100, 1.0, 0.5);
        double[] cumulativeDistArray = RandomUtil.getCumulativeDistArray(expDistArray);
        //Since we are talking on statistics the number supposed to be close to 1.0
        assertEquals(cumulativeDistArray[cumulativeDistArray.length - 1], 1.0, 0.1 );
    }

    public void testRandomGaussianNumbers() {
    }

    public void testRandomGaussianNumber() {
    }

    public void testEnumeratedDistribution() {
    }

    public void testRandomDateInEpoch() {
    }

    public void testRandomDate() {
    }

    public void testGetRandDistArray() {
    }

    public void testGetRandomElementFromList() {
    }

    public void testGetRandomElementFromArray() {
    }

    public void testRandomEnum() {
    }

    public void testRandomInt() {
    }

    public void testUniform() {
    }

    public void testTestUniform() {
    }

    public void testTestUniform1() {
    }

    public void testTestUniform2() {
    }

    public void testGeometric() {
    }

    public void testBernoulli() {
    }

    public void testTestBernoulli() {
    }

    public void testPoisson() {
    }

    public void testExp() {
    }

    public void testShuffle() {
    }

    public void testTestShuffle() {
    }
}
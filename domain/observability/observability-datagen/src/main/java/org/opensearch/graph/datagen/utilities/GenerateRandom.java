
package org.opensearch.graph.datagen.utilities;

/*-
 * #%L
 * dragons-datagen
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */





import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.opensearch.graph.datagen.utilities.GenerateRandom.GeneratorStrategy.RANDOM;
import static org.opensearch.graph.datagen.utilities.GenerateRandom.GeneratorStrategy.valueOf;

public class GenerateRandom {

    public enum GeneratorStrategy {
        RANDOM,NORMAL,DELTA
    }

    public static final int DEFAULT_MIN_VALUE = -100000;
    public static final int DEFAULT_MAX_VALUE = 100000;
    public static final String MIN_VALUE = "MIN_VALUE";
    public static final String MAX_VALUE = "MAX_VALUE";
    public static final String DIVIDER = "DIVIDER";
    public static final String DELTA = "DELTA";
    public static final String MEAN = "MEAN";
    public static final String STANDARD_DEVIATION = "STANDARD_DEVIATION";

    public static final String STRATEGY = "STRATEGY";

    public static int genRandomInt(int min , int max) {
        
        if (min > max)
            throw new IllegalArgumentException("Start cannot exceed End.");
        Random rand = new Random();
        return rand.nextInt(((max - min) + 1) > 0 ?((max - min) + 1) : ((max - min) + 10) ) + min;
    }

    public static long genRandomLong(long min , long max) {
        if (min > max)
            throw new IllegalArgumentException("Start cannot exceed End.");
        Random rand = new Random();
        return rand.longs(1,min,max).findAny().getAsLong();

    }

    public static double genRandomDouble(double rangeMin , double rangeMax) {
        if (rangeMin > rangeMax)
            throw new IllegalArgumentException("Start cannot exceed End.");
        Random r = new Random();
        double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
        return randomValue ;
    }
    
    public static boolean testWithProb(int prob) {
        
        int rand = genRandomInt(1,100) ;
        boolean isPos = true ;
        if((rand - (100-prob)) < 0)
            isPos =  false ;
        return isPos;
    }
    
    public static int genRandomWithNormalDistribution(int desiredStandardDeviation , int desiredMean) {
        
        Random r = new Random() ;
        int val = (int) (Math.round(r.nextGaussian()*desiredStandardDeviation + desiredMean));
        return val ;
    }
    
    public static int genRandomWithDiffDelta(int maxValue , int divisor ,double delta) {
        
        double rand = Math.random() ;
        int res ;
        if (rand < delta)
            res = 0 ;
        else if ((rand >= delta) && (rand < (1.0 - delta)) ) 
            res = genRandomInt(1,(int)Math.round((double)maxValue/divisor)) ;
        else // rand >= (1.0 - delta)
            res = genRandomInt((int)Math.round((double)maxValue/2),maxValue);
        return res;
    }

    public static int generateIntProperty(Optional propertyContext, String property) {
        if(propertyContext.isEmpty())
            return genRandomInt(0,100);

        if(propertyContext.get() instanceof Map) {
            Map map = (Map) propertyContext.get();

            GeneratorStrategy strategy = valueOf(map.getOrDefault(STRATEGY, RANDOM).toString());
            int min_value = (int) map.getOrDefault(MIN_VALUE, 0);
            int max_value = (int) map.getOrDefault(MAX_VALUE, 100);
            int mean = (int) map.getOrDefault(MEAN, 10);
            int standardDeviation = (int) map.getOrDefault(STANDARD_DEVIATION, 5);
            int divider = (int) map.getOrDefault(DIVIDER, 10);
            double delta = (double) map.getOrDefault(DELTA, 0.5);

            switch (strategy) {
                case DELTA:
                    return genRandomWithDiffDelta(max_value,divider,delta);
                case RANDOM:
                    return genRandomInt(min_value,max_value);
                case NORMAL:
                    return genRandomWithNormalDistribution(standardDeviation,mean);
            }
        }
        return genRandomInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
    }

    public static long generateLongProperty(Optional propertyContext, String property) {
        if(propertyContext.isEmpty())
            return genRandomInt(0,100);

        if(propertyContext.get() instanceof Map) {
            Map map = (Map) propertyContext.get();

            GeneratorStrategy strategy = valueOf(map.getOrDefault(STRATEGY, RANDOM).toString());
            long min_value = ((Number) map.getOrDefault(MIN_VALUE, 0)).longValue();
            long max_value = ((Number) map.getOrDefault(MAX_VALUE, 100)).longValue();
            long mean = ((Number) map.getOrDefault(MEAN, 10)).longValue();
            long standardDeviation = ((Number)  map.getOrDefault(STANDARD_DEVIATION, 5)).longValue();
            long divider = ((Number) map.getOrDefault(DIVIDER, 10)).longValue();
            double delta = (double) map.getOrDefault(DELTA, 0.5);

            switch (strategy) {
                case DELTA:
                    return genRandomWithDiffDelta((int) max_value, (int) divider,delta);
                case RANDOM:
                    return genRandomLong(min_value,max_value);
                case NORMAL:
                    return genRandomWithNormalDistribution((int) standardDeviation, (int) mean);
            }
        }
        return genRandomLong(Long.MIN_VALUE,Long.MAX_VALUE);
    }

    public static float generateFloatProperty(Optional propertyContext, String property) {
        if(propertyContext.isEmpty())
            return genRandomInt(0,100);
        if(propertyContext.get() instanceof Map) {
            int min_value = (int) ((Map) propertyContext.get()).getOrDefault(MIN_VALUE, 0);
            int max_value = (int) ((Map) propertyContext.get()).getOrDefault(MAX_VALUE, 100);
            return genRandomInt(min_value,max_value);
        }
        return genRandomInt(DEFAULT_MIN_VALUE,DEFAULT_MAX_VALUE);
    }
}

package org.unipop.common.valueSuppliers;






import java.util.function.Supplier;

public class CompiledSupplierFactory extends TimeBasedSupplierFactory {
    public enum ValueAggMethod {
        max {
            @Override
            public int apply(int[] buffer, int startIndex, int endIndex) {
                int max = Integer.MIN_VALUE;
                for(int i = startIndex ; i < endIndex ; i++) {
                    max = Math.max(max, buffer[i]);
                }
                return max;
            }
        },

        min {
            @Override
            public int apply(int[] buffer, int startIndex, int endIndex) {
                int min = Integer.MAX_VALUE;
                for(int i = startIndex ; i < endIndex ; i++) {
                    min = Math.min(min, buffer[i]);
                }
                return min;
            }
        },

        avg {
            @Override
            public int apply(int[] buffer, int startIndex, int endIndex) {
                int sum = 0;
                for(int i = startIndex ; i < endIndex ; i++) {
                    sum += buffer[i];
                }
                return Math.round((float) sum / (endIndex - startIndex));
            }
        };

        public abstract int apply(int[] buffer, int startIndex, int endIndex);
    }

    //region Constructors
    public CompiledSupplierFactory(
            TimeBasedSupplierFactory rawSupplierFactory,
            long duration,
            int resolution,
            ValueAggMethod valueAggMethod) {
        this(rawSupplierFactory, duration, resolution, valueAggMethod, Clock.System.instance);
    }

    public CompiledSupplierFactory(
            TimeBasedSupplierFactory rawSupplierFactory,
            long duration,
            int resolution,
            ValueAggMethod valueAggMethod,
            Clock clock) {
        super(clock);

        Clock.Manual manualClock = new Clock.Manual(0);
        rawSupplierFactory.setClock(manualClock);
        Supplier<Integer> rawSupplier = rawSupplierFactory.get();

        int[] compiledValueBuffer = new int[(int)duration];
        for(int t = 0; t < duration ; t++) {
            manualClock.setTime(t);
            compiledValueBuffer[t] = rawSupplier.get();
        }

        int[] compressedValueBuffer = new int[resolution + 1];
        long[] compressedTimeBuffer = new long[resolution + 1];

        double resolutionStep = (double)duration / resolution;
        double resolutionProgress = resolutionStep;
        int valueStartIndex = 0;
        for(int t = 0 ; t < resolution ; t++, resolutionProgress += resolutionStep) {
            compressedTimeBuffer[t] = Math.round(resolutionProgress);
            compressedValueBuffer[t] = valueAggMethod.apply(compiledValueBuffer, valueStartIndex, (int)compressedTimeBuffer[t]);
            valueStartIndex = (int)compressedTimeBuffer[t];
        }

        compressedTimeBuffer[resolution] = Math.round(resolutionProgress);

        manualClock.setTime(duration + 1);
        compressedValueBuffer[resolution] = rawSupplier.get();

        this.compiledSupplier = new LinearBufferedSupplierFactory(compressedTimeBuffer, compressedValueBuffer, clock);
    }
    //endregion

    //region Supplier Implementation
    @Override
    public Supplier<Integer> get() {
        return this.compiledSupplier.get();
    }
    //endregion

    //region Fields
    Supplier<Supplier<Integer>> compiledSupplier;
    //endregion
}

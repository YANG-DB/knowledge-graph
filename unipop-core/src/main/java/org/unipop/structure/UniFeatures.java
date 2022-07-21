package org.unipop.structure;






import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

public class UniFeatures implements Graph.Features {

    EngineVertexFeatures engineVertexFeatures = new EngineVertexFeatures();
    EngineEdgeFeatures engineEdgeFeatures = new EngineEdgeFeatures();
    EngineGraphFeatures engineGraphFeatures = new EngineGraphFeatures();

    @Override
    public String toString() {
        return StringFactory.featureString(this);
    }

    @Override
    public VertexFeatures vertex() {
        return engineVertexFeatures;
    }

    @Override
    public EdgeFeatures edge() {
        return engineEdgeFeatures;
    }

    @Override
    public GraphFeatures graph() {
        return engineGraphFeatures;
    }

    private class EngineGraphFeatures implements GraphFeatures {
        EngineVariableFeatures engineVariableFeatures = new EngineVariableFeatures();

        @Override
        public VariableFeatures variables() {
            return engineVariableFeatures;
        }

        @Override
        public boolean supportsComputer() {
            return false;
        }

        @Override
        public boolean supportsTransactions() {
            return false;
        }

        @Override
        public boolean supportsThreadedTransactions() {
            return false;
        }
    }

    private class EngineVariableFeatures implements VariableFeatures {
        @Override
        public boolean supportsVariables() {
            return false;
        }

        @Override
        public boolean supportsBooleanValues() {
            return false;
        }

        @Override
        public boolean supportsByteValues() {
            return false;
        }

        @Override
        public boolean supportsDoubleValues() {
            return false;
        }

        @Override
        public boolean supportsFloatValues() {
            return false;
        }

        @Override
        public boolean supportsIntegerValues() {
            return false;
        }

        @Override
        public boolean supportsLongValues() {
            return false;
        }

        @Override
        public boolean supportsMapValues() {
            return false;
        }

        @Override
        public boolean supportsMixedListValues() {
            return false;
        }

        @Override
        public boolean supportsBooleanArrayValues() {
            return false;
        }

        @Override
        public boolean supportsByteArrayValues() {
            return false;
        }

        @Override
        public boolean supportsDoubleArrayValues() {
            return false;
        }

        @Override
        public boolean supportsFloatArrayValues() {
            return false;
        }

        @Override
        public boolean supportsIntegerArrayValues() {
            return false;
        }

        @Override
        public boolean supportsStringArrayValues() {
            return false;
        }

        @Override
        public boolean supportsLongArrayValues() {
            return false;
        }

        @Override
        public boolean supportsSerializableValues() {
            return false;
        }

        @Override
        public boolean supportsStringValues() {
            return false;
        }

        @Override
        public boolean supportsUniformListValues() {
            return false;
        }
    }

    private class EngineEdgeFeatures implements EdgeFeatures {
        @Override
        public boolean supportsNumericIds() {
            return false;
        }

        @Override
        public boolean supportsAnyIds() {
            return false;
        }

        @Override
        public boolean supportsUuidIds() {
            return false;
        }

        @Override
        public boolean supportsCustomIds() {
            return false;
        }

        @Override
        public boolean willAllowId(Object id) {
            return true;
        }
    }

    private class EngineVertexFeatures implements VertexFeatures {
        EngineVertexPropertyFeatures engineVertexPropertyFeatures = new EngineVertexPropertyFeatures();

        @Override
        public boolean supportsMultiProperties() {
            return true;
        }

        @Override
        public boolean supportsNumericIds() {
            return false;
        }

        @Override
        public boolean supportsUuidIds() {
            return false;
        }

        @Override
        public boolean supportsCustomIds() {
            return false;
        }

        @Override
        public boolean supportsAnyIds() {
            return false;
        }

        @Override
        public boolean supportsMetaProperties() {
            return false;
        }

        @Override
        public VertexPropertyFeatures properties() {
            return engineVertexPropertyFeatures;
        }

        @Override
        public boolean willAllowId(Object id) {
            return true;
        }
    }

    private class EngineVertexPropertyFeatures implements VertexPropertyFeatures {
        @Override
        public boolean supportsUserSuppliedIds() {
            return false;
        }

        @Override
        public boolean supportsNumericIds() {
            return true;
        }

        @Override
        public boolean supportsStringIds() {
            return false;
        }

        @Override
        public boolean supportsUuidIds() {
            return false;
        }

        @Override
        public boolean supportsCustomIds() {
            return false;
        }

        @Override
        public boolean supportsAnyIds() {
            return false;
        }
    }
}

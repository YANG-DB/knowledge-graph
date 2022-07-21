package org.openserach.graph.asg.util;





import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.ontology.Property;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;

public class OntologyPropertyTypeFactory {

    static SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    //region Constructor
    public OntologyPropertyTypeFactory() {
        this.map = new HashMap<>();
        this.map.put("string", new StringFunction());
        this.map.put("int", new LongFunction());
        this.map.put("float", new DoubleFunction());
        this.map.put("double", new DoubleFunction());
        this.map.put("date", new DateFunction());
    }
    //endregion

    //region Public Methods
    public Object supply(Property prop, Object exp) {
        if (exp != null) {
            Function<Object, Object> tranformFunction = this.map.get(prop.getType());
            if (tranformFunction != null) {
                return tranformFunction.apply(exp);
            }
        }

        return exp;
    }
    //endregion

    //region Fields
    private Map<String, Function<Object, Object>> map;
    //endregion

    //Functions
    private class StringFunction implements Function<Object, Object> {

        @Override
        public Object apply(Object o) {
            return o;
        }
    }

    private class LongFunction implements Function<Object, Object> {

        @Override
        public Object apply(Object o) {
            if (o instanceof String) {
                return Long.parseLong((String) o);
            }

            if (Number.class.isAssignableFrom(o.getClass())) {
                return ((Number) o).longValue();
            }

            return o;
        }
    }

    private class DoubleFunction implements Function<Object, Object> {

        @Override
        public Object apply(Object o) {
            if (o instanceof String) {
                return Double.parseDouble((String) o);
            }

            if (Number.class.isAssignableFrom(o.getClass())) {
                return ((Number) o).doubleValue();
            }

            return o;
        }
    }

    private class DateFunction implements Function<Object, Object> {

        @Override
        public Object apply(Object o) {
            if (o instanceof Date)
                return o;

            if (Number.class.isAssignableFrom(o.getClass()))
                return new Date(((Number) o).longValue());

            if (o instanceof String) {
                try {
                    return sdf.parse(o.toString());
                } catch (Exception err) {
                    //faild parsing date
                }
            }
            return o;
        }
    }
    //endregion
}

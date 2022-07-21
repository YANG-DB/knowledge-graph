package org.opensearch.graph.model.query.entity;




/**
 * Created by lior.perry on 4/26/2017.
 */
public interface Typed {

    String getTyped();

    String[] getParentTyped();

    interface eTyped extends Typed{
        void seteType(String eType);

        String geteType();
    }

    interface rTyped extends Typed{
        void setrType(String rType);

        String getrType();
    }
}


package org.opensearch.graph.datagen.storyoficeandfire.relations;





import org.opensearch.graph.datagen.dateandtime.DateFactory;

/**
 *
 * @author smuel
 * @param <T>
 */
public class RelationEntity <T>{
   
    
    public T obj ;
    public DateFactory since ;
    public DateFactory till ;
    
    public RelationEntity( T obj ) {
        this.obj = obj ;
    }
    
    public RelationEntity( T obj , DateFactory since) {
        
        this.obj = obj ;
        this.since = new DateFactory(since.gc) ;
        
    }
    
    public void setSince (DateFactory since) {
        this.since = new DateFactory(since.gc) ;
    }
    
    public void setTill(DateFactory till) {
        
        this.till = new DateFactory(till.gc) ;
    }
}
   

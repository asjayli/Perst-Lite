package org.garret.perst;

import java.util.Iterator;

/**
 * Class representing JSQL query. JSQL allows to select members of Perst collections 
 * using SQL like predicate. Almost all Perst collections have select() method 
 * which execute arbitrary JSQL query. But it is also possible to create Query instance explicitely, 
 * Using storage.createQuery class. In this case it is possible to specify query with parameters, 
 * once prepare query and then multiple times specify parameters and execute it. 
 * Also Query interface allows to specify <i>indices</i> and <i>resolvers</i>.
 * JSQL can use arbitrary Perst <code>GenericIndex</code> to perform fast selection if object
 * instead of sequeial search. And user provided <i>resolver</i> allows to substitute SQL joins.
 */
public interface Query {
    /**
     * Execute query
     * @param cls class of inspected objects
     * @param iterator iterator for sequential access to objects in the table
     * @param predicate selection crieria
     * @return iterator through selected objects. This iterator doesn't support remove() 
     * method.
     */
    public Iterator select(Class cls, Iterator iterator, String predicate) throws CompileError;


    /**
     * Execute query
     * @param className name of the class of inspected objects
     * @param iterator iterator for sequential access to objects in the table
     * @param predicate selection crieria
     * @return iterator through selected objects. This iterator doesn't support remove() 
     * method.
     */
    public Iterator select(String className, Iterator iterator, String predicate) throws CompileError;

    /**
     * Set value of query parameter
     * @param index parameters index (1 based)
     * @param value value of parameter (for scalar parameters instance f correspondendt wrapper class, 
     * for example <code>java.lang.Long</code>
     */
    public void setParameter(int index, Object value);

    /**
     * Set value of query parameter
     * @param index parameters index (1 based)
     * @param value value of integer parameter
     */
    public void setIntParameter(int index, long value);

    /**
     * Set value of query parameter
     * @param index parameters index (1 based)
     * @param value value of real parameter
     */
    public void setRealParameter(int index, double value);

    /**
     * Set value of query parameter
     * @param index parameters index (1 based)
     * @param value value of boolean parameter
     */
    public void setBoolParameter(int index, boolean value);

    /**
     * Prepare SQL statement
     * @param cls class of iterated objects
     * @param predicate selection crieria with '?' placeholders for parameter value
     */    
    public void prepare(Class cls, String predicate);

    /**
     * Prepare SQL statement
     * @param className name of the class of iterated objects
     * @param predicate selection crieria with '?' placeholders for parameter value
     */    
    public void prepare(String className, String predicate);

    /**
     * Execute prepared query
     * @param iterator iterator for sequential access to objects in the table
     * @return iterator through selected objects. This iterator doesn't support remove() 
     * method.
     */
    public Iterator execute(Iterator iterator);
            
    /**
     * Enable or disable reporting of runtime errors on console.
     * Runtime errors during JSQL query are reported in two ways:
     * <OL>
     * <LI>If query error reporting is enabled then message is  printed to System.err</LI>
     * <LI>If storage listener is registered, then JSQLRuntimeError of method listener is invoked</LI>
     * </OL>     
     * By default reporting to System.err is enabled.
     * @param enabled if <code>true</code> then reportnig is enabled
     */
    public void enableRuntimeErrorReporting(boolean enabled); 
    
    /**
     * Specify resolver. Resolver can be used to replaced SQL JOINs: given object ID, 
     * it will provide reference to the resolved object
     * @param original class which instances will have to be resolved
     * @param resolved class of the resolved object
     * @param resolver class implementing Resolver interface
     */
    public void setResolver(Class original, Class resolved, Resolver resolver);

    /**
     * Add index which can be used to optimize query execution (replace sequential search with direct index access)
     * @param key indexed field
     * @param index implementation of index
     */
    public void addIndex(String key, GenericIndex index);
}

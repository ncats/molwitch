package gov.nih.ncats.witch;

import gov.nih.ncats.common.functions.BiIndexedConsumer;
import gov.nih.ncats.common.functions.BiIntConsumer;

/**
 * A table of all the {@link Bond}s that exist
 * in a {@link Chemical}.
 * 
 * @author katzelda
 *
 */
public interface BondTable {
	/**
	 * Does a {@link Bond} exist between
	 * the {@code ith} {@link Atom} and the {@code jth} {@link Atom}.
	 * 
	 * @param i the {@code ith} {@link Atom} in this {@link Chemical}.
	 * @param j the {@code jth} {@link Atom} in this {@link Chemical}.
	 * 
	 * @return {@code true} if a bond does exist; {@code false} otherwise.
	 */
	boolean bondExists(int i, int j);
	/**
	 * Get the {@link Bond}exist between
	 * the {@code ith} {@link Atom} and the {@code jth} {@link Atom}.
	 * 
	 * @param i the {@code ith} {@link Atom} in this {@link Chemical}.
	 * @param j the {@code jth} {@link Atom} in this {@link Chemical}.
	 * 
	 * @return the {@link Bond} if this bond exists;
	 * or {@code null} otherwise.
	 * 
	 * @see #bondExists(int, int)
	 */
	Bond getBond(int i, int j);
	int getAtomCount();
	
	default void existingBonds(BiIntConsumer consumer){
		int count= getAtomCount();
		
		for(int i=0; i<count; i++){
			for(int j=0; j<count; j++){
				if(bondExists(i,j)){
					consumer.accept(i, j);
				}
			}
		}
	}
	
	default void existingBonds(BiIndexedConsumer<Bond> consumer){
		int count= getAtomCount();
		
		for(int i=0; i<count; i++){
			for(int j=0; j<count; j++){
				if(bondExists(i,j)){
				    Bond b = getBond(i,j);
				    if(b == null){
				        System.out.println("null bond !?? " + i + "  " + j);
				    }
					consumer.accept(i, j, b);
				}
			}
		}
	}
}

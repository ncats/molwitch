package gov.nih.ncats.witch.io;

import java.io.Closeable;
import java.io.IOException;

import gov.nih.ncats.witch.Chemical;
/**
 * Interface for writing (multiple) Chemicals out.
 * @author katzelda
 *
 */
public interface ChemicalWriter extends Closeable{
	/**
	 * Write the given {@link Chemical} to the output for this writer.
	 * @param chemical the {@link Chemical} to write; can not be null.
	 * 
	 * @throws IOException if there is a problem writing out the chemcial.
	 * 
	 * @throws NullPointerException if chemical is null.
	 */
	void write(Chemical chemical) throws IOException;
	
}

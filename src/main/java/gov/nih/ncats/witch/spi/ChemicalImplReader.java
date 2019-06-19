package gov.nih.ncats.witch.spi;

import java.io.Closeable;
import java.io.IOException;

import gov.nih.ncats.witch.io.ChemicalReader;
/**
 * Reader to parse {@link ChemicalImpl} objects
 * from a datasource.  This is the SPI equivalent to
 * {@link ChemicalReader}.
 * 
 * @author katzelda
 *
 */
public interface ChemicalImplReader extends Closeable{

	
	/**
	 * Read the next {@link ChemicalImpl} from the datasource
	 * if there is one.
	 * @return a new {@link ChemicalImpl} instance or
	 * {@code null} if there are no more chemicals left
	 * or the end of file was reached.
	 * 
	 * @throws IOException if there is a problem parsing the 
	 * next chemical.
	 */
	ChemicalImpl read() throws IOException;

}

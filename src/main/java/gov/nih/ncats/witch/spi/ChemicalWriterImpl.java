package gov.nih.ncats.witch.spi;

import java.io.Closeable;
import java.io.IOException;



public interface ChemicalWriterImpl extends Closeable{

	void write(ChemicalImpl chemicalImpl) throws IOException;
	
}

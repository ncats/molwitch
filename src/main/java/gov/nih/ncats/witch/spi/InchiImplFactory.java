package gov.nih.ncats.witch.spi;

import java.io.IOException;

import gov.nih.ncats.witch.Chemical;
import gov.nih.ncats.witch.inchi.InChiResult;

public interface InchiImplFactory {

	InChiResult asStdInchi(Chemical chemical, boolean trustCoordinates) throws IOException;
	
	Chemical parseInchi(String inchi) throws IOException;
}

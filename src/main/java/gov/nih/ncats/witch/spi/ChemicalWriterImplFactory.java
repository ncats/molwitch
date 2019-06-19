package gov.nih.ncats.witch.spi;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import gov.nih.ncats.witch.io.ChemFormat.ChemFormatWriterSpecification;
public interface ChemicalWriterImplFactory {

	
	ChemicalWriterImpl newInstance(OutputStream out, ChemFormatWriterSpecification spec) throws IOException;
	

	default ChemicalWriterImpl newInstance(File outputFile, ChemFormatWriterSpecification spec) throws IOException{
		return newInstance(new BufferedOutputStream(new FileOutputStream(outputFile)), spec);
	}

	boolean supports(ChemFormatWriterSpecification spec);
}

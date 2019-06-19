package gov.nih.ncats.witch.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import gov.nih.ncats.witch.Chemical;
import gov.nih.ncats.witch.io.ChemFormat.ChemFormatWriterSpecification;
import gov.nih.ncats.witch.spi.ChemicalWriterImpl;
import gov.nih.ncats.witch.spi.ChemicalWriterImplFactory;

class DelegateChemicalWriter implements ChemicalWriter{

	private final ChemicalWriterImpl delegate;
	
	

	public DelegateChemicalWriter(ChemicalWriterImplFactory factory, OutputStream out, ChemFormatWriterSpecification spec) throws IOException{
		Objects.requireNonNull(factory);
		Objects.requireNonNull(out);
		delegate = factory.newInstance(out, spec);
	}
	
	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public void write(Chemical chemical) throws IOException {
		try{
			delegate.write(chemical.getImpl());
		}catch(Throwable t){
			//sometimes the impl writer throws unchecked
			//exceptions which slip through so catch all throwables
			throw new IOException(t);
		}
	}

	

}

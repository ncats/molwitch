package gov.nih.ncats.witch.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.ServiceLoader;

import gov.nih.ncats.witch.Chemical;
import gov.nih.ncats.witch.io.ChemFormat.ChemFormatWriterSpecification;
import gov.nih.ncats.witch.internal.IOUtil;
import gov.nih.ncats.witch.spi.ChemicalWriterImplFactory;

public final class ChemicalWriterFactory {

//	private static ThreadLocal<ServiceLoader<ChemicalWriterImplFactory>> loader = ThreadLocal.withInitial(()->ServiceLoader.load(ChemicalWriterImplFactory.class));
	
	
	private ChemicalWriterFactory(){
		//can not instantiate
	}
	

	
	/**
	 * Create a new ChemicalWriter that will write {@link Chemical}s
	 * in the given format to the provided {@link File}.
	 * 
	 * @param spec the {@link ChemFormatWriterSpecification} object describing
	 * how the chemical should be written; can not be null.
	 * @param outputFile the {@link File} to write to; can not be null. If the 
	 * directory for this file, does not exist, then it will be created.  If the file
	 * already exists, then it will be overwritten.
	 * 
	 * @return a new {@link ChemicalWriter}, will never be null.
	 * 
	 * @throws IOException if there is a problem creating the writer.
	 * @throws NullPointerException if either parameter is null.
	 */
	public static ChemicalWriter newWriter(ChemFormatWriterSpecification spec, File outputFile) throws IOException{
		ChemicalWriterImplFactory factory = getImplForFormat(spec);
		
		IOUtil.mkdirs(outputFile.getParentFile());
		return new DelegateChemicalWriter(factory, new BufferedOutputStream(new FileOutputStream(outputFile)),spec);
	
	}
	
	
	
	private static ChemicalWriterImplFactory getImplForFormat(ChemFormatWriterSpecification spec) {
		//can't cache because options could be different
		//and return different factory for the same format
		//(ex: mol v2000 vs v3000)
		for(ChemicalWriterImplFactory factory  : ServiceLoader.load(ChemicalWriterImplFactory.class, 
				Thread.currentThread().getContextClassLoader())){
			if(factory.supports(spec)){
				return factory;
			}
		}
		throw new IllegalStateException("no implementation for format :" + spec );	
		
	}


	
	/**
	 * Create a new ChemicalWriter that will write {@link Chemical}s
	 * in the given format to the provided {@link OutputStream}.
	 * 
	 * @param out the {@link OutputStream} to write to; can not be null.
	 * @param spec the {@link ChemFormatWriterSpecification} to use; can not be null.
	 *
	 * @return a new {@link ChemicalWriter}, will never be null.
	 * 
	 * @throws IOException if there is a problem creating the writer.
	 * @throws NullPointerException if either parameter is null.
	 */
	public static ChemicalWriter newWriter(ChemFormatWriterSpecification spec, OutputStream out) throws IOException{
		Objects.requireNonNull(spec);
		Objects.requireNonNull(out);
		
		ChemicalWriterImplFactory factory = getImplForFormat(spec);
		
		return new DelegateChemicalWriter(factory, out,spec);
	}
}

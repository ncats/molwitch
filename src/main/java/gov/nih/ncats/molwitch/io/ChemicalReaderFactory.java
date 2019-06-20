/*
 * NCATS-MOLWITCH
 *
 * Copyright 2019 NIH/NCATS
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gov.nih.ncats.molwitch.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.molwitch.ImplUtil;
import gov.nih.ncats.molwitch.spi.ChemicalImpl;
import gov.nih.ncats.molwitch.spi.ChemicalImplFactory;
import gov.nih.ncats.molwitch.spi.ChemicalImplReader;
/**
 * Factory class that can create {@link ChemicalReader}s
 * from various input sources.  The factory methods
 * will automatically determine the type of molecule encoding
 * that was used and choose the appropriate decoder.
 * 
 * @author katzelda
 *
 */
public final class ChemicalReaderFactory {

	private ChemicalReaderFactory(){
		//can not instantiate
	}
	
	/**
	 * Create a new Reader that will read in the
	 *  data from the given byte array.  The entire byte array
	 *  will be parsed.
	 *  <p>
	 *  This is the same as
	 *  {{@link #newReader(byte[], int, int) newReader(bytes, 0, bytes.length)}
	 *  
	 *  
	 * @param molBytes the byte encoded molecule data to parse, usually
	 * a mol file  or SMILES string.
	 * 
	 * @return a new {@link ChemicalReader}, will never be null.
	 * 
	 * @throws IOException if there is a problem parsing the data.
	 * @throws NullPointerException if molBytes is null.
	 */
	public static ChemicalReader newReader(byte[] molBytes) throws IOException{
		return newReader(molBytes, 0, molBytes.length);
	}
	/**
	 * Create a new Reader that will read in the {@code length} bytes 
	 *  from the given byte array starting from the start offset.
	 *  
	 *  
	 * @param molBytes the byte encoded molecule data to parse, usually
	 * a mol file  or SMILES string.
	 * @param start the start offset in the array to start reading from.
	 * @param length the number of bytes in thearray to read.
	 * 
	 * @return a new {@link ChemicalReader}, will never be null.
	 * 
	 * @throws IOException if there is a problem parsing the data.
	 * @throws NullPointerException if molBytes is null.
	 */
	public static ChemicalReader newReader(byte[] molBytes, int start, int length) throws IOException{
		ChemicalImplFactory factory = ImplUtil.getChemicalImplFactory();
		return new DefaultChemicalReader(factory.create(molBytes, start, length));
	}
	
	public static ChemicalReader newReader(InputStream in) throws IOException{
		Objects.requireNonNull(in, "inputstream can not be null");
		ChemicalImplFactory factory = ImplUtil.getChemicalImplFactory();
		if(factory ==null) {
			throw new IOException("could not find chemical factory");
		}
		return new DefaultChemicalReader(factory.create(in));
	}
	public static ChemicalReader newReader(String format, InputStream in) throws IOException{
		Objects.requireNonNull(in, "inputstream can not be null");
		ChemicalImplFactory factory = ImplUtil.getChemicalImplFactory(format);
		if(factory ==null) {
			throw new IOException("could not find chemical factory for format " + format);
		}
		return new DefaultChemicalReader(factory.create(format, in));
	}
	/**
	 * Create a new Reader that will read in the
	 *  data from the given File.
	 *  
	 *  
	 * @param molFile the {@link File} encoding molecule data, usually
	 * a mol file  or SMILES file.  The File must exist.
	 * 
	 * @return a new {@link ChemicalReader}, will never be null.
	 * 
	 * @throws IOException if there is a problem parsing the data or reading the file.
	 * @throws NullPointerException if molFile is null.
	 * 
	 * @see #newReader(File)
	 */
	public static ChemicalReader newReader(File molFile) throws IOException{
		ChemicalImplFactory factory = ImplUtil.getChemicalImplFactory();
		return new DefaultChemicalReader(factory.create(molFile));
		
	}
	
	/**
	 * Create a new Reader that will read in the
	 *  data from the given File.
	 *  
	 * @param format the format the file is encoded with.
	 * 
	 * @param file the {@link File} encoding molecule data, usually
	 * a mol file  or SMILES file.  The File must exist.
	 * 
	 * @return a new {@link ChemicalReader}, will never be null.
	 * 
	 * @throws IOException if there is a problem parsing the data or reading the file.
	 * @throws NullPointerException if molFile is null.
	 * 
	 * @see #newReader(File)
	 */
	public static ChemicalReader newReader(String format, File file) throws IOException{
		ChemicalImplFactory factory = ImplUtil.getChemicalImplFactory(format);
		return new DefaultChemicalReader(factory.create(format, file));
		
	}
	
	
	private static class DefaultChemicalReader implements ChemicalReader{
		private final ChemicalImplReader delegate;

		private ChemicalImpl next;
		private boolean closed=false;
		
		public DefaultChemicalReader(ChemicalImplReader delegate) throws IOException {
			this.delegate = delegate;
			updateNext();
		}

		private void updateNext() throws IOException{
			next = delegate.read();
		}
		@Override
		public void close() throws IOException {
			closed=true;
			delegate.close();
		}

		@Override
		public boolean canRead() {
			return !closed && next !=null;
		}

		@Override
		public Chemical read() throws IOException {
			if(closed){
				throw new IOException("already closed");
			}
			Chemical ret = new Chemical(next, next.getSource());
			updateNext();
			
			return ret;
		}

		
		
		
		
		
	}

	
}

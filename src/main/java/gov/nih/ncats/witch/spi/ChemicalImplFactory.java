/*
 * NCATS-WITCH
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

package gov.nih.ncats.witch.spi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import gov.nih.ncats.witch.Atom;
import gov.nih.ncats.common.io.InputStreamSupplier;

/**
 * Factory class to create {@link ChemicalImpl}s instances.
 * 
 * @author katzelda
 *
 */
public interface ChemicalImplFactory {
	/**
	 * Create a {@link ChemicalImpl} from the given SMILES string.
	 * 
	 * @param smiles the SMILES string, will never be null.
	 * 
	 * @return a new {@link ChemicalImpl}, will never be null.
	 * 
	 * @throws IOException if there is a problem parsing the SMILES.
	 */
	ChemicalImpl createFromSmiles(String smiles) throws IOException;
	/**
	 * Parse the given data as bytes and create all the {@link ChemicalImpl}s encoded inside it.
	 * This could be bytes from a file or bytes from a String encoding.
	 * If it's from a file, the file format is not provided so it could be any of the
	 * standard cheminformatics file formats.
	 * 
	 * @param bytes the byte array to parse.
	 * @param start the start offset into the array to start parsing; may be {@code 0}.
	 * @param length the number of bytes to read from the array.
	 * 
	 * 
	 * @return a List of {@link ChemicalImpl}, will never be null, and shouldn't be empty.
	 * 
	 * @throws IOException if there is a problem parsing the bytes.
	 */
	ChemicalImplReader create(byte[] bytes, int start, int length) throws IOException;
	/**
	 * Parse the given file and create all the {@link ChemicalImpl}s encoded inside it.
	 * The file format of the file is not provided so it could be any of the
	 * standard cheminformatics file formats.
	 * 
	 * @param file the {@link File} to parse.
	 * 
	 * @return a List of {@link ChemicalImpl}, will never be null, and shouldn't be empty.
	 * 
	 * @throws IOException if there is a problem parsing the file.
	 */
	ChemicalImplReader create(File file) throws IOException;
	/**
	 * Parse the given {@link InputStream} and create all the {@link ChemicalImpl}s encoded inside it.
	 * The file format of the streamed file is not provided so it could be any of the
	 * standard cheminformatics file formats.
	 * 
	 * @param format the format the file is encoded in.
	 * 
	 * @param in the {@link InputStream} to parse.
	 * 
	 * @return a List of {@link ChemicalImpl}, will never be null, and shouldn't be empty.
	 * 
	 * @throws IOException if there is a problem parsing the InputStream.
	 */
	ChemicalImplReader create(String format, InputStream in) throws IOException;
	
	/**
	 * Parse the given file and create all the {@link ChemicalImpl}s encoded inside it.
	 * The file format of the file is not provided so it could be any of the
	 * standard cheminformatics file formats.
	 * 
	 * @param format the format the file is encoded in.
	 * 
	 * @param file the {@link File} to parse.
	 * 
	 * @return a List of {@link ChemicalImpl}, will never be null, and shouldn't be empty.
	 * 
	 * @throws IOException if there is a problem parsing the file.
	 */
	ChemicalImplReader create(String format, File file) throws IOException;
	
	/**
	 * Parse the given {@link InputStreamSupplier} and create all the {@link ChemicalImpl}s encoded inside it.
	 * 
	 * 
	 * @param format the format the file is encoded in.
	 * 
	 * @param in the {@link InputStreamSupplier} to parse.
	 * 
	 * @return a List of {@link ChemicalImpl}, will never be null, and shouldn't be empty.
	 * 
	 * @throws IOException if there is a problem parsing the InputStream.
	 */
	ChemicalImplReader create(String format, InputStreamSupplier in) throws IOException;
	
	/**
	 * Parse the given {@link InputStreamSupplier} and create all the {@link ChemicalImpl}s encoded inside it.
	 * The file format of the streamed file is not provided so it could be any of the
	 * standard cheminformatics file formats.
	 * 
	 * @param in the {@link InputStreamSupplier} to parse.
	 * 
	 * @return a List of {@link ChemicalImpl}, will never be null, and shouldn't be empty.
	 * 
	 * @throws IOException if there is a problem parsing the InputStream.
	 */
	ChemicalImplReader create(InputStreamSupplier in) throws IOException;
	/**
	 * Parse the given {@link InputStream} and create all the {@link ChemicalImpl}s encoded inside it.
	 * The file format of the streamed file is not provided so it could be any of the
	 * standard cheminformatics file formats.
	 * 
	 * @param in the {@link InputStream} to parse.
	 * 
	 * @return a List of {@link ChemicalImpl}, will never be null, and shouldn't be empty.
	 * 
	 * @throws IOException if there is a problem parsing the InputStream.
	 */
	ChemicalImplReader create(InputStream in) throws IOException;
	/**
	 * Create a new {@link ChemicalImpl} object
	 * that is doesn't have any atoms
	 * or bonds.  Clients can then
	 * add atoms and bonds later.
	 * 
	 * @return a new {@link ChemicalImpl} object;
	 * will never be null.
	 */
	ChemicalImpl createNewEmptyChemical();
	boolean supports(String format);
	ChemicalImpl createFromSmarts(String smarts) throws IOException;
	boolean isDefault();
	
}

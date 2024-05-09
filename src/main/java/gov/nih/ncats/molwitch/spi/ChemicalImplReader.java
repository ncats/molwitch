/*
 * NCATS-MOLWITCH
 *
 * Copyright 2024 NIH/NCATS
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

package gov.nih.ncats.molwitch.spi;

import java.io.Closeable;
import java.io.IOException;

import gov.nih.ncats.molwitch.io.ChemicalReader;
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

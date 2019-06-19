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

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

package gov.nih.ncats.molwitch.internal;

import java.io.IOException;
import java.util.Objects;

import uk.ac.cam.ch.wwmm.opsin.NameToStructure;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult.OPSIN_RESULT_STATUS;
/**
 * Wrapper class around OPSIN to handle
 * converting chemical names to structures.
 * 
 * @author katzelda
 *
 */
public final class OpsinHelper {

	private static final NameToStructure nts = NameToStructure.getInstance();
	/**
	 * Parse the given name and return the structure 
	 * as a smiles.
	 * @param name the name to parse; can not be null.
	 * 
	 * @return the smiles will never be null.
	 * 
	 * @throws IOException if there was a problem parsing name.
	 * @throws NullPointerException if name is null.
	 */
	public static String parseToSmiles(String name) throws IOException{
		OpsinResult result= nts.parseChemicalName(Objects.requireNonNull(name));

		if(OPSIN_RESULT_STATUS.FAILURE == result.getStatus()){
			throw new IOException(result.getMessage());
		}
		String smiles = result.getSmiles();
		if(smiles ==null){
			throw new IOException("could not generate smiles for '"+name +"' but did not cause opsin failure");
		}
		return smiles;
		
	}
}

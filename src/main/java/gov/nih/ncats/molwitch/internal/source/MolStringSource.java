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

package gov.nih.ncats.molwitch.internal.source;

import java.io.IOException;
import java.util.Map;

import gov.nih.ncats.molwitch.ChemicalSource;


public class MolStringSource extends StringSource{

	public MolStringSource(String data) {
		super(data, Type.MOL,false);
		extractProperties(data);
	}
	public MolStringSource(String data, Type type) {
		super(data, type,false);
		extractProperties(data);
	}

	private void extractProperties(String data){
		
		Map<String,String> props = getProperties();
		try {
			MolFileInfo info = MolFileInfo.parseFrom(data);
			props.put(ChemicalSource.CommonProperties.Version, info.getVersion().toString());
			props.put(ChemicalSource.CommonProperties.Name, info.getName());
			
			props.put("#atoms", Integer.toString(info.getNumberOfAtoms()));
			props.put("#bonds", Integer.toString(info.getNumberOfBonds()));
			props.put("#aromaticBonds", Integer.toString(info.getNumberAromaticBonds()));
			props.put("#doubleBonds", Integer.toString(info.getNumberDoubleBonds()));
			props.put("#singleBonds", Integer.toString(info.getNumberSingleBonds()));
		}catch(IOException e) {
			props.put(ChemicalSource.CommonProperties.ProcessingException, e.getMessage());
			//throw new UncheckedIOException(e);
		}
		
		
	}

}

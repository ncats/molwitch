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

package gov.nih.ncats.molwitch;

import java.util.Map;

public interface ChemicalSource {

	public enum Type{
		SMILES,
		MOL{
			@Override
			public boolean includesCoordinates() {
				return true;
			}
			
		},
		SDF{
			@Override
			public boolean includesCoordinates() {
				return true;
			}
		},
		INCHI,
		SMARTS;
		
		public static Type parseType(String format){
			switch(format){
			case "cxsmiles" :
			case "smiles" :return Type.SMILES;
			case "sdf" : return Type.SDF;
			case "mol" : return Type.MOL;
			case "inchi" : return Type.INCHI;
			case "smarts" : return Type.SMARTS;
			}
			if(format.startsWith("mol")) {
				return Type.MOL;
			}
			throw new IllegalArgumentException("unknown format: " + format);
		}
		
		public boolean includesCoordinates() {
			return false;
		}
	}
	
	Type getType();
	
	
	String getData();
	
	
	Map<String,String> getProperties();
	
	public static final class CommonProperties{
		private CommonProperties() {
			//can not instantiate
		}
		
		public static final String Filename = "Filename";
		public static final String Filepath = "Filepath";
		public static final String Filesize = "Filesize";
		public static final String Version = "Version";
		public static final String Name = "Name";
		
		public static final String ProcessingException = "ProcessingException";
		
		
	}
	
	
}

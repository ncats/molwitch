/*
 * NCATS-MOLWITCH
 *
 * Copyright 2025 NIH/NCATS
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nih.ncats.molwitch.io.ChemFormat.MolFormatSpecification.Version;
/**
 * Parses some information from a Mol file
 * since different implementations write out
 * some things like order of single vs double bonds
 * differently and we don't care about most 
 * of the other fields in the file.
 * It's easier to just parse what we 
 * care about and get counts for other things.
 * 
 * @author katzelda
 *
 */
public class MolFileInfo {

	private String name;
	private Version version;
	
	private int numberOfAtoms;
	private int numberOfBonds;
	
	private int numberSingleBonds;
	private int numberDoubleBonds;
	private int numberAromaticBonds;
	//aaabbblllfffcccsssxxxrrrpppiiimmmvvvvv
	//a = # atoms 255 max
	//b= #bonds 255 max
	//l = #atom lists (max 30)
	//f = obsolete
	//c = chiral flag = 0 = not chiral 1 = chiral
	//s = number of stext entries
	//x = obsolete
	//r = obsolete
	//p = obsolete
	//ii = obsolete
	//m = number of lines of additional properties includeing the mEND line no longer supported defaults to 999
			
	
	private static Pattern HEADER_PATTERN = Pattern.compile("^\\s*(\\d+)\\s+(\\d+).+V(\\d{4})$");
	private static Pattern BOND_PATTERN = Pattern.compile("^\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
	
	private static Pattern V3_BOND_PATTERN = Pattern.compile("M\\s+V30\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
	
	public static MolFileInfo parseFrom(String mol) throws IOException{
		try(BufferedReader reader = new BufferedReader(new StringReader(mol))){
			return parse(reader);
		}
	}
	public static MolFileInfo parseFrom(InputStream in) throws IOException{
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(in))){
			return parse(reader);
		}
	}
	private static MolFileInfo parse(BufferedReader reader) throws IOException {
		MolFileInfo info = new MolFileInfo();
		
		info.name = reader.readLine().trim();
		
		//skip next 2 lines
		reader.readLine();
		reader.readLine();
		
		String headerLine = reader.readLine();
		if(headerLine.endsWith("V3000")){
			info.version = Version.V3000;
			parseV3000(info, reader);
		}else{
			info.version =  Version.V2000;
			parseV2000(info, reader, headerLine);
		}
		
		//each atom lists the bond so there are 2x the number of bonds
		//take the ceiling because there could be extra bonds outside the aromratic rings?
		
		//C-N-C
		info.numberSingleBonds = (int)Math.ceil(info.numberSingleBonds/2D);
		info.numberDoubleBonds = (int)Math.ceil(info.numberDoubleBonds/2D);
		info.numberAromaticBonds = (int)Math.ceil(info.numberAromaticBonds/2D);
		
		
		
		return info;
	}
	private static void parseV2000(MolFileInfo info, BufferedReader reader, String headerLine) throws IOException {
		Matcher headerMatcher = HEADER_PATTERN.matcher(headerLine);
		if(!headerMatcher.matches()){
			throw new IOException("invalid mol header: '" + headerLine +"'");
		}
		info.numberOfAtoms = Integer.parseInt(headerMatcher.group(1));
		info.numberOfBonds = Integer.parseInt(headerMatcher.group(2));
		info.version = Version.parse(headerMatcher.group(3));
		
		//for now just skip the atom lines...
		for(int i=0; i< info.numberOfAtoms; i++){
			reader.readLine();
		}
		for(int i=0; i< info.numberOfBonds; i++){				
			String line = reader.readLine();
			Matcher matcher = BOND_PATTERN.matcher(line);
			if(!matcher.find()){
				throw new IOException("invalid bond line '" + line + "'");					
			}
			
			int bondType = Integer.parseInt(matcher.group(3));
			switch(bondType){
				case 1 : info.numberSingleBonds++; break;
				case 2 : info.numberDoubleBonds++; break;
				case 4 : info.numberAromaticBonds++; break;
				default: //no op
			}
		}
	}
	
	private static MolFileInfo parseV3000(MolFileInfo info, BufferedReader reader) throws IOException {
		Pattern countPattern = Pattern.compile("M\\s+V30\\s+COUNTS\\s+(\\d+)\\s+(\\d+)");
		Matcher matcher;
		do{
			matcher = countPattern.matcher(reader.readLine());
		}while(!matcher.find());
		info.numberOfAtoms = Integer.parseInt(matcher.group(1));
		info.numberOfBonds = Integer.parseInt(matcher.group(2));

		//skip over atom stuff
		while(!"M  V30 BEGIN BOND".equals(reader.readLine())){
			//keep looping
		}
		for(int i=0; i< info.numberOfBonds; i++){
			String line = reader.readLine();
			Matcher bondMatcher = V3_BOND_PATTERN.matcher(line);
			if(!bondMatcher.find()){
				throw new IOException("invalid bond line '" + line + "'");					
			}
			
			int bondType = Integer.parseInt(bondMatcher.group(2));
			switch(bondType){
				case 1 : info.numberSingleBonds++; break;
				case 2 : info.numberDoubleBonds++; break;
				case 4 : info.numberAromaticBonds++; break;
				default: //no op
			}
		}
		return info;
	}
	private MolFileInfo(){
		
	}
	
	public String getName() {
		return name;
	}

	public Version getVersion() {
		return version;
	}
	public int getNumberOfAtoms() {
		return numberOfAtoms;
	}
	public int getNumberOfBonds() {
		return numberOfBonds;
	}
	public int getNumberSingleBonds() {
		return numberSingleBonds;
	}
	public int getNumberDoubleBonds() {
		return numberDoubleBonds;
	}
	public int getNumberAromaticBonds() {
		return numberAromaticBonds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + numberAromaticBonds;
		result = prime * result + numberDoubleBonds;
		result = prime * result + numberOfAtoms;
		result = prime * result + numberOfBonds;
		result = prime * result + numberSingleBonds;
		result = prime * result + version.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MolFileInfo other = (MolFileInfo) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (numberAromaticBonds != other.numberAromaticBonds)
			return false;
		if (numberDoubleBonds != other.numberDoubleBonds)
			return false;
		if (numberOfAtoms != other.numberOfAtoms)
			return false;
		if (numberOfBonds != other.numberOfBonds)
			return false;
		if (numberSingleBonds != other.numberSingleBonds)
			return false;
		if (version != other.version)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "MolFileInfo [name=" + name + ", version=" + version + ", numberOfAtoms=" + numberOfAtoms
				+ ", numberOfBonds=" + numberOfBonds + ", numberSingleBonds=" + numberSingleBonds
				+ ", numberDoubleBonds=" + numberDoubleBonds + ", numberAromaticBonds=" + numberAromaticBonds + "]";
	}
	
	
	
}

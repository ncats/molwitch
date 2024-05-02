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

package gov.nih.ncats.molwitch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
public interface SGroup {

    enum SGroupConnectivity{
		HEAD_TO_HEAD("HH"),
		HEAD_TO_TAIL("HT"),
		EITHER_UNKNOWN("EU")
		;
		private static final Map<String, SGroupConnectivity> map;
		static {
			map = new HashMap<>();
			for(SGroupConnectivity sgc : values()) {
				map.put(sgc.type, sgc);
			}
		}
		private final String type;
		SGroupConnectivity(String type) {
			this.type = type;
		}
		
		public static SGroupConnectivity parse(String value) {
			return map.get(value);
		}
	}
	
	enum PolymerSubType{
		//ALT = alternating, RAN = random, BLO = block
		ALTERNATING("ALT"),
		RANDOM("RAN"),
		BLOCK("BLO");
		
		private String subtypeCode;
		
		private static Map<String, PolymerSubType> map;
		static {
			map = new HashMap<>();
			for(PolymerSubType t : values()) {
				map.put(t.subtypeCode, t);
			}
		}
		private PolymerSubType(String code) {
			this.subtypeCode = code;
		}
		
		public String getCode() {
			return subtypeCode;
		}
		
		public static PolymerSubType valueByCode(String code) {
			return map.get(code);
		}
	}
	public enum SGroupType{
	
		
		GENERIC("GEN"),
		COMPONENT("COM"),
		COPOLOYMER("COP"),
		CROSSLINK("CRO"),
		DATA("DAT"),
		FORMULATION("FOR"),
		MULTIPLE("MUL"),
		MONOMER("MON"),
		SRU("SRU"),
		SUPERATOM_OR_ABBREVIATION("SUP"), 
		ANYPOLYMER("ANY"),
		GRAFT("GRA"),
		MIXTURE("MIX"),
		MER("MER"),
		MODIFICATION("MOD")
		;
		
		private static final Map<String, SGroupType> map;
		
		static {
			map = new HashMap<>();
			for(SGroupType t : values()) {
				map.put(t.typeName, t);
			}
		}
		private String typeName;
		
		SGroupType(String typeName){
			this.typeName = typeName;
		}
		
		public String getTypeName() {
			return typeName;
		}
		
		public static SGroupType valueByTypeName(String typeName) {
			return map.get(typeName);
		}
	}
	
	SGroupType getType();
	
	Stream<Atom> getAtoms();
	Stream<Bond> getBonds();
	Stream<Atom> getOutsideNeighbors();
	
	Stream<SGroup> getParentHierarchy();
	
	SGroupConnectivity getConnectivity();
	
	void addAtom(Atom a);
	void addBond(Bond b);
	
	void removeAtom(Atom a);
	void removeBond(Bond b);
	
	PolymerSubType getPolymerSubType();
	
	void setPolymerSubType(PolymerSubType polymerSubtype);
	
	boolean hasBrackets();
	
	List<SGroupBracket> getBrackets();
	Optional<String> getSruLabel();
	interface SGroupBracket{
		AtomCoordinates getPoint1();
		AtomCoordinates getPoint2();
		//orientation?
		//shape ?
	}
	
	boolean bracketsSupported();
	Optional<String> getSubscript();
	Optional<String> getSuperscript();

	Optional<String> getSuperatomLabel();
	boolean bracketsTrusted();
}

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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import gov.nih.ncats.molwitch.Bond.BondType;
/**
 * Object representation of a single atom.
 * 
 * @author katzelda
 *
 */
public interface Atom {
	/**
	 * Get the atomic number of this atom.
	 * 
	 * @return the atomic number; should always be &ge; 1.
	 */
	int getAtomicNumber();
	/**
	 * Get the symbol for this atom, for example
	 * for Hydrogen, this method will return "H".
	 * 
	 * @return the symbol; will never be null.
	 */
	String getSymbol();
	/**
	 * Get all the {@link Bond}s of this atom.
	 * 
	 * @return the list of Bonds.  If there are 
	 * no bonds, then this method will return an empty list.
	 * Will never be null, nor will any elements in the list be null.
	 */
	List<? extends Bond> getBonds();
	/**
	 * Does this atom have any aromatic bonds.
	 * 
	 * @return {@code true} if it does; {@code false} otherwise.
	 */
	boolean hasAromaticBond();
	/**
	 * Get the charge for this atom.
	 * @return the charge value.
	 */
	int getCharge();
	
	AtomCoordinates getAtomCoordinates();
	void setAtomCoordinates(AtomCoordinates atomCoordinates);
	
	Chirality getChirality();
	
	double getExactMass();
	/**
	 * Gets the atom mass, if is specific isotope, otherwise 0 for natural abundance
	 * @return {@code 0} if the atom represents
	 * the generic element with the isotopes in the
	 * natural abundance, or a positive integer for the mass
	 * of the specific isotope.
	 */
	int getMassNumber();
	/**
	 * Sets the charge for this atom.
	 * 
	 * @param charge the formal charge for this atom; may be
	 * positive, or negative or zero.
	 * 
	 */
	void setCharge(int charge);
	/**
	 * Sets the atom mass, usually only called for isotopes.
	 *  
	 * @param mass if this is specific isotope, then the
	 * value should be the mass of this isotope.
	 * If this value is {@code 0}, then the mass
	 * is the natural abundance
	 * 
	 * @throws IllegalArgumentException if mass is negative.
	 */
	void setMassNumber(int mass);
	
	int getImplicitHCount();
	
	int getRadicalValue();
	
	boolean isInRing();
	
	/**
	 * Get the total number of bonds.  This is the same
	 * as {@code getBonds().size()}.
	 * @return the number of bonds as an int will always be &ge; 0.
	 */
	default int getBondCount(){
		return getBonds().size();
	}
	/**
	 * Get the number of bonds on this atom that are the 
	 * specified type.
	 * @param type the {@link BondType} to count; can not be null.
	 * 
	 * @return the number of bonds as an int will always be &ge; 0.
	 * 
	 * @throws NullPointerException if type is null.
	 */
	default int getBondCount(BondType type){
		Objects.requireNonNull(type);
		int count=0;
		for(Bond b : getBonds()){
			if(type.equals(b.getBondType())){
				count++;
			}
		}
		return count;
	}
	OptionalInt getValence();

	boolean hasValenceError();
	/**
	 * Get the Atoms that this atom
	 * is bonded to.
	 * 
	 * @return
	 */
	default List<Atom> getNeighbors(){
		return getBonds().stream()
					.map(b-> b.getOtherAtom(this))
					.collect(Collectors.toList());
		
	}
	
	boolean isIsotope();
	
	boolean isQueryAtom();
	
	boolean isRGroupAtom();
	OptionalInt getRGroupIndex();
	
	void setRGroup(Integer rGroup);
	
	OptionalInt getAtomToAtomMap();
	/**
	 * set the atom to atom map value a value of 0
	 * means no mapping set.
	 * @param value if 0 then no map otherwise gt; 0 means map is set
	 * to 1-based atom index ?
	 */
	void setAtomToAtomMap(int value);
	/**
	 * Default implementation same as {@link #setAtomToAtomMap(int) setAtomToAtomMap(0)}
	 */
	default void clearAtomToAtomMap() {
		setAtomToAtomMap(0);
	}
	default Optional<? extends Bond> bondTo(Atom other){
		Objects.requireNonNull(other);
		return getBonds().stream()
				.filter(b->b.getOtherAtom(this).equals(other))
				.findFirst();
	}
	int getSmallestRingSize();
	
	int getAtomIndexInParent();
	
	Optional<String> getAlias();
	
	void setAlias(String alias);
	
	default boolean hasAtomToAtomMap() {
		return getAtomToAtomMap().isPresent();
	}
	
	void setImplicitHCount(Integer implicitH);
	
	boolean isValidAtomicSymbol();
}

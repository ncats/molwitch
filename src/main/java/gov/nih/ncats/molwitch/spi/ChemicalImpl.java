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

package gov.nih.ncats.molwitch.spi;

import gov.nih.ncats.molwitch.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import gov.nih.ncats.molwitch.Bond.BondType;
import gov.nih.ncats.molwitch.SGroup.SGroupType;
import gov.nih.ncats.molwitch.isotopes.Isotope;

/**
 * The actual interface that wraps the SPI implementation of a chemical (or Molecule).
 * 
 * @author katzelda
 *
 */
public interface ChemicalImpl<T extends ChemicalImpl<T>> {
	/**
	 * Get the name of this Chemical.
	 * @return the name as a String.
	 */
	String getName();
	/**
	 * Sets the name.
	 * 
	 * @param name the new name to use.
	 */
	void setName(String name);
	/**
	 * Get the total mass of this chemical.
	 * @return the mass as a double; should always be &ge; 0.
	 */
	double getMass();
	/**
	 * Get the number of {@link Atom}s in the Chemical.
	 * @return the number of atoms in the chemical; should always be &ge; 0.
	 */
	int getAtomCount();
	/**
	 * Get the number of {@link Bond}s in the Chemical.
	 * @return the number of bonds in the chemical; should always be &ge; 0.
	 */
	int getBondCount();
	/**
	 * Get the {@code ith} {@link Atom}.
	 * @param i the index of the atom to get; should always be &ge; 0.
	 * @return the {@link Atom} or {@code null} if the ith atom does not exist.
	 */
	Atom getAtom(int i);
	/**
	 * Aromatize the chemical.  Make any ring structures that can be made aromatic, aromatic.
	 * @see #kekulize()
	 */
	void aromatize();
	/**
	 * Kekulize the chemical.
	 * 
	 * @see #aromatize()
	 */
	void kekulize();
	
	/**
	 * Create a new {@link GraphInvariant} for the atoms in 
	 * the Chemical.
	 * @return a new {@link GraphInvariant}, will never be null.
	 */
	GraphInvariant getGraphInvariant();
	/**
	 * Get the {@code ith} {@link Bond}.
	 * @param i the index of the bond to get; should always be &ge; 0.
	 * @return the {@link Bond} or {@code null} if the ith bond does not exist.
	 */
	Bond getBond(int i);
	/**
	 * Create a {@link BondTable} for the current bonds in the Chemical.
	 * @return a new {@link BondTable}; will never be null.
	 */
	BondTable getBondTable();
	/**
	 * Get the index of the given {@link Atom}.
	 * 
	 * @param a the atom to get the index of.
	 * @return the index as an int.
	 * 
	 */
	int indexOf(Atom a);
	/**
	 * Get the index of the given {@link Bond}.
	 * 
	 * @param b the bond to get the index of.
	 * @return the index as an int.
	 * 
	 */
	int indexOf(Bond b);
	/**
	 * Create a "shallow copy" of this Chemical.
	 * the returned chemical instance should point to a new reference
	 * but contain the same values.  In addition, the actual bonds and Atoms
	 * should be THE SAME REFERENCE as this Chemical such that
	 * {@code this.getAtom(i) == shallowCopy.getAtom(i)}
	 * and 
	 * {@code this.getBond(i) == shallowCopy.getBond(i)}.
	 * 
	 * <p>
	 * Future modifications to this Chemical should not affect its copy and vice versa
	 * (except those that would modify the atoms or bonds).
	 * @return the new {@link ChemicalImpl} object.
	 */
	T shallowCopy();
	
	void makeHydrogensExplicit();
	
	Atom addAtom(String symbol);
	
	Atom addAtomByAtomicNum(int atomicNumber);
	
	Bond addBond(Atom atom1, Atom atom2, BondType type);
	
	List<ExtendedTetrahedralChirality> getExtendedTetrahedrals();
	
	List<TetrahedralChirality> getTetrahedrals();
	List<DoubleBondStereochemistry> getDoubleBondStereochemistry();
	
	void prepareForBuild(PreparationOptions options);
	
	
	String getProperty(String key);
	
	void setProperty(String key, String value);
	
	Iterator<Entry<String,String>> properties();
	
	ChemicalSource getSource();

	void removeProperty(String name);

	int getSGroupCount();

	public static final class PreparationOptions{
		public final boolean aromatize, makeHydrogensExplicit, computeCoords, computeStereo;

		public PreparationOptions(boolean aromatize, boolean makeHydrogensExplicit, boolean computeCoords,
				boolean computeStereo) {
			this.aromatize = aromatize;
			this.makeHydrogensExplicit = makeHydrogensExplicit;
			this.computeCoords = computeCoords;
			this.computeStereo = computeStereo;
		}

		@Override
		public String toString() {
			return "PreparationOptions [aromatize=" + aromatize + ", makeHydrogensExplicit=" + makeHydrogensExplicit
					+ ", computeCoords=" + computeCoords + ", computeStereo=" + computeStereo + "]";
		}
		
	}
	
	/**
	 * Get the wrapped object from the wrapped library that represents
	 * this chemical.  Care should be taken when accessing this object
	 * because any modifications done to this returned object might
	 * corrupt this ChemicalImpl.
	 * <p>
	 * This method should only be called when trying to perform functionality
	 * not yet defined by a MolWitch API call!
	 * </p>
	 * 
	 * 
	 * @return The Object that the underlying wrapped library uses to represent a chemical
	 * must be casted to the appropriate type.
	 */
	public Object getWrappedObject();
	String getFormula();
	void makeHydrogensImplicit();
	
	String getFormula(boolean includeImplicitHydrogen);
	
	boolean hasImplicitHydrogens();
	
	T deepCopy();
	
	Iterator<T> connectedComponents();
	
	boolean hasCoordinates();
	
	boolean has2DCoordinates();
	
	boolean has3DCoordinates();
	int getSmallestRingSize();
	Atom removeAtom(int i);
	Atom removeAtom(Atom a);
	Bond removeBond(int i);
	Bond removeBond(Bond b);
	Bond removeBond(Atom a, Atom b);
	Atom addAtom(Atom a);
	Bond addBond(Bond b);
	Atom addAtom(Isotope isotope);
	
	void addChemical(ChemicalImpl<T> other);
	
	void removeSGroup(SGroup sgroup);
	
	SGroup addSgroup(SGroupType type);
	
	List<SGroup> getSGroups();
	
	boolean hasSGroups();
	void expandSGroups();
	void generateCoordinates() throws ChemkitException;
	void flipChirality(Stereocenter s);
	
	
}

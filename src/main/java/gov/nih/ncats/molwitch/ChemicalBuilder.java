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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nih.ncats.molwitch.Bond.BondType;
import gov.nih.ncats.molwitch.inchi.Inchi;
import gov.nih.ncats.molwitch.internal.OpsinHelper;
import gov.nih.ncats.molwitch.spi.ChemicalImpl;
import gov.nih.ncats.molwitch.spi.ChemicalImplFactory;
import gov.nih.ncats.molwitch.spi.ChemicalImpl.PreparationOptions;
/**
 * 
 * @author katzelda
 *
 */
public class ChemicalBuilder {
	private static Pattern SMILES_PATTERN = Pattern.compile("(\\S+)(\\s+(.+))?");
	
	
	private final ChemicalImpl impl;

	private ChemicalSource source;
	
	private boolean makeHydrogensExplicit;
	private boolean aromatize;
	private boolean computeCoords;
	private boolean computeStereo;
	
	/**
	 * Create a new chemical from its Inchi.
	 * 
	 * @param inchi the Inchi for; can not be null.
	 * 
	 * @return a new Molecule; will never be null.
	 * 
	 * @throws IOException if there is a problem parsing the name
	 * string;
	 * 
	 * @throws NullPointerException if name is null.
	 */
	public static ChemicalBuilder createFromInchi(String inchi) throws IOException{
		return _fromImpl(Inchi.toChemical(inchi).getImpl());
	}
	/**
	 * Look up the name of the chemical by its name
	 * and create a new Chemical object with its structure.
	 * 
	 * @param name the chemical name to get the Chemical for; can not be null.
	 * 
	 * @return a new Molecule; will never be null.
	 * 
	 * @throws IOException if there is a problem parsing the name
	 * string;
	 * 
	 * @throws NullPointerException if name is null.
	 */
	public static ChemicalBuilder createFromName(String name) throws IOException{
		String smiles = OpsinHelper.parseToSmiles(name);
		return createFromSmiles(smiles);
	}
	/**
	 * Utility method for SPIs to easily create a Chemical from
	 * the SPI implementation object.  This method should not be used
	 * by end users and is only exposed as a public method
	 * due to java visibility limitations. 
	 * ONLY USE IF YOU ARE AN SPI
	 * 
	 * @param impl the ChemicalImpl to wrap for this builder.
	 * @return a new Builder instance.
	 */
	public static ChemicalBuilder _fromImpl(ChemicalImpl impl) {
		return new ChemicalBuilder(impl, impl.getSource());
	}
	
	public static ChemicalBuilder createFromSmarts(String smarts) throws IOException{
		ChemicalImplFactory factory = ImplUtil.getChemicalImplFactory();
		
		return _fromImpl(factory.createFromSmarts(smarts));
		
	}
	
	
	/**
	 * Create a new molecule from the given mol
	 * record as a String.
	 * @param mol the mol file to parse;
	 * can not be null.
	 * 
	 * @return a new ChemicalBuilder; will never be null.
	 * 
	 * @throws IOException if there is a problem parsing the mol file.
	 * 
	 * @throws NullPointerException if mol is null.
	 */
	public static ChemicalBuilder createFromMol(File mol) throws IOException{
		return _fromImpl(Chemical.parseMol(mol).getImpl());
	}
	
	/**
	 * Create a new molecule from the given mol
	 * record as a String using the default Charset.
	 * @param rawMol the mol record as a string to parse;
	 * can not be null.
	 * 
	 * @return a new ChemicalBuilder; will never be null.
	 * 
	 * @throws IOException if there is a problem parsing the mol
	 * string;
	 * 
	 * @throws NullPointerException if smiles is null.
	 * @see #createFromMol(String, Charset)
	 * 
	 * <strong>API Note</strong>: this is the same as calling {@code createFromMol(rawMol, Charset.defaultCharset());}.
	 */
	public static ChemicalBuilder createFromMol(String rawMol) throws IOException{
		return createFromMol(rawMol, Charset.defaultCharset());
	}
	/**
	 * Create a new molecule from the given mol
	 * record as a String.
	 * @param rawMol the mol record as a string to parse;
	 * can not be null.
	 * @param charset the {@link Charset} to use to parse 
	 * this mol record.
	 * 
	 * @return a new ChemicalBuilder; will never be null.
	 * 
	 * @throws IOException if there is a problem parsing the mol
	 * string;
	 * 
	 * @throws NullPointerException if smiles is null.
	 */
	public static ChemicalBuilder createFromMol(String rawMol, Charset charset) throws IOException{
		return _fromImpl(Chemical.parseMol(rawMol.getBytes(charset)).getImpl());
	}
	/**
	 * Create a new molecule from the given single SMILES
	 * encoded String.
	 * @param smiles the smiles string to parse;
	 * can not be null.
	 * 
	 * @return a new ChemicalBuilder; will never be null.
	 * 
	 * @throws IOException if there is a problem parsing the SMILES
	 * string;
	 * 
	 * @throws NullPointerException if smiles is null.
	 */
	public static ChemicalBuilder createFromSmiles(String smiles) throws IOException{
		Objects.requireNonNull(smiles);
		
		//some smiles have and id after the smiles string
		Matcher matcher = SMILES_PATTERN.matcher(smiles);
		if(!matcher.find()){
			throw new IOException("invalid smiles line '" + smiles + "'");
		}
		
		//just get first?
		ChemicalImplFactory factory = ImplUtil.getChemicalImplFactory();
		String rawSmiles = matcher.group(1);
		ChemicalBuilder chem= new ChemicalBuilder(factory.createFromSmiles(rawSmiles),  new SmilesSource(rawSmiles));
				
		//set id if present
		String id = matcher.group(3);
		if(id != null){
			chem.setName(id);
		}
		
		return chem;
	}
	/**
	 * Create a new empty {@link Chemical} object
	 * that initially does not have any atoms or bonds.
	 */
	public ChemicalBuilder(){
		ChemicalImplFactory factory = ImplUtil.getChemicalImplFactory();
		this.impl = factory.createNewEmptyChemical();
		this.source = null;
	}
	
	public ChemicalSource getSource() {
		return source;
	}
	public void setSource(ChemicalSource source) {
		this.source = source;
	}
	ChemicalBuilder(ChemicalImpl impl, ChemicalSource source) {
		
		this.impl = Objects.requireNonNull(impl);
		this.source = source;
	}
	/**
	 * Get the bond between the given atom indices.
	 * @param a1 atom 1.
	 * @param a2 atom2.
	 * @return an Bond wrapped in an optional.  If there is no bond
	 * between the 2 atoms, then the optional will be empty; will never be null.
	 * @throws NullPointerException if either parameter is null.
	 */
	public Optional<? extends Bond> getBond(Atom a1, Atom a2){
		int index1 = a1.getAtomIndexInParent();
		int index2 = a2.getAtomIndexInParent();
		//sometimes index returned is -1
		if(index1 < 0 || index2 < 0){
			return Optional.empty();
		}
		return getBond(index1, index2);
	}

	/**
	 * Get the bond between the given atom indices.
	 * @param a1 index of atom 1.
	 * @param a2 index of atom2.
	 * @return an Bond wrapped in an optional.  If there is no bond
	 * between the 2 atoms, then the optional will be empty; will never be null.
	 */
	public Optional<? extends Bond> getBond(int a1, int a2){
		Atom aa1 = getAtom(a1);
		Atom aa2 = getAtom(a2);
		return aa1.bondTo(aa2);
	}
	/**
	 * Get the name of this molecule.
	 * @return the name or null, if no name is given.
	 * 
	 * @see #setName(String)
	 */
	public String getName(){
		return impl.getName();
	}
	/**
	 * Sets the name of this molecule.
	 * 
	 * @param name the name to use,
	 * null values are allowed to unset the name.
	 *
	 * @return this.
	 */
	public ChemicalBuilder setName(String name){
		impl.setName(name);
		
		return this;
	}
	/**
	 * Get the property value with the given key.
	 * 
	 * @param key the key of the key-value pair
	 * to use to look up the property value, can not be null.
	 * @return the property value or null if there is 
	 * no property with that key.
	 * 
	 * @throws NullPointerException if key is null.
	 */
	public String getProperty(String key){
		Objects.requireNonNull(key);
		
		return impl.getProperty(key);
	}
	/**
	 * Set the given property key-value pair.
	 * 
	 * @param key the key to use for this property;
	 * can not be null.
	 * 
	 * @param value the value to use for this property;
	 * can not be null.
	 *
	 * @return this.
	 *
	 * @throws NullPointerException if either the key or value are null.
	 */
	public ChemicalBuilder setProperty(String key, String value){
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
		impl.setProperty(key, value);
		
		return this;
	}
	
	/**
	 * Add a new atom to this molecule with the
	 * given element symbol.
	 * 
	 * 
	 * @param symbol the elemental symbol of the atom;
	 * can not be null.
	 * 
	 * @return the new {@link Atom} that was created.
	 * 
	 * @throws NullPointerException if symbol is null.
	 * 
	 */
	public Atom addAtom(String symbol){
		Objects.requireNonNull(symbol);
		return impl.addAtom(symbol);
	}
	
	public Atom removeAtom(int i){
		return impl.removeAtom(i);
	}
	
	public Atom removeAtom(Atom a){
		return impl.removeAtom(a);
	}
	@SuppressWarnings("unchecked")
	public ChemicalBuilder addChemical(ChemicalBuilder other) {
		impl.addChemical(other.impl);
		return this;
	}
	@SuppressWarnings("unchecked")
	public ChemicalBuilder addChemical(Chemical other) {
		impl.addChemical(other.getImpl());
		return this;
	}
	public Atom addAtom(String symbol, double x, double y){
		return addAtom(symbol, x, y, 0D);
	}
	/**
	 * Add a new atom to this molecule with the
	 * given element symbol and the given coordinates.
	 * <p>
	 * This is the same as :
	 * <pre>
	 * Atom a = addAtom(symbol);
	 * a.setLocation(x, y, z);
	 * return a;
	 * </pre>
	 * 
	 * @param symbol the elemental symbol of the atom;
	 * can not be null.
	 * 
	 * @param x the x coordinate.
	 * @param y the y coordinate.
	 * @param z the z coordinate.
	 * 
	 * 
	 * @return the new {@link Atom} that was created.
	 * 
	 * @throws NullPointerException if symbol is null.
	 * 
	 */
	public Atom addAtom(String symbol, double x, double y, double z) {
		Atom a = addAtom(symbol);
		a.setAtomCoordinates(AtomCoordinates.valueOf(x, y, z));
		return a;
	}
	/**
	 * Add a new atom to this molecule with the
	 * given element atomic number.
	 * 
	 * 
	 * @param atomicNumber the atomic number of the atom
	 * to add; must be &ge; 1.
	 * 
	 * @return the new {@link Atom} that was created.
	 * 
	 * @throws IllegalArgumentException if atomicNumber is less than 1.
	 * 
	 */
	public Atom addAtomByAtomicNum(int atomicNumber){
		if(atomicNumber < 1){
			throw new IllegalArgumentException("atomic number must be >= 1");
		}
		return impl.addAtomByAtomicNum(atomicNumber);
	}
	
	public Bond addBond(Atom atom1, Atom atom2, BondType type) {
		Objects.requireNonNull(atom1);
		Objects.requireNonNull(atom2);
		Objects.requireNonNull(type);
		
		return impl.addBond(atom1, atom2, type);
	}
	
	public ChemicalBuilder makeHydrogensExplicit(boolean makeHydrogensExplicit) {
		this.makeHydrogensExplicit = makeHydrogensExplicit;
		
		return this;		
	}
	
	public ChemicalBuilder computeCoordinates(boolean computeCoords) {
		this.computeCoords = computeCoords;
		
		return this;		
	}
	
	public ChemicalBuilder computeStereo(boolean computeStereo) {
		this.computeStereo = computeStereo;
		
		return this;		
	}
	
	public ChemicalBuilder aromatize(boolean aromatize) {
		this.aromatize = aromatize;
		
		return this;
		
	}
	
	public Chemical build(){
		PreparationOptions options = new PreparationOptions(aromatize, makeHydrogensExplicit, 
															computeCoords, computeStereo);
		
		impl.prepareForBuild(options);
		return new Chemical(impl, source);
	}
//	public ChemicalBuilder flipParity(BondPath bondPath) {
//		bondPath.flipParityFromBuilder(this);
//		return this;
//	}
	public Bond getBond(int i) {
		return impl.getBond(i);		
	}
	public Atom getAtom(int i) {
		return impl.getAtom(i);
	}
	
	
	
}

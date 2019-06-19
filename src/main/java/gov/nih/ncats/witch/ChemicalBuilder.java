package gov.nih.ncats.witch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nih.ncats.witch.Bond.BondType;
import gov.nih.ncats.witch.inchi.Inchi;
import gov.nih.ncats.witch.internal.OpsinHelper;
import gov.nih.ncats.witch.spi.ChemicalImpl;
import gov.nih.ncats.witch.spi.ChemicalImplFactory;
import gov.nih.ncats.witch.spi.ChemicalImpl.PreparationOptions;
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
	 * @param name the chemical name to get the Chemical for; can not be null.
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
	 * @apiNote this is the same as calling {@code createFromMol(rawMol, Charset.defaultCharset());}.
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
	public Bond bondAt(int i) {
		return impl.getBond(i);		
	}
	public Atom atomAt(int i) {
		return impl.getAtom(i);
	}
	
	
	
}

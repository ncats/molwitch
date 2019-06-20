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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.NoSuchElementException;

import gov.nih.ncats.molwitch.spi.ChemicalImplFactory;
import gov.nih.ncats.common.functions.IndexedConsumer;
import gov.nih.ncats.molwitch.Bond.BondType;
import gov.nih.ncats.molwitch.SGroup.SGroupType;
import gov.nih.ncats.molwitch.io.ChemFormat.SmilesChemFormat;
import gov.nih.ncats.molwitch.io.ChemFormat.SmilesFormatWriterSpecification;
import gov.nih.ncats.molwitch.io.ChemFormat.ChemFormatWriterSpecification;
import gov.nih.ncats.molwitch.io.ChemFormat.MolFormatSpecification;
import gov.nih.ncats.molwitch.io.ChemFormat.SdfFormatSpecification;
import gov.nih.ncats.molwitch.io.ChemicalReader;
import gov.nih.ncats.molwitch.io.ChemicalReaderFactory;
import gov.nih.ncats.molwitch.io.ChemicalWriter;
import gov.nih.ncats.molwitch.io.ChemicalWriterFactory;
import gov.nih.ncats.molwitch.isotopes.Isotope;
import gov.nih.ncats.molwitch.spi.ChemicalImpl;
/**
 * Represents a Molecule and the contained
 * atoms and bonds.
 * 
 * @author katzelda
 *
 */
public class Chemical {

	
	private static final SdfFormatSpecification DEFAULT_SDF_SPEC = new SdfFormatSpecification();

	private static final MolFormatSpecification DEFAULT_MOL_SPEC = new MolFormatSpecification();

	private static final SmilesFormatWriterSpecification DEFAULT_SMILES_SPEC = SmilesChemFormat.createOptions();
	
	private final ChemicalImpl impl;

	private final ChemicalSource source;
	/**
	 * Parse the given mol record byte array into a single {@link Chemical} object.
	 * 
	 * @param bytes the byte array containing the mol record
	 * @param start the offset into the array where the mol record starts.
	 * @param length the number of bytes the mol record is long.
	 * @return a new Chemical object will never be null but may contain 0 atoms.
	 * @throws IOException if there is a problem parsing the mol record.
	 */
	public static Chemical parseMol(byte[] bytes, int start, int length) throws IOException {
		try(ChemicalReader reader = ChemicalReaderFactory.newReader(bytes,start, length)){
			return reader.read();
		}
	}
	/**
	 * Parse a single Chemical from the given mol record provided 
	 * as an InputStream.  Warning: this input is not closed
	 * so the caller needs to make sure they close the stream
	 * after calling this method, preferably in a finally block.
	 * 
	 * @param in the inputstream to read; can not be null.
	 * @return a new Chemical object will never be null but may contain 0 atoms.
	 * @throws IOException if there is a problem parsing the mol record.
	 * @throws NullPointerException if in is null.
	 */
	public static Chemical parseMol(InputStream in)  throws IOException {
		try(ChemicalReader reader = ChemicalReaderFactory.newReader(in)){
			return reader.read();
		}
	}
	
	public static Chemical parse(String input) throws IOException{
		//check for smarts
		if(new BufferedReader(new StringReader(input.trim())).lines().count() == 1){
			//only 1 line assume smarts query?
			
			if(input.indexOf('~') > -1 || input.indexOf('*') > -1){
				//has wildcards
				return Chemical.createFromSmarts(input);
			}
			return Chemical.createFromSmiles(input);
		}
		return parseMol(input);
		
	}
	/**
	 * Parse the given mol formatted String into a single {@link Chemical} object.
	 * This is the same as calling {@link #parseMol(byte[]) parseMol(mol.getBytes())}
	 * 
	 * @see #parseMol(byte[], int, int)
	 * 
	 * @param mol the String containing a mol formatted record
	 * @return a new Chemical object will never be null but may contain 0 atoms.
	 * @throws IOException if there is a problem parsing the mol record.
	 */
	public static Chemical parseMol(String mol) throws IOException {
		return parseMol(mol.getBytes());
	}
	/**
	 * Parse the given mol record byte array into a single {@link Chemical} object.
	 * This is the same as calling {@link #parseMol(byte[], int, int) parseMol(bytes, 0, bytes.length}
	 * 
	 * @see #parseMol(byte[], int, int)
	 * 
	 * @param bytes the byte array containing the mol record
	 * @return a new Chemical object will never be null but may contain 0 atoms.
	 * @throws IOException if there is a problem parsing the mol record.
	 */
	public static Chemical parseMol(byte[] bytes) throws IOException {
		return parseMol(bytes, 0, bytes.length);
	}
	/**
	 * Parse a mol file into a Chemical object.
	 * @param mol the mol file to parse.
	 * @return a new Chemical object will never be null but may contain 0 atoms.
	 * @throws IOException if there is a problem parsing the mol file.
	 */
	public static Chemical parseMol(File mol) throws IOException {
		try(ChemicalReader reader = ChemicalReaderFactory.newReader(mol)){
			return reader.read();
		}
	}

	/**
	 * Create a new {@link Chemical} from the given single SMILES
	 * encoded String and compute the coordinates of all the atoms.
	 * @param smiles the smiles string to parse;
	 * can not be null.
	 * 
	 * @return a new Chemical; will never be null.
	 * 
	 * @throws IOException if there is a problem parsing the SMILES
	 * string;
	 * 
	 * @throws NullPointerException if smiles is null.
	 */
	public static Chemical createFromSmilesAndComputeCoordinates(String smiles) throws IOException{
		
		ChemicalBuilder chem= ChemicalBuilder.createFromSmiles(smiles);
//		chem.aromatize(true);
		chem.computeCoordinates(true);
		return chem.build();
	}

	/**
	 * Create a new {@link Chemical} from the given single SMILES
	 * encoded String and do not compute the coordinates.
	 * @param smiles the smiles string to parse;
	 * can not be null.
	 * 
	 * @return a new Chemical; will never be null.
	 * 
	 * @throws IOException if there is a problem parsing the SMILES
	 * string;
	 * 
	 * @throws NullPointerException if smiles is null.
	 * @see #createFromSmilesAndComputeCoordinates(String)
	 */
	public static Chemical createFromSmiles(String smiles) throws IOException{
		
		ChemicalBuilder chem= ChemicalBuilder.createFromSmiles(smiles);
//		chem.aromatize(true);
//		chem.computeCoordinates(true);
		return chem.build();
	}
	
	/**
	 * Create a new {@link Chemical} from the given single SMILES
	 * encoded String and do not compute the coordinates.
	 * @param smiles the smiles string to parse;
	 * can not be null.
	 * 
	 * @return a new Chemical; will never be null.
	 * 
	 * @throws IOException if there is a problem parsing the SMILES
	 * string;
	 * 
	 * @throws NullPointerException if smiles is null.
	 * @see #createFromSmilesAndComputeCoordinates(String)
	 */
	public static Chemical createFromSmarts(String smarts) throws IOException{
		ChemicalImplFactory chemicalImplFactory = ImplUtil.getChemicalImplFactory();
		return new Chemical(chemicalImplFactory.createFromSmarts(smarts),
				new SmartsSource(smarts));
	}
	
	/**
	 * Create a new empty {@link Chemical} object
	 * that initially does not have any atoms or bonds.
	 */
	public Chemical(){
		ChemicalImplFactory factory = ImplUtil.getChemicalImplFactory();
		this.impl = factory.createNewEmptyChemical();
		this.source= null;
	}
	public Chemical(ChemicalImpl impl){
		this(impl, impl.getSource());
	}
	public Chemical(ChemicalImpl impl, ChemicalSource source) {
		Objects.requireNonNull(impl);
		this.impl = impl;
		this.source = source;
	}
	public Stream<Chemical> connectedComponentsAsStream(){
		return StreamSupport.stream(
		          Spliterators.spliteratorUnknownSize(connectedComponents(), Spliterator.ORDERED),
		          false);
	}
	public Iterator<Chemical> connectedComponents(){
		Iterator<ChemicalImpl<?>> iter = impl.connectedComponents();
		return new Iterator<Chemical>() {
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public Chemical next() {
				return new Chemical(iter.next());
			}
			
		};
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
	 */
	public void setName(String name){
		impl.setName(name);
	}
	/**
	 * Convenience method for {@link #getProperty(String, boolean) getProperty(key ,false)}.
	 * @param key the key of the property to lookup.
	 *
	 * @return the property value.
	 * @see #getProperty(String, boolean)
	 */
	public String getProperty(String key){		
		return getProperty(key ,false);
	}
	
	/**
	 * Get the property value with the given key.
	 * 
	 * @param key the key of the key-value pair
	 * to use to look up the property value, can not be null.
	 * 
	 * @param removeNewLines should inner new lines be removed
	 * from the value.
	 * 
	 * @return the property value or null if there is 
	 * no property with that key.
	 * 
	 * @throws NullPointerException if key is null.
	 */
	public String getProperty(String key, boolean removeNewLines){
		Objects.requireNonNull(key);
		
		String value= impl.getProperty(key);
		if(value ==null){
			return null;
		}
		if(removeNewLines){
			StringBuilder builder = new StringBuilder();
			try(BufferedReader reader = new BufferedReader(new StringReader(value))){
				String line;
				while(  (line= reader.readLine()) !=null){
					builder.append(line);
				}
				return builder.toString();
			} catch (IOException e) {
				//this shouldn't happen since we are reading from String
				throw new IllegalStateException("error parsing string");
			}
		}
		
		return value;
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
	 * @throws NullPointerException if either the key or value are null.
	 */
	public void setProperty(String key, String value){
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
		impl.setProperty(key, value);
	}
	/**
	 * Get the total mass of this molecule.
	 * 
	 * 
	 * @return the mass as a double.
	 */
	public double getMass(){
		return impl.getMass();
	}
	/**
	 * Get the number of atoms currently in the molecule.
	 * @return the number of atoms.
	 */
	public int getAtomCount(){
		return impl.getAtomCount();
	}
	/**
	 * Get the number of bonds currently in the molecule.
	 * @return the number of bonds.
	 */
	public int getBondCount(){
		return impl.getBondCount();
	}
	/**
	 * Get all the properties as a set of key-value pairs.
	 * 
	 * @return the Set of properties; will never be null,
	 * but may be empty if no properties are set.
	 */
	public Map<String, String> getProperties(){
		Iterator<Entry<String, String>> iter = impl.properties();
		Map<String, String> set = new HashMap<>();
		while(iter.hasNext()){
			Entry<String, String> next = iter.next();
			set.put(next.getKey(), next.getValue());
		}
		return set;
	}
	
	/**
	 * Get all the properties as a set of key-value pairs.
	 * 
	 * @return the Set of properties; will never be null,
	 * but may be empty if no properties are set.
	 */
	public Iterator<Entry<String, String>> getPropertyIterator(){
		return impl.properties();
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
	public Atom addAtom(Isotope isotope) {
		return impl.addAtom(isotope);
	}
	public Atom addAtom(Atom a){
		Objects.requireNonNull(a);
		return impl.addAtom(a);
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
	
	/**
	 * Gets the ith {@link Atom} in this molecule.
	 * 
	 * @param i the ith atom to get.
	 * @return the {@link Atom} or {@code null}.
	 */
	public Atom getAtom(int i){
		return impl.getAtom(i);
	}
	/**
	 * Get this index of the given {@link Atom}.
	 * @param a the atom to look up; can not be null.
	 * 
	 * @return the index of this atom in the molecule.
	 * 
	 * @throws NullPointerException if a is null.
	 */
	public int indexOf(Atom a){
		Objects.requireNonNull(a);
		
		return impl.indexOf(a);
	}
	/**
	 * Gets the ith {@link Bond} in this molecule.
	 * 
	 * @param i the ith atom to get.
	 * @return the {@link Atom} or {@code null}.
	 */
	public Bond getBond(int i){
		return impl.getBond(i);
	}
	/**
	 * Get this index of the given {@link Bond}.
	 * @param b the bond to look up; can not be null.
	 * 
	 * @return the index of this atom in the molecule.
	 * 
	 * @throws NullPointerException if b is null.
	 */
	public int indexOf(Bond b){
		return impl.indexOf(b);
	}
	
	public BondTable getBondTable(){
		return impl.getBondTable();
	}

	public void aromatize(){
		impl.aromatize();
	}
	
	public void kekulize(){
		impl.kekulize();
	}
	
	public GraphInvariant getGraphInvariant(){
		return impl.getGraphInvariant();
	}

	public ChemicalImpl getImpl() {
		return impl;
	}
	
	
	public Chemical copy(){
		return new Chemical(impl.shallowCopy(), this.source);
	}
	
	public byte[] formatToBytes(ChemFormatWriterSpecification spec) throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try(ChemicalWriter writer = ChemicalWriterFactory.newWriter(spec, out)){
			writer.write(this);
		}
		return out.toByteArray();
	}
	private String formatToString(ChemFormatWriterSpecification spec) throws IOException{
		return rightTrim(new String(formatToBytes(spec), "UTF-8"));
	}
	
	public String toSmiles(SmilesFormatWriterSpecification spec) throws IOException{
		return formatToString(spec);
	}
	
	public String toMol(MolFormatSpecification spec) throws IOException{
		return formatToString(spec);
	}
	
	public String toSd(SdfFormatSpecification spec) throws IOException
	{
		return formatToString(spec);
	}

	public String toSmiles() throws IOException
	{
		return toSmiles(DEFAULT_SMILES_SPEC);
	}
	public String toMol() throws IOException
	{
		return toMol(DEFAULT_MOL_SPEC);
	}
	public String toSd() throws IOException
	{
		return toSd(DEFAULT_SDF_SPEC);
	}
	
	public boolean hasQueryAtoms() {
		return atoms().anyMatch(a -> a.isQueryAtom());
	}
	public boolean hasAtomToAtomMappings() {
		return atoms().anyMatch(a -> a.getAtomToAtomMap().isPresent());
		
	}

	private String rightTrim(String string) {
		//can't do a normal trim() because sometimes there is leading whitespace
		//that we want to maintain
		char[] chars = string.toCharArray();
		int i=chars.length -1;
		while(i >=0){
			if(!Character.isWhitespace(chars[i])){
				break;
			}
			i--;
		}
		return new String(chars, 0 , i+1);
	}

	
	
	public List<Bond> computeShortestPath(Atom from, Atom to){
		VisitorFilter filter = new FloydWarshallShortestPathFilter(this);
		SingleVisitor visitor = new SingleVisitor();
		walk(from, to, visitor, filter);
		return visitor.path;
	}
	
	public List<List<Bond>> computeAllPaths(Atom from, Atom to){
		VisitorFilter filter = NullFilter.INSTANCE;
		MultiVisitor visitor = new MultiVisitor();
		walk(from, to, visitor, filter);
		return visitor.paths;
	}
	private void walk(Atom from, Atom to, PathVisitor visitor, VisitorFilter filter){
		int i = indexOf(from);
		int j = indexOf(to);
		
		 BitSet visited = new BitSet (impl.getAtomCount());
		 
		 Stack<Bond> stack = new Stack<>();
		 dfs(visited, stack, i, i, j, visitor, filter);
	}
	
	private void dfs(BitSet visited, Stack<Bond> pathSoFar,  
			int start, int current, int end, PathVisitor visitor, VisitorFilter filter){
		if(current == end){
			visitor.visit(pathSoFar);
			return;
		}
		
		visited.set(current);
		Atom atom = getAtom(current);
		for(Bond bond : atom.getBonds()){
			Atom other = bond.getOtherAtom(atom);
			int o = indexOf(other);
			if(!visited.get(o) && filter.shouldVisit(start, o, end)){
				pathSoFar.push(bond);
				dfs(visited, pathSoFar, start, o, end, visitor, filter);
				pathSoFar.pop();
			}
		}
	}
	
	public void makeHydrogensExplicit() {
		impl.makeHydrogensExplicit();
		
	}
	
	public void makeHydrogensImplicit() {
		impl.makeHydrogensImplicit();
		
	}
	public List<ExtendedTetrahedralChirality> getExtendedTetrahedrals(){
		return impl.getExtendedTetrahedrals();
	}
	public List<TetrahedralChirality> getTetrahedrals(){
		return impl.getTetrahedrals();
	}
	
	public List<Stereocenter> getAllStereocenters(){
		List<Stereocenter>  list= new ArrayList<>();
		list.addAll(getTetrahedrals());
		list.addAll(getExtendedTetrahedrals());
		
		return list;
	}

	public List<DoubleBondStereochemistry> getDoubleBondStereochemistry(){
		return impl.getDoubleBondStereochemistry();
	}
	
	public void flipChirality() {
		for(Stereocenter s : getAllStereocenters()) {
			flipChirality(s);
		}
	}
	
	public void flipChirality(Stereocenter s) {
		impl.flipChirality(s);
	}
	public Optional<ChemicalSource> getSource(){
		return Optional.ofNullable(source);
	}
	
	private interface VisitorFilter{
		boolean shouldVisit(int start, int current, int end);
	}
	
	private static class FloydWarshallShortestPathFilter implements VisitorFilter{

		private final int[][] dist;
		
		FloydWarshallShortestPathFilter(Chemical chem){
			dist = computeFloydWarshallDistance(chem.getBondTable());
		}
		@Override
		public boolean shouldVisit(int start, int current, int end) {
			return (dist[start][current] + dist[current][end]) <= dist[start][end];
		}
		
		private int[][] computeFloydWarshallDistance(BondTable bondTable){
			int length = bondTable.getAtomCount();
			int[][] dist = new int[length][length];
			
			for(int i=0; i< length; i++){
				for(int j= i+1; j< length; j++){
					int d = bondTable.bondExists(i, j) ? 1 : length;
					dist[i][j] =d;
					dist[j][i] = d;
				}
			}
			
			 for (int k = 0; k < length; ++k){
					for (int i = 0; i < length; ++i){ 
					    for (int j = 0; j < length; ++j){
					    	dist[i][j] = Math.min(dist[i][j], dist[i][k]+dist[k][j]);
					    }
					}
			 }
			 return dist;
		}
		
	}
	
	private static class SingleVisitor implements PathVisitor {
		private List<Bond> path;
		@Override
		public void visit(List<Bond> path) {
			//need to make defensive copy since the input list
			//gets mutated later as we keep traversing
			this.path = new ArrayList<>(path);
		}

	}
	
	private static class MultiVisitor implements PathVisitor {
		private List<List<Bond>> paths = new ArrayList<>();
		@Override
		public void visit(List<Bond> path) {
			//need to make defensive copy since the input list
			//gets mutated later as we keep traversing
			this.paths.add(new ArrayList<>(path));
		}

	}
	
	private static enum NullFilter implements VisitorFilter{
		INSTANCE;

		@Override
		public boolean shouldVisit(int start, int current, int end) {
			// always visit
			return true;
		}

		
		
	}
	public Bond addBond(Bond b){
		Objects.requireNonNull(b);
		return impl.addBond(b);
	}
	public Bond addBond(Atom atom1, Atom atom2, BondType type) {
		Objects.requireNonNull(atom1);
		Objects.requireNonNull(atom2);
		Objects.requireNonNull(type);
		
		return impl.addBond(atom1, atom2, type);
	}

	public Iterator<Atom> getAtomIterator() {
		return new AtomIterator();
	}
	public Iterator<Bond> getBondIterator() {
		return new BondIterator();
	}
	public Iterable<Atom> getAtoms(){
		return this::getAtomIterator;
	}
	public Iterable<Bond> getBonds(){
		return this::getBondIterator;
	}
	
	public boolean hasCoordinates() {
		return impl.hasCoordinates();
	}
	
	public boolean has2DCoordinates() {
		return impl.has2DCoordinates();
	}
	
	public boolean has3DCoordinates() {
		return impl.has3DCoordinates();
	}
	
	public Stream<Atom> atoms(){
		return StreamSupport.stream( Spliterators.spliteratorUnknownSize(getAtomIterator(), Spliterator.ORDERED),
		          false);
	}
	
	public Stream<Bond> bonds(){
		return StreamSupport.stream( Spliterators.spliteratorUnknownSize(getBondIterator(), Spliterator.ORDERED),
		          false);
	}
	
	public void atoms(IndexedConsumer<Atom> consumer){
		int count = impl.getAtomCount();
		for(int i=0; i< count; i++){
			consumer.accept(i, getAtom(i));
		}
	}
	
	public ChemicalBuilder toBuilder(){
		return new ChemicalBuilder(getImpl().deepCopy(), getSource().orElse(null));
	}
	
	private class AtomIterator implements Iterator<Atom>{
		private int currentIndex=0;

		@Override
		public boolean hasNext() {
			return currentIndex < impl.getAtomCount();
		}

		@Override
		public Atom next() {
			if(!hasNext()){
				throw new NoSuchElementException();
			}
			return getAtom(currentIndex ++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
			
		}
		
		
		
	}
	private class BondIterator implements Iterator<Bond>{
		private int currentIndex=0;

		@Override
		public boolean hasNext() {
			return currentIndex < impl.getBondCount();
		}

		@Override
		public Bond next() {
			if(!hasNext()){
				throw new NoSuchElementException();
			}
			return getBond(currentIndex ++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
			
		}
		
		
		
	}

	public int getSmallestRingSize() {
		return impl.getSmallestRingSize();
	}
	/**
	 * Gets the molecular formula. 
	 * @return the molecular formula as a string.
	 */
	public String getFormula() {
		return impl.getFormula();
	}
	/**
	 * Gets the molecular formula. 
	 * @param includeImplicitHydrogen should implicit Hydrogens be incuded in
	 * the resulting formula.
	 * 
	 * @return the molecular formula as a string.
	 */
	public String getFormula(boolean includeImplicitHydrogen) {
		return impl.getFormula(includeImplicitHydrogen);
	}
	public boolean hasImplicitHs() {
		return impl.hasImplicitHydrogens();
	}
	public List<Bond> getBondsTo(Atom atom) {
		return bonds().filter(b-> b.getAtom1()==atom || b.getAtom2() == atom)
				.collect(Collectors.toList());
	}

	public void clearAtomMaps() {
		for(Atom a : getAtoms()) {
			a.setAtomToAtomMap(0);
		}
		
	}
	/**
	 * Set each atom's atomToAtomMap in this Chemical
	 * to it's position in this chemical (offset +1).
	 * So the first atom will get an atomToAtomMap of 1
	 * the second atom will get an atomToAtomMap of 2 etc.
	 */
	public void setAtomMapToPosition() {
		int i=0;
		for(Atom a : getAtoms()) {
			a.setAtomToAtomMap(++i);
		}
		
	}
	
	public Atom removeAtom(int i){
		return impl.removeAtom(i);
	}
	public Atom removeAtom(Atom a){
		return impl.removeAtom(a);
	}
	

	public Bond removeBond(int i){
		return impl.removeBond(i);
	}
	public Bond removeBond(Bond b){
		return impl.removeBond(b);
	}
	
	public List<SGroup> getSGroups(){
		return impl.getSGroups();
	}
	
	public boolean hasSGroups() {
		return impl.hasSGroups();
	}
	
	public SGroup addSGroup(SGroupType type) {
		return impl.addSgroup(type);
	}
	
	public void removeSGroup(SGroup sgroup) {
		impl.removeSGroup(sgroup);
	}
	public void expandSGroups() {
		impl.expandSGroups();
		
	}
	
	public void generateCoordinates() throws ChemkitException{
		impl.generateCoordinates();
	}
}

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

package gov.nih.ncats.molwitch.inchi;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import gov.nih.ncats.common.util.CachedSupplier;
import gov.nih.ncats.common.util.SingleThreadCounter;
import gov.nih.ncats.molwitch.*;
import gov.nih.ncats.molwitch.spi.InchiImplFactory;
import io.github.dan2097.jnainchi.*;

public final class Inchi {

	private static CachedSupplier<List<InchiImplFactory>> implLoaders = CachedSupplier.runOnce(()->{
		List<InchiImplFactory> list = new ArrayList<>();
		for(InchiImplFactory i : ServiceLoader.load(InchiImplFactory.class)){
			list.add(i);
		}
		return list;
	});
	private static Pattern STD_INCHI_PREFIX = Pattern.compile("^\\s*InChI=1S/");

	public static InChiResult asStdInchi(Chemical chemical) throws IOException{
		return asStdInchi(chemical, true);
	}
	private static InChiResult.Status convertEnumStatus(InchiStatus status){
		if(status == InchiStatus.SUCCESS ){
			return InChiResult.Status.VALID;
		}
		if(status == InchiStatus.WARNING){
			return InChiResult.Status.WARNING;
		}
		return InChiResult.Status.ERROR;

	}
	private static boolean canDoDirectInchi(Chemical orig){
		boolean hasProblemAtoms = orig.atoms()
				.filter(a -> a.isQueryAtom() || a.isRGroupAtom() || a.isPseudoAtom())
				.findAny()
				.isPresent();
		return !hasProblemAtoms;
	}
	public static InChiResult asStdInchi(Chemical chemical, boolean trustCoordinates) throws IOException{
		//1. first see if we can use the jna-inchi and try that if we call.
		//2. fall back to using the inchi implementation of the molwitch flavor:

		//inchi doesn't work well with pseudo atoms, R groups and queries
		//so first see if this chemical has something like that.
		//as of this writing, it is not simple to mess around with atoms and bonds at the molwitch-api
		//level like that while the molwithc-flavor is more able to fiddle with things at the implementation level
		//and might have their own workarounds.
		if(canDoDirectInchi(chemical)) {
//			String molText = chemical.toMol(new ChemFormat.MolFormatSpecification()
//											.setKekulization(ChemFormat.KekulizationEncoding.KEKULE)
//											.setHydrogenEncoding(ChemFormat.HydrogenEncoding.MAKE_EXPLICIT));
			String molText = chemical.toMol();
			//JNA-inchi can take mol files
			InchiOutput output = JnaInchi.molToInchi(molText);


			if (output.getStatus() == InchiStatus.SUCCESS || output.getStatus() == InchiStatus.WARNING) {
				InChiResult.Builder builder = new InChiResult.Builder(convertEnumStatus(output.getStatus()));
				builder.setMessage(output.getMessage() == null ? "" : output.getMessage());
				builder.setAuxInfo(output.getAuxInfo() == null ? "" : output.getAuxInfo());

				String fullInchi = output.getInchi();
				builder.setInchi(fullInchi);
				InchiKeyOutput keyOutput = JnaInchi.inchiToInchiKey(fullInchi);
				if (keyOutput.getStatus() == InchiKeyStatus.OK) {
					builder.setKey(keyOutput.getInchiKey());
				}else{
					builder.setKey("");
				}
				return builder.build();
			}

		}
		//if we can't get the inchi for some reason fall back and ask the molwitch implementation to do it
		return computeInchiFromFactory(chemical, trustCoordinates);
	}

	private static InChiResult computeInchiFromFactory(Chemical chemical, boolean trustCoordinates) throws IOException{
		for(InchiImplFactory impl : implLoaders.get()) {
			InChiResult result =  impl.asStdInchi(chemical, trustCoordinates);

			if(result !=null) {
				return result;
			}
		}
		throw new IOException("could not find suitable inchi writer");
	}
	public static Chemical toChemical(String inchi) throws IOException{
		return (Chemical) toChemicalBuilder(inchi, true);
	}
	public static ChemicalBuilder toChemicalBuilder(String inchi) throws IOException{
		return (ChemicalBuilder) toChemicalBuilder(inchi, false);
	}
	private static Object toChemicalBuilder(String inchi, boolean asChemical) throws IOException{
		//first see if we can use jna-inchi
		//jna inchi string needs the InChi=1S/ prefix
		//but some times we don't have the inchi = part
		String jnaInchiString;
		if(inchi.startsWith("InChI=")){
			jnaInchiString = inchi;
		}else{
			jnaInchiString = "InChI="+inchi;
		}
		InchiInputFromInchiOutput inchiInputFromInchi= JnaInchi.getInchiInputFromInchi(jnaInchiString);
		if(InchiStatus.SUCCESS == inchiInputFromInchi.getStatus()){
			InchiInput inchiInput = inchiInputFromInchi.getInchiInput();
			ChemicalBuilder builder= convertToChemicalBuilder(inchiInput, jnaInchiString);
			if(asChemical){
				return builder.build();
			}
			return builder;
		}
		Matcher matcher = STD_INCHI_PREFIX.matcher(inchi);
		if(!matcher.find()) {
			//TODO should we error out here?
		}
		for(InchiImplFactory impl : implLoaders.get()) {
			Chemical result =  impl.parseInchi(inchi);
			if(result !=null) {
				if(asChemical){
					return result;
				}
				return ChemicalBuilder._fromImpl(result.getImpl());
			}
		}
		throw new IOException("could not find suitable inchi parser");
	}

	private static ChemicalBuilder convertToChemicalBuilder(InchiInput inchiInput, String originalInchiString) {
		ChemicalBuilder builder = new ChemicalBuilder();
		Map<InchiAtom, Atom> atomMap = new HashMap<>();
		for(InchiAtom atom : inchiInput.getAtoms()){
			Atom a = builder.addAtom(atom.getElName());
			a.setAtomCoordinates(AtomCoordinates.valueOf(atom.getX(), atom.getY(), atom.getZ()));
//			if(atom.getCharge() !=0) {
//				a.setCharge(atom.getCharge());
//			}
//			int implH = atom.getImplicitHydrogen();
//			if(implH > 0) {
//				a.setImplicitHCount(implH);
//			}
			if(atom.getIsotopicMass() >0) {
				a.setMassNumber(atom.getIsotopicMass());
			}
			atomMap.put(atom, a);
		}
		for(InchiBond bond : inchiInput.getBonds()){
			Bond b = builder.addBond(atomMap.get(bond.getStart()), atomMap.get(bond.getEnd()),
					Bond.BondType.ofOrder(bond.getType().ordinal()));
			switch(bond.getStereo()){
				case SINGLE_1DOWN: b.setStereo(Bond.Stereo.DOWN);
									break;
				case SINGLE_1UP: b.setStereo(Bond.Stereo.UP);
					break;
				case SINGLE_1EITHER: b.setStereo(Bond.Stereo.UP_OR_DOWN);
					break;
				case SINGLE_2DOWN: b.setStereo(Bond.Stereo.DOWN_INVERTED);
					break;
				case SINGLE_2UP: b.setStereo(Bond.Stereo.UP_INVERTED);
					break;
				case SINGLE_2EITHER: b.setStereo(Bond.Stereo.UP_OR_DOWN_INVERTED);
					break;

			}

		}
		for(Map.Entry<InchiAtom, Atom> entry : atomMap.entrySet()){
			//now set implH incase setting bonds messed that up
			int implH = entry.getKey().getImplicitHydrogen();
			if(implH !=0){
				entry.getValue().setImplicitHCount(implH);
			}
			int charge = entry.getKey().getCharge();
			entry.getValue().setCharge(charge);
		}
		for(InchiStereo stereo : inchiInput.getStereos()){
			switch(stereo.getType()){
				case DoubleBond: setDoubleBondStereo(stereo, atomMap, builder);
								break;
				case Tetrahedral: setTetrahedral(stereo, atomMap, builder);
								break;
				case Allene: setExtendedTetrahedral(stereo, atomMap, builder);
					break;
			}
		}
		
//		builder.computeStereo(true);
		builder.setSource(new InchiSource(originalInchiString));
		return builder;
	}

	private static void setDoubleBondStereo(InchiStereo stereo, Map<InchiAtom, Atom> atomMap, ChemicalBuilder builder){

		InchiAtom[] atoms = stereo.getAtoms();
		Map<Atom, SingleThreadCounter> counterMap = new HashMap<>();
		Atom[] builderAtoms = new Atom[4];
		int i=0;
		for(InchiAtom a : atoms) {
			Atom atom = atomMap.get(a);
			builderAtoms[i++] = atom;
			for(Bond b : atom.getBonds()){
				counterMap.computeIfAbsent(b.getOtherAtom(atom), x -> new SingleThreadCounter()).increment();
			}
		}
		List<Atom> doubleBondAtoms = counterMap.entrySet().stream().filter(e -> e.getValue().getAsInt() ==2).map(Map.Entry::getKey).collect(Collectors.toList());
		Optional<? extends Bond> foundBond = builder.getBond(doubleBondAtoms.get(0), doubleBondAtoms.get(1));
		if(foundBond.isPresent()){

			//need to figure out E or Z
			builder.addDoubleBondStereo(foundBond.get(), Bond.DoubleBondStereo.E_TRANS,builderAtoms[0], builderAtoms[1],builderAtoms[2],builderAtoms[3]);
		}

	}

	private static void setTetrahedral(InchiStereo stereo, Map<InchiAtom, Atom> atomMap, ChemicalBuilder builder){
		Atom central = atomMap.get(stereo.getCentralAtom());

		InchiAtom[] atoms = stereo.getAtoms();

		builder.addTetrahedralStereo(central, Chirality.valueByParity(stereo.getParity().ordinal()),
				Arrays.stream(atoms).map(atomMap::get).toArray(i-> new Atom[i]));


	}

	private static void setExtendedTetrahedral(InchiStereo stereo, Map<InchiAtom, Atom> atomMap, ChemicalBuilder builder){
		Atom central = atomMap.get(stereo.getCentralAtom());

		InchiAtom[] atoms = stereo.getAtoms();
		Set<Atom> terminalAtoms = new LinkedHashSet<>();
		for(InchiAtom a : atoms){
			Atom atom = atomMap.get(a);
			for(Bond b : atom.getBonds()){
				Atom otherAtom = b.getOtherAtom(atom);
				if(otherAtom.getAtomicNumber() !=1){
					terminalAtoms.add(otherAtom);
				}
			}
		}
		if(terminalAtoms.size() !=2){
			throw new IllegalArgumentException("could not find 2 terminal atoms");
		}
		Atom[] terminalArray = terminalAtoms.toArray(new Atom[2]);
		builder.addExtendedTetrahedralStereo(central, terminalArray[0], terminalArray[1], Chirality.valueByParity(stereo.getParity().ordinal()),
				Arrays.stream(atoms).map(atomMap::get).toArray(i-> new Atom[i]));


	}

	private static class InchiSource implements ChemicalSource{
		private final String inchi;

		public InchiSource(String inchi) {
			this.inchi = inchi;
		}

		@Override
		public Type getType() {
			return Type.INCHI;
		}

		@Override
		public String getData() {
			return inchi;
		}

		@Override
		public Map<String, String> getProperties() {
			return null;
		}
	}
}

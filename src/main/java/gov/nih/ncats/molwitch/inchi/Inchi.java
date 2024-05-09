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

package gov.nih.ncats.molwitch.inchi;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nih.ncats.common.util.CachedSupplier;
import gov.nih.ncats.molwitch.Atom;
import gov.nih.ncats.molwitch.Chemical;
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

	/**
	 * Convert the given Mol formatted String into an {@link InChiResult}
	 * object.
	 * <strong>Implementation Note</strong>: this method will first try to use the raw mol file
	 * but there are problems converting it into an inchi directly,
	 * the mol will be parsed into a {@link Chemical} and then passed to {@link #asStdInchi(Chemical, boolean)}
	 * so this method should only be used if the caller doesn't already have a {@link Chemical}
	 * object as it might incur a performance penalty.
	 * @param mol the mol formatted record to parse as a String.
	 * @return an {@link InChiResult}
	 * @throws IOException if there was a problem parsing the mol data.
	 * @throws NullPointerException if mol is null.
	 */
	public static InChiResult computeInchiFromMol(String mol) throws IOException{
		Optional<InChiResult> result = inchiFromMol(Objects.requireNonNull(mol));
		if(result.isPresent()){
			return result.get();
		}
		return asStdInchi(Chemical.parseMol(mol), true);
	}

	/**
	 * Compute the inchi of the given {@link Chemical}.
	 * @param chemical the {@link Chemical} to compute the inchi for.
	 * @return an {@link InChiResult}.
	 * @throws IOException if there is a problem computing the inchi.
	 * @throws NullPointerException if chemical is null.
	 */
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

	private static Optional<InChiResult> inchiFromMol(String molText){
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
			return Optional.of(builder.build());
		}
		return Optional.empty();
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
			Optional<InChiResult> result = inchiFromMol(molText);
			if(result.isPresent()){
				return result.get();
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

	/**
	 * Create a {@link Chemical} from the input full inchi string.
	 * @param inchi the full inchi string.  If the String does not start with "InChI=1S/"
	 *              then it is assumed to be a standard version 1 inchi.
	 * @return a Chemical representing the same structure as the input full inchi.
	 * @throws IOException if there is a problem parsing the inchi or if inchi
	 * to Chemical conversion is not supported by the molwitch flavor.
	 */
	public static Chemical toChemical(String inchi) throws IOException{
		Matcher matcher = STD_INCHI_PREFIX.matcher(inchi);
		String input;
		if(!matcher.find()) {
			input = "InChI=1S/"+inchi;
		}else{
			input = inchi;
		}
		Throwable throwable = null;
		for(InchiImplFactory impl : implLoaders.get()) {
			try {
				Chemical result = impl.parseInchi(input);
				if (result != null) {
					return result;
				}
			}catch(Throwable t){
				System.err.printf("Error running impl: %s\n", t.getMessage());
				throwable = t;
			}
		}
		if(throwable !=null){
			throw new IOException("could not find suitable inchi parser", throwable);
		}
		throw new IOException("could not find suitable inchi parser");
	}
}

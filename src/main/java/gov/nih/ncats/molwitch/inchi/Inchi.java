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
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
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
		if(canDoDirectInchi(chemical)) {
			String molText = chemical.toMol();
			InchiOutput output = JnaInchi.molToInchi(molText);
			InChiResult.Builder builder = new InChiResult.Builder(convertEnumStatus(output.getStatus()));
			builder.setMessage(output.getMessage() == null ? "" : output.getMessage());
			builder.setAuxInfo(output.getAuxInfo() == null ? "" : output.getAuxInfo());


			if (output.getStatus() == InchiStatus.SUCCESS || output.getStatus() == InchiStatus.WARNING) {
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
		Matcher matcher = STD_INCHI_PREFIX.matcher(inchi);
		if(!matcher.find()) {
			//TODO should we error out here?
		}
		for(InchiImplFactory impl : implLoaders.get()) {
			Chemical result =  impl.parseInchi(inchi);
			if(result !=null) {
				return result;
			}
		}
		throw new IOException("could not find suitable inchi parser");
	}
}

package gov.nih.ncats.witch.internal;

import java.io.IOException;
import java.util.Objects;

import uk.ac.cam.ch.wwmm.opsin.NameToStructure;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult.OPSIN_RESULT_STATUS;
/**
 * Wrapper class around OPSIN to handle
 * converting chemical names to structures.
 * 
 * @author katzelda
 *
 */
public final class OpsinHelper {

	private static final NameToStructure nts = NameToStructure.getInstance();
	/**
	 * Parse the given name and return the structure 
	 * as a smiles.
	 * @param name the name to parse; can not be null.
	 * 
	 * @return the smiles will never be null.
	 * 
	 * @throws IOException if there was a problem parsing name.
	 * @throws NullPointerException if name is null.
	 */
	public static String parseToSmiles(String name) throws IOException{
		OpsinResult result= nts.parseChemicalName(Objects.requireNonNull(name));

		if(OPSIN_RESULT_STATUS.FAILURE == result.getStatus()){
			throw new IOException(result.getMessage());
		}
		String smiles = result.getSmiles();
		if(smiles ==null){
			throw new IOException("could not generate smiles for '"+name +"' but did not cause opsin failure");
		}
		return smiles;
		
	}
}

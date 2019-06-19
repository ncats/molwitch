package gov.nih.ncats.witch.inchi;

import java.io.IOException;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nih.ncats.witch.Chemical;
import gov.nih.ncats.witch.spi.InchiImplFactory;

public final class Inchi {

	private static ThreadLocal<ServiceLoader<InchiImplFactory>> implLoaders = ThreadLocal.withInitial(()->ServiceLoader.load(InchiImplFactory.class));
	private static Pattern STD_INCHI_PREFIX = Pattern.compile("^\\s*InChI=1S/");
	
	public static InChiResult asStdInchi(Chemical chemical) throws IOException{
		return asStdInchi(chemical, false);
	}
	public static InChiResult asStdInchi(Chemical chemical, boolean trustCoordinates) throws IOException{
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

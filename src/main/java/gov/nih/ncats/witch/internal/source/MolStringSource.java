package gov.nih.ncats.witch.internal.source;

import java.io.IOException;
import java.util.Map;

import gov.nih.ncats.witch.ChemicalSource;


public class MolStringSource extends StringSource{

	public MolStringSource(String data, Type type) {
		super(data, type,false);
		extractProperties(data);
	}

	private void extractProperties(String data){
		
		Map<String,String> props = getProperties();
		try {
			MolFileInfo info = MolFileInfo.parseFrom(data);
			props.put(ChemicalSource.CommonProperties.Version, info.getVersion().toString());
			props.put(ChemicalSource.CommonProperties.Name, info.getName());
			
			props.put("#atoms", Integer.toString(info.getNumberOfAtoms()));
			props.put("#bonds", Integer.toString(info.getNumberOfBonds()));
			props.put("#aromaticBonds", Integer.toString(info.getNumberAromaticBonds()));
			props.put("#doubleBonds", Integer.toString(info.getNumberDoubleBonds()));
			props.put("#singleBonds", Integer.toString(info.getNumberSingleBonds()));
		}catch(IOException e) {
			props.put(ChemicalSource.CommonProperties.ProcessingException, e.getMessage());
			//throw new UncheckedIOException(e);
		}
		
		
	}

}

package gov.nih.ncats.witch;

import java.util.Map;

public interface ChemicalSource {

	public enum Type{
		SMILES,
		MOL{
			@Override
			public boolean includesCoordinates() {
				return true;
			}
			
		},
		SDF{
			@Override
			public boolean includesCoordinates() {
				return true;
			}
		},
		INCHI,
		SMARTS;
		
		public static Type parseType(String format){
			switch(format){
			case "cxsmiles" :
			case "smiles" :return Type.SMILES;
			case "sdf" : return Type.SDF;
			case "mol" : return Type.MOL;
			case "inchi" : return Type.INCHI;
			case "smarts" : return Type.SMARTS;
			}
			if(format.startsWith("mol")) {
				return Type.MOL;
			}
			throw new IllegalArgumentException("unknown format: " + format);
		}
		
		public boolean includesCoordinates() {
			return false;
		}
	}
	
	Type getType();
	
	
	String getData();
	
	
	Map<String,String> getProperties();
	
	public static final class CommonProperties{
		private CommonProperties() {
			//can not instantiate
		}
		
		public static final String Filename = "Filename";
		public static final String Filepath = "Filepath";
		public static final String Filesize = "Filesize";
		public static final String Version = "Version";
		public static final String Name = "Name";
		
		public static final String ProcessingException = "ProcessingException";
		
		
	}
	
	
}

package gov.nih.ncats.witch;

import java.util.HashMap;
import java.util.Map;

public class SmilesSource implements ChemicalSource {

	private final String rawSmiles;
	private final Map<String,String> properties = new HashMap<>();
	public SmilesSource(String rawSmiles) {
		//trim in case it has trailing new lines
		this.rawSmiles = rawSmiles.trim();
	}

	@Override
	public Type getType() {
		return Type.SMILES;
	}

	@Override
	public String getData() {
		return rawSmiles;
	}

	@Override
	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result +  rawSmiles.hashCode();
		result = prime * result + getType().hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "SmilesSource [rawSmiles=" + rawSmiles + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		
		if(!(obj instanceof ChemicalSource)){
			return false;
		}
		ChemicalSource other = (ChemicalSource) obj;
		return other.getType()==getType() && rawSmiles.equals(other.getData());
	}

}

package gov.nih.ncats.witch;

import java.util.HashMap;
import java.util.Map;


public class SmartsSource implements ChemicalSource{
	private final String smarts;
	private final Map<String,String> properties = new HashMap<>();
	public SmartsSource(String smarts) {
		//trim in case it has trailing new lines
		this.smarts = smarts.trim();
	}

	@Override
	public Type getType() {
		return Type.SMARTS;
	}

	@Override
	public String getData() {
		return smarts;
	}

	@Override
	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public String toString() {
		return "SmartsSource [smarts=" + smarts + ", properties=" + properties + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((smarts == null) ? 0 : smarts.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SmartsSource other = (SmartsSource) obj;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (smarts == null) {
			if (other.smarts != null)
				return false;
		} else if (!smarts.equals(other.smarts))
			return false;
		return true;
	}
	
	
}

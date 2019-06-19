package gov.nih.ncats.witch.internal.source;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import gov.nih.ncats.witch.ChemicalSource;

public class StringSource implements ChemicalSource{

	private final String data;
	private final Type type;
	
	private final Map<String,String> properties = new HashMap<>();
	public StringSource(String data, Type type) {
		this(data, type, true);
	}
	public StringSource(String data, Type type, boolean trim) {
		Objects.requireNonNull(data);
		if(trim){
			this.data = data.trim();
		}else{
			this.data = data;
		}
		this.type = Objects.requireNonNull(type);
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String getData() {
		return data;
	}

	@Override
	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
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
		return other.getType()==getType() && data.equals(other.getData());
	}

	@Override
	public String toString() {
		return "StringSource [ type=" + type + ",  data=" + data + "]";
	}

}

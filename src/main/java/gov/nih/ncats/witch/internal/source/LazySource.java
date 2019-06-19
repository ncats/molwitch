package gov.nih.ncats.witch.internal.source;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import gov.nih.ncats.witch.ChemicalSource;

public class LazySource implements ChemicalSource{

	private final Type type;
	private final Supplier<String> supplier;
	private String data;
	private final Map<String,String> properties = new HashMap<>();
	
	public LazySource(Type type, Supplier<String> supplier) {
		this.type = Objects.requireNonNull(type);
		this.supplier = Objects.requireNonNull(supplier);
	}

	@Override
	public Type getType() {
		return type;
	}
	

	@Override
	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public synchronized String getData() {
		if(data !=null){
			return data;
		}
		String temp= supplier.get();
		if(temp ==null){
			throw new NullPointerException("could not get source data");
		}
		data=temp;
		return data;
	}

}

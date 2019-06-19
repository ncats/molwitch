package gov.nih.ncats.witch;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FileSource implements ChemicalSource{

	private final File f;
	private final Type type;
	private final Map<String, String> properties = new HashMap<>();
	public FileSource(File f, Type type) {
		this.f = f;
		this.type = type;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String getData() {
		return f.getAbsolutePath();
	}

	@Override
	public Map<String, String> getProperties() {
		return properties;
	}

}

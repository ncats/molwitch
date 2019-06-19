package gov.nih.ncats.witch;

import java.util.ServiceLoader;

import gov.nih.ncats.witch.spi.WitchModule;

public class Witch {
	private static ServiceLoader<WitchModule> loader;
	 
	private static WitchModule module;
	
	static{
		loader = ServiceLoader.load(WitchModule.class);
		for(WitchModule m : loader){
			module= m;
		}
		if(module ==null){
			throw new IllegalStateException("could not find a module!!!! " + loader);
		}
	}
	public static String getModuleName(){
		return module.getName();
	}

}

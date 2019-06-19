package gov.nih.ncats.witch.isotopes;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gov.nih.ncats.witch.isotopes.Isotope.IsotopeBuilder;

public enum NISTIsotopeFactory implements IsotopeFactory{

	INSTANCE;
	private static final Map<String, Set<Isotope>> SYMBOL_MAP = new HashMap<>();
	private static final Map<Integer, Set<Isotope>> ATNO_MAP = new HashMap<>();
	static {
		Map<String, Set<Isotope>> bySymbol = new HashMap<>();
		Map<Integer, Set<Isotope>> byAtno = new HashMap<>();
		try(InputStream in = NISTIsotopeFactory.class.getResourceAsStream("elements.NIST.2017.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));	
			){
			String line;
			IsotopeBuilder builder = new IsotopeBuilder();
			while( (line = reader.readLine()) !=null) {
				int offset = line.indexOf('=');
				if(offset == -1) {
					if(builder.getAtomicNumber() >0) {
						Isotope isotope = builder.build();
						bySymbol.computeIfAbsent(isotope.getSymbol(), k-> new HashSet<>()).add(isotope);
						byAtno.computeIfAbsent(isotope.getAtomicNumber(), k-> new HashSet<>()).add(isotope);
						
						builder = new IsotopeBuilder();
					}
					continue;
				}
				String key = line.substring(0, offset).trim();
				String value = line.substring(offset+1).trim();
				if(value.isEmpty()) {
					continue;
				}
				switch(key) {
				case "Atomic Number" : builder.setAtomicNumber(Integer.parseInt(value));
										break;
				case "Atomic Symbol" : builder.setSymbol(value);
										break;
										
				case "Mass Number" : builder.setRelativeAtomicMass(ValueWithUncertainty.parse(value));
										break;
				case "Isotopic Composition" : builder.setIsotopicComposition(ValueWithUncertainty.parse(value));
										break;		
				case "Standard Atomic Weight" : builder.setStandardAtomicWeight(WeightInterval.parse(value));
										break;
										
				default: //no op
				}
			}
			
			if(builder.getAtomicNumber() >0) {
				Isotope isotope = builder.build();
				bySymbol.computeIfAbsent(isotope.getSymbol(), k-> new HashSet<>()).add(isotope);
				byAtno.computeIfAbsent(isotope.getAtomicNumber(), k-> new HashSet<>()).add(isotope);
				
			}
			
			
		}catch(Exception e) {
			
		}
		
		bySymbol.entrySet().forEach(e-> SYMBOL_MAP.put(e.getKey(), Collections.unmodifiableSet(e.getValue())));
		byAtno.entrySet().forEach(e-> ATNO_MAP.put(e.getKey(), Collections.unmodifiableSet(e.getValue())));
		
	
	}
	@Override
	public Set<Isotope> getIsotopesFor(String symbol) {
		return SYMBOL_MAP.get(symbol);
	}

	@Override
	public Set<Isotope> getIsotopesFor(int atomicNumber) {
		return ATNO_MAP.get(atomicNumber);
	}
	


}

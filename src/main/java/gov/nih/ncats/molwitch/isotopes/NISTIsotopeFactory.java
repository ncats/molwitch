/*
 * NCATS-MOLWITCH
 *
 * Copyright 2023 NIH/NCATS
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gov.nih.ncats.molwitch.isotopes;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nih.ncats.molwitch.isotopes.Isotope.IsotopeBuilder;

public enum NISTIsotopeFactory implements IsotopeFactory{

	INSTANCE;
	private static final Map<String, Set<Isotope>> SYMBOL_MAP = new HashMap<>();
	private static final Map<Integer, Set<Isotope>> ATNO_MAP = new HashMap<>();
	static {

		Pattern RADIOACTIVE_PATTERN = Pattern.compile("\\[(\\d+)\\]");
		Pattern NBSP_PATTERN = Pattern.compile("&nbsp;");
		Map<String, Set<Isotope>> bySymbol = new HashMap<>();
		Map<Integer, Set<Isotope>> byAtno = new HashMap<>();
		try(InputStream in = NISTIsotopeFactory.class.getResourceAsStream("/elements.NIST.2017.txt");
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
				value = NBSP_PATTERN.matcher(value).replaceAll(" ").trim();
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
				case "Standard Atomic Weight" :
					//radioactive values sometimes are in brackets like [98]
					Matcher m = RADIOACTIVE_PATTERN.matcher(value);
					if(m.find()){
						value = m.group(1);
					}
					builder.setStandardAtomicWeight(WeightInterval.parse(value));
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
			e.printStackTrace();
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

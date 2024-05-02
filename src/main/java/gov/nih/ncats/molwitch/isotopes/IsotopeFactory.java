/*
 * NCATS-MOLWITCH
 *
 * Copyright 2024 NIH/NCATS
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

import java.util.Optional;
import java.util.Set;

public interface IsotopeFactory {

	Set<Isotope> getIsotopesFor(String symbol);
	
	Set<Isotope> getIsotopesFor(int atomicNumber);
	
	default Optional<Isotope> getMostAbundant(String symbol){
		return getIsotopesFor(symbol).stream()
			.max((a,b)->{
				ValueWithUncertainty vA = a.getIsotopicComposition();
				ValueWithUncertainty vB = b.getIsotopicComposition();
				
				if(vA ==null) {
					if(vB==null) {
						return 0;
					}
					return -1;
				}
				if(vB==null) {
					return 1;
				}
				return vA.compareTo(vB);
			});
		
	}
	
	default Optional<Isotope> getMostAbundant(int atomicNumber){
		return getIsotopesFor(atomicNumber).stream()
				.max((a,b)->{
					ValueWithUncertainty vA = a.getIsotopicComposition();
					ValueWithUncertainty vB = b.getIsotopicComposition();
					
					if(vA ==null) {
						if(vB==null) {
							return 0;
						}
						return -1;
					}
					if(vB==null) {
						return 1;
					}
					return vA.compareTo(vB);
				});
	}
}

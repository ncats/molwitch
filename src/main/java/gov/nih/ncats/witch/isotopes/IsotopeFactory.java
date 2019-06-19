package gov.nih.ncats.witch.isotopes;

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

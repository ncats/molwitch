/*
 * NCATS-MOLWITCH
 *
 * Copyright 2025 NIH/NCATS
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


public class Isotope {

	private final String synbol;
	private final int atomicNumber, massNumber;
	private final WeightInterval standardAtomicWeight;
	private final ValueWithUncertainty relativeAtomicMass, isotopicComposition;
	
	
	
	public Isotope(String synbol, int atomicNumber, int massNumber, WeightInterval standardAtomicWeight,
			ValueWithUncertainty relativeAtomicMass, ValueWithUncertainty isotopicComposition) {
		this.synbol = synbol;
		this.atomicNumber = atomicNumber;
		this.massNumber = massNumber;
		this.standardAtomicWeight = standardAtomicWeight;
		this.relativeAtomicMass = relativeAtomicMass;
		this.isotopicComposition = isotopicComposition;
	}


	public String getSymbol() {
		return synbol;
	}



	public int getAtomicNumber() {
		return atomicNumber;
	}



	public int getMassNumber() {
		return massNumber;
	}



	public WeightInterval getStandardAtomicWeight() {
		return standardAtomicWeight;
	}



	public ValueWithUncertainty getRelativeAtomicMass() {
		return relativeAtomicMass;
	}



	public ValueWithUncertainty getIsotopicComposition() {
		return isotopicComposition;
	}



	public static class IsotopeBuilder{
		private  String synbol;
		private  int atomicNumber, massNumber;
		private  WeightInterval standardAtomicWeight;
		private  ValueWithUncertainty relativeAtomicMass, isotopicComposition;
		public String getSynbol() {
			return synbol;
		}
		public IsotopeBuilder setSymbol(String synbol) {
			this.synbol = synbol;
			return this;
		}
		public int getAtomicNumber() {
			return atomicNumber;
		}
		public IsotopeBuilder setAtomicNumber(int atomicNumber) {
			this.atomicNumber = atomicNumber;
			return this;
		}
		public int getMassNumber() {
			return massNumber;
		}
		public IsotopeBuilder setMassNumber(int massNumber) {
			this.massNumber = massNumber;
			return this;
		}
		public WeightInterval getStandardAtomicWeight() {
			return standardAtomicWeight;
		}
		public IsotopeBuilder setStandardAtomicWeight(WeightInterval standardAtomicWeight) {
			this.standardAtomicWeight = standardAtomicWeight;
			return this;
		}
		public ValueWithUncertainty getRelativeAtomicMass() {
			return relativeAtomicMass;
		}
		public IsotopeBuilder setRelativeAtomicMass(ValueWithUncertainty relativeAtomicMass) {
			this.relativeAtomicMass = relativeAtomicMass;
			return this;
		}
		public ValueWithUncertainty getIsotopicComposition() {
			return isotopicComposition;
		}
		public IsotopeBuilder setIsotopicComposition(ValueWithUncertainty isotopicComposition) {
			this.isotopicComposition = isotopicComposition;
			return this;
		}
		
		public Isotope build() {
			return new Isotope(synbol, atomicNumber, 
					massNumber, standardAtomicWeight, 
					relativeAtomicMass, isotopicComposition);
		}
	}
}

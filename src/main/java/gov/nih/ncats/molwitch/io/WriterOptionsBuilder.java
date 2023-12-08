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

package gov.nih.ncats.molwitch.io;

public class WriterOptionsBuilder {

	private Boolean makeImplicitHydrogrensExplicit;
	private Boolean removeExplicitHydrogrens;
	
	private Boolean aromatize;
	private Boolean kekulize;
	
	private Float avgBondLength;
	private String version;
	
	public WriterOptionsBuilder makeImplicitHydrogrensExplicit() {
		makeImplicitHydrogrensExplicit = true;
		removeExplicitHydrogrens = null;
		
		return this;
	}
	
	public WriterOptionsBuilder removeExplicitHydrogrens() {
		makeImplicitHydrogrensExplicit = null;
		removeExplicitHydrogrens = true;
		
		return this;
	}
	
	public WriterOptionsBuilder aromatize() {
		this.aromatize= true;
		this.kekulize = null;
		return this;
	}
	public WriterOptionsBuilder kekulize() {
		this.aromatize= null;
		this.kekulize = true;
		return this;
	}
	/**
	 * then the exported atom coordinates are scaled in such a way that the average C-C bond length will be the specified number.
	 * @param angstroms the average bond length to use in anstroms.
	 * @return  this.
	 */
	public WriterOptionsBuilder avgBondLength(float angstroms){
		avgBondLength = angstroms;
		return this;
	}
	
	public WriterOptionsBuilder requiredVersion(String version){
		this.version = version;
		return this;
	}
	
	public WriterOptions build(){
		return new DefaultWriterOptions(this);
	}
	
	
	private static class DefaultWriterOptions implements WriterOptions{
		private final Boolean makeImplicitHydrogrensExplicit;
		private final Boolean removeExplicitHydrogrens;
		
		private final Boolean aromatize;
		private final Boolean kekulize;
		
		private final Float avgBondLength;
		
		private final String version;
		
		DefaultWriterOptions(WriterOptionsBuilder builder){
			this.makeImplicitHydrogrensExplicit = builder.makeImplicitHydrogrensExplicit;
			this.removeExplicitHydrogrens = builder.removeExplicitHydrogrens;
			
			this.aromatize = builder.aromatize;
			this.kekulize = builder.kekulize;
			this.avgBondLength = builder.avgBondLength;
			this.version = builder.version;
		}

		public Boolean shouldMakeImplicitHydrogrensExplicit() {
			return makeImplicitHydrogrensExplicit;
		}

		public Boolean shouldRemoveExplicitHydrogrens() {
			return removeExplicitHydrogrens;
		}

		public Boolean forceAromatize() {
			return aromatize;
		}

		public Boolean forceKekulize() {
			return kekulize;
		}

		public Float normalizeAvgBondLength() {
			return avgBondLength;
		}
		
		
		public String getVersion(){
			return version;
		}
		
		
	}
}

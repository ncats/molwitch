package gov.nih.ncats.witch.io;

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

package gov.nih.ncats.witch.io;

public interface WriterOptions {

	public static WriterOptions V2000 = new WriterOptionsBuilder()
											.requiredVersion("2000")
											.build();
	
	public static WriterOptions V3000 = new WriterOptionsBuilder()
											.requiredVersion("3000")
											.build();
	
	Boolean shouldMakeImplicitHydrogrensExplicit();

	Boolean shouldRemoveExplicitHydrogrens();

	Boolean forceAromatize();

	Boolean forceKekulize();

	Float normalizeAvgBondLength();
	
	String getVersion();
}

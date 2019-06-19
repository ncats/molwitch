package gov.nih.ncats.witch;

public interface DoubleBondStereochemistry {

	 /**
     * Possible stereo types of two-atom bonds depending
     * on which atom in the bond is the stereo center and
     * which direction the bond is.
     */
    public enum DoubleBondStereo {
		/** A bond for which there is no stereochemistry. */
		NONE,
		
		/**
		 * Indication that this double bond has a fixed, but unknown E/Z
		 * configuration.
		 */
		E_OR_Z,
		/**
		 * Indication that this double bond has a E configuration (aka trans).
		 * Each substituent on a double bond is assigned a priority 
		 * (using Cahn-Ingold-Prelog priority rules). 
		 * If the two groups of higher priority are on opposite sides
		 *  of the double bond, the bond is assigned the configuration
		 *  E (from entgegen, the German word for "opposite").
		 */
		E_TRANS,
		/**
		 * Indication that this double bond has a Z configuration (aka cis).
		 * Each substituent on a double bond is assigned a priority 
		 * (using Cahn-Ingold-Prelog priority rules). 
		 * If the two groups of higher priority are on opposite sides
		 *  of the double bond, the bond is assigned the configuration
		 *  Z (from zusammen, the German word for "together").
		 */
		Z_CIS;
		
    }
    
    DoubleBondStereo getStereo();
    
    Bond getDoubleBond();
    
    Atom getLigand(int i);
    
    Bond getLigandBond(int i);
}

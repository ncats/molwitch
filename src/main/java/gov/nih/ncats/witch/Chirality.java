package gov.nih.ncats.witch;

public enum Chirality {
	Unknown(-1),
	/**
	 * Not chiral (aka achiral).
	 */
	Non_Chiral(0),
	/**
	 * Rectus (right). Priority decreases in clockwise direction.
	 */
	R(1),
	/**
	 * Sinister (left). priority decreases in counterclockwise direction.
	 */
	S(2),
	Parity_Either(3);
	
	
	
	private int parity;
	
	
	private static final Chirality[] VALUES;
	private static final Chirality[] INVERTED;
	static {
		VALUES = new Chirality[] {Non_Chiral, R, S, Parity_Either};
		INVERTED = new Chirality[] {Unknown,Non_Chiral, S, R, Parity_Either};
		
	}
	Chirality(int v){
		parity = (byte) v;
	}
	
	public boolean isOdd(){
		return parity==1;
	}
	
	public boolean isEven(){
		return parity==2;
	}
	
	public boolean isEither(){
		return parity ==3;
	}
	
	public int getParity(){
		return parity;
	}
	
	public static Chirality valueByParity(int parity) {
		if(parity <0 || parity > 3) {
			return Unknown;
		}
		return VALUES[parity];
	}
	
	public Chirality invert() {
		return INVERTED[ordinal()];
	}
}

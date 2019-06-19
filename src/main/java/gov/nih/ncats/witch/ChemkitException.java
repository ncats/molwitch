package gov.nih.ncats.witch;

public class ChemkitException extends Exception{

	public ChemkitException(String message, Throwable cause) {
		super(message, cause);
	}

	public ChemkitException(String message) {
		super(message);
	}

	public ChemkitException(Throwable cause) {
		super(cause);
	}

}

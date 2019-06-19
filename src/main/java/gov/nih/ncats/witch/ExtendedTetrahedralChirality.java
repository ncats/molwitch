package gov.nih.ncats.witch;

import java.util.List;

public interface ExtendedTetrahedralChirality extends Stereocenter{

	List<Atom> getTerminalAtoms();

	List<Atom> getPeripheralAtoms();

	Atom getCenterAtom();
	
	Chirality getChirality();

}
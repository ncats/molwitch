package gov.nih.ncats.witch;

import java.util.List;

public interface Stereocenter {

	Atom getCenterAtom();

	Chirality getChirality();

	List<Atom> getPeripheralAtoms();

}
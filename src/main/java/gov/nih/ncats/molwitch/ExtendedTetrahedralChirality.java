/*
 * NCATS-MOLWITCH
 *
 * Copyright 2019 NIH/NCATS
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

package gov.nih.ncats.molwitch;

import java.util.List;

/**
 * An Allene like Stereocenter configured
 * with a Carbon in the center, connected to
 * 2 terminal atoms by double bonds
 * and each terminal atom connected to
 * at most 2 peripheral atoms.  It is possible
 * that a peripheral is an implicit Hydrogen
 * so there might be fewer peripherals.
 *
 * <pre>
 *
 * p0          p3
 *   \        /
 *    T = C = T
 *   /         \
 * p1          p4
 * </pre>
 */
public interface ExtendedTetrahedralChirality extends Stereocenter{
	/**
	 * Get the 2 atoms that are double bonded to the center atom.
	 *  @return the list of Terminal Atoms; should never be null but may be empty.
	 */
	List<Atom> getTerminalAtoms();

	/**
	 * Get the atoms that are bonded to the terminal atoms
	 * which aren't the center atom.
	 * @return the list of Peripheral Atoms; should never be null but may be empty.
	 */
	List<Atom> getPeripheralAtoms();

	/**
	 * Get the Atom in the center.
	 * @return the center {@link Atom}; should never be {@code null}.
	 */
	Atom getCenterAtom();

	/**
	 * Get the {@link Chirality}.
	 * @return the {@link Chirality}; should never be {@code null}.
	 */
	Chirality getChirality();

}

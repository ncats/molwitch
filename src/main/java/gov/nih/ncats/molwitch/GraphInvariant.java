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

package gov.nih.ncats.molwitch;
/**
 * The Graph Invariant data for a specific {@link Chemical}.
 * 
 * @author katzelda
 *
 */
public interface GraphInvariant {
	/**
	 * Get the number of atoms in the {@link Chemical}.
	 * 
	 * @return the number of atoms; will always be &ge; 0.
	 */
	int getNumberOfAtoms();
	/**
	 * Get the invariant raw value for the {@code ith} {@link Atom}.
	 * Different spi implementations may encode this data differently.
	 * 
	 * @param atomIndex the atom index for the atom to lookup.
	 * 
	 * @return the raw invariant data encoded as a long.
	 */
	long getAtomInvariantValue(int atomIndex);
	/**
	 * Are the {@code ith} {@link Atom} and the
	 * {@code jth} {@link Atom} compatible.
	 * 
	 * @param i the {@code ith} {@link Atom}.
	 * @param j the  {@code jth} {@link Atom}.
	 * 
	 * @return {@code true} if the atoms are compatible;
	 * {@code false} otherwise.
	 */
	boolean isAtomCompatible(int i, int j);
}

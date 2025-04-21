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
 * CIS or TRANS
 *
 * A        X
 *  \     /
 *   C = C
 *  /     \
 * H       H
 */
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

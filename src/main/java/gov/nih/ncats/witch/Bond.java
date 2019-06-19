/*
 * NCATS-WITCH
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

package gov.nih.ncats.witch;

import java.util.HashMap;
import java.util.Map;

/**
 * Object representation of a Bond between
 * two {@link Atom}s.
 * 
 * @author katzelda
 *
 */
public interface Bond {
	/**
	 * Get the other {@link Atom} in this bond
	 * that is not the passed in {@link Atom}.
	 * If the passed in {@link Atom} is "atom 1" then
	 * this will return "atom 2" and vice versa.
	 * 
	 * @param a the {@link Atom} in the bond NOT to return;
	 * can not be null.
	 * @return the other {@link Atom}; will not be null.
	 * 
	 * @throws NullPointerException if a is null.
	 * 
	 */
	Atom getOtherAtom(Atom a);
	/**
	 * Get the first atom in the bond.
	 * 
	 * @return the {@link Atom}; will never be null.
	 */
	Atom getAtom1();
	/**
	 * Get the second atom in the bond.
	 * 
	 * @return the {@link Atom}; will never be null.
	 */
	Atom getAtom2();
	
	BondType getBondType();
	
	Stereo getStereo();
	DoubleBondStereo getDoubleBondStereo();
	
	Bond switchParity();
	
	void setStereo(Stereo stereo);
	
	void setBondType(BondType type);
	
	default double getBondLength(){
		return Math.sqrt(getAtom1().getAtomCoordinates().distanceSquaredTo(getAtom2().getAtomCoordinates()));
	}
	
	enum BondType{
		SINGLE('-', 1){
			@Override
			public BondType switchParity(){
				return DOUBLE;
			}
		},
		DOUBLE('=',2){
			
			@Override
			public BondType switchParity(){
				return SINGLE;
			}
		},
		TRIPLE('#',3),
		QUADRUPLE('$', 4),
		AROMATIC(':', 4)
		;
		
		public BondType switchParity(){
			return this;
		}
		
		
		private static Map<Integer, BondType> ORDER_MAP;
		private final char symbol;
		private final Integer order;
		
		static{
			ORDER_MAP = new HashMap<>();
			for(BondType type : BondType.values()){
				ORDER_MAP.put(type.order, type);
			}
		}
		
		BondType(char symbol, Integer order){
			this.symbol = symbol;
			this.order = order;
		}

		

		public int getOrder() {
			return order;
		}



		public char getSymbol() {
			return symbol;
		}



		public static BondType ofOrder(int order) {
			return ORDER_MAP.get(order);
		}
		
		
		
	}
	
	boolean isQueryBond();
	
	 /**
     * Possible stereo types of two-atom bonds depending
     * on which atom in the bond is the stereo center and
     * which direction the bond is.
     */
    public enum Stereo {
		/** A bond for which there is no stereochemistry. */
		NONE{
			@Override
			public Stereo flip() {
				return NONE;
			}
		},
		/**
		 * A bond pointing up of which the {@link Bond#getAtom1()} is the
		 * stereocenter and {@link Bond#getAtom2()} is above the drawing plane.
		 */
		UP{
			@Override
			public Stereo flip() {
				return DOWN;
			}
		},
		/**
		 * A bond pointing up of which {@link Bond#getAtom2()} is the
		 * stereocenter and {@link Bond#getAtom1()} is above the drawing plane.
		 */
		UP_INVERTED{
			@Override
			public Stereo flip() {
				return DOWN_INVERTED;
			}
		},
		/**
		 * A bond pointing down of which {@link Bond#getAtom1()} is the
		 * stereocenter and {@link Bond#getAtom2()} is below the drawing plane.
		 */
		DOWN{
			@Override
			public Stereo flip() {
				return UP;
			}
		},
		/**
		 * A bond pointing down of which {@link Bond#getAtom2()} is the
		 * stereocenter and {@link Bond#getAtom1()} is below the drawing plane.
		 */
		DOWN_INVERTED{
			@Override
			public Stereo flip() {
				return UP_INVERTED;
			}
		},
		/**
		 * A bond for which there is stereochemistry, we just do not know if it
		 * is {@link #UP} or {@link #DOWN}. {@link Bond#getAtom1()} is the
		 * stereocenter.
		 */
		UP_OR_DOWN{
			@Override
			public Stereo flip() {
				return UP_OR_DOWN;
			}
		},
		/**
		 * A bond for which there is stereochemistry, we just do not know if it
		 * is {@link #UP} or {@link #DOWN}. {@link Bond#getAtom2()} is the
		 * stereocenter.
		 */
		UP_OR_DOWN_INVERTED{

			@Override
			public Stereo flip() {
				return UP_OR_DOWN_INVERTED;
			}
			
		};
    	
    		 public abstract Stereo flip();
		
    }
    
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

	boolean isInRing();
	boolean isAromatic();
	
	
}

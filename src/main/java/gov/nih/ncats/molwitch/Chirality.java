/*
 * NCATS-MOLWITCH
 *
 * Copyright 2024 NIH/NCATS
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

public enum Chirality {
	Unknown(-1),
	/**
	 * Not chiral (aka achiral).
	 */
	Non_Chiral(0),
	/**
	 * Rectus (right, odd). Priority decreases in clockwise direction.
	 */
	R(1),
	/**
	 * Sinister (left, even). priority decreases in counterclockwise direction.
	 */
	S(2),
	Parity_Either(3),
	/**
	 * Psuedo-Rectus (right, odd). Priority decreases in clockwise direction.
	 */
	r(4),
	/**
	 * Psuedo-Sinister (left, even). priority decreases in counterclockwise direction.
	 */
	s(5);
	
	
	
	private int parity;
	
	
	private static final Chirality[] VALUES;
	private static final Chirality[] INVERTED;
	static {
		VALUES = new Chirality[] {Non_Chiral, R, S, Parity_Either, r, s};
		INVERTED = new Chirality[] {Unknown,Non_Chiral, S, R, Parity_Either, s, r};
		
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
	public boolean isRForm() {
		return parity==1 || parity==4;
	}
	public boolean isSForm() {
		return parity==2 || parity==5;
	}
	
	public boolean isDefined() {
		return parity==2 || parity==1 || parity==4 || parity==5;
	}
	
	public static Chirality valueByParity(int parity) {
		if(parity <0 || parity > 5) {
			return Unknown;
		}
		return VALUES[parity];
	}
	
	public Chirality invert() {
		return INVERTED[ordinal()];
	}
}

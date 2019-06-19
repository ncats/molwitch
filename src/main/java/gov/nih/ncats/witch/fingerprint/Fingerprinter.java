package gov.nih.ncats.witch.fingerprint;

import gov.nih.ncats.witch.Chemical;

/**
 * Encapsulates an algorithm to compute
 * a Chemical {@link Fingerprint}.
 * 
 * @author katzelda
 *
 */
public interface Fingerprinter {
	/**
	 * Compute the {@link Fingerprint} for the given {@link Chemical}.
	 * 
	 * @param chemical the {@link Chemical} to fingerprint; can not be null.
	 * 
	 * @return a new {@link Fingerprint} will never be null.
	 * 
	 * @throws NullPointerException if chemical is null.
	 */
	Fingerprint computeFingerprint(Chemical chemical);
}

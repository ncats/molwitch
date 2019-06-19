package gov.nih.ncats.witch.spi;

import java.util.ServiceLoader;
import java.util.Set;

import gov.nih.ncats.witch.fingerprint.Fingerprinter;
import gov.nih.ncats.witch.fingerprint.Fingerprinters;
import gov.nih.ncats.witch.fingerprint.Fingerprinters.FingerprintSpecification;
import gov.nih.ncats.witch.fingerprint.Fingerprinter;
import gov.nih.ncats.witch.fingerprint.Fingerprinters;

/**
 * Interface that can generate fingerprints.
 * 
 * <p>
 * All implementations of this interface should be placed 
 * in the {@code src/main/resources/META-INF/services/gov.nih.ncats.chemkit.spi.FingerprinterImpl}
 * file so the {@link ServiceLoader} can find them.
 * 
 * @author katzelda
 *
 */
public interface FingerprinterImpl{
	/**
	 * Does this implementation support the given FingerPrinterOptions.
	 * If this implementation does, then {@code true} will be returned.
	 * 
	 * This is used by 
	 * 
	 * @param options the FingerPrinterOptions to check which may include 
	 * the name of the algorithm, any parameter settings.
	 * 
	 * @return {@code true} if it does support it; {@code false} otherwise.
	 * 
	 * @apiNote the default implemenation where {@link #isDefault()}
	 * returns {@code true} should always return true.
	 */
	boolean supports(Fingerprinters.FingerprintSpecification options);

	/**
	 * Is this implementation the default implementation
	 * that should be used by {@link Fingerprinters#getDefault()}.
	 * Note, only 1 implementation per SPI should return {@code true}.
	 * 
	 * @return {@code true} if this is the default,
	 * {@code false} otherwise.
	 */
	boolean isDefault();
	Set<String> getSupportedAlgorithmNames();
	/**
	 * 
	 * @param fingerPrinterOptions
	 * @return
	 */
	Fingerprinter createFingerPrinterFor(FingerprintSpecification fingerPrinterOptions);

	/**
	 * Create a new Fingerprinter object using the default
	 * implementation with default settings.
	 * 
	 * @apiNote by default, this method throws UnsupportedOperationException
	 * for all implementations where {@link #isDefault()} returns {@code false}.
	 * This should only be implemented by the implementation where {@link #isDefault()}
	 * returns {@code true}.
	 * 
	 * @return a new Fingerprinter
	 */
	default Fingerprinter createDefaultFingerprinter() {
		throw new UnsupportedOperationException("not default implementation");
	}
}

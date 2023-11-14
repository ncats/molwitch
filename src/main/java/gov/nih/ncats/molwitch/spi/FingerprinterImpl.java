/*
 * NCATS-MOLWITCH
 *
 * Copyright 2023 NIH/NCATS
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

package gov.nih.ncats.molwitch.spi;

import java.util.ServiceLoader;
import java.util.Set;

import gov.nih.ncats.molwitch.fingerprint.Fingerprinter;
import gov.nih.ncats.molwitch.fingerprint.Fingerprinters;
import gov.nih.ncats.molwitch.fingerprint.Fingerprinters.FingerprintSpecification;

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
	 * @param spec the FingerPrinterOptions to check which may include
	 * the name of the algorithm, any parameter settings.
	 * 
	 * @return {@code true} if it does support it; {@code false} otherwise.
	 * 
	 * @apiNote the default implemenation where {@link #isDefault()}
	 * returns {@code true} should always return true.
	 */
	boolean supports(FingerprintSpecification spec);

	/**
	 * Is this implementation the default implementation
	 * that should be used by {@link Fingerprinters#getDefault()}.
	 * Note, only 1 implementation per SPI should return {@code true}.
	 * 
	 * @return {@code true} if this is the default,
	 * {@code false} otherwise.
	 */
	boolean isDefault();

	/**
	 * Get the set of Algorithm names this Fingerprinter supports.
	 * @return the set of algorithm names; can not be null but may be empty.
	 */
	Set<String> getSupportedAlgorithmNames();
	/**
	 * Create a {@link Fingerprinter} with the given specification.
	 * @param spec the {@link FingerprintSpecification}
	 * @return a new Fingerprinter.
	 */
	Fingerprinter createFingerPrinterFor(FingerprintSpecification spec);

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

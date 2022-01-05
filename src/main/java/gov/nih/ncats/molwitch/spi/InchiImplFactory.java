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

package gov.nih.ncats.molwitch.spi;

import java.io.IOException;

import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.molwitch.inchi.InChiResult;

public interface InchiImplFactory {

	InChiResult asStdInchi(Chemical chemical, boolean trustCoordinates) throws IOException;

	/**
	 * Parse the given full inchi into a Chemical object.
	 * @param inchi the full inchi as a String.  This should be prefixed with "InChI=1S/" in most cases.
	 * @return a new Chemical object or null if not supported.
	 * @throws IOException if there is a problem parsing the inchi.
	 */
	Chemical parseInchi(String inchi) throws IOException;
}

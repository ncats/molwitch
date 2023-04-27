/*
 * NCATS-MOLWITCH
 *
 * Copyright 2022 NIH/NCATS
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

package gov.nih.ncats.molwitch.inchi;

import java.util.Objects;
import java.util.regex.Pattern;

final class InchiUtil {

    private InchiUtil(){
        //can not instantiate
    }

       /*
    The first 14 characters result from a SHA-256 hash of the connectivity information
    (the main layer and /q sublayer of the charge layer) of the InChI

    a single character indicating the kind of InChIKey (S for standard and N for nonstandard),
    and a character indicating the version of InChI used (currently A for version 1.)
    Finally, the single character at the end indicates the protonation of the core parent structure,
    corresponding to the /p sublayer of the charge layer (N for no protonation, O, P, ... if protons should
     be added and M, L, ... if they should be removed.)

     14, 10 and one character(s), respectively, like XXXXXXXXXXXXXX-YYYYYYYYFV-P
     */

    private static Pattern PATTERN = Pattern.compile("^[A-Z]{14}\\-[A-Z]{10}\\-[A-Z]$");

    /**
     * Is this input String match the Inchi Key pattern XXXXXXXXXXXXXX-YYYYYYYYFV-P.
     * @param s the string to check.
     * @return {@code true} if it does; {@code false} otherwise.
     * @throws NullPointerException if s is null.
     */
    static boolean isValidInchiKey(String s){
        Objects.requireNonNull(s);
        return PATTERN.matcher(s).matches();
    }

    /**
     * Returns the substring of the connectivity layer (the first 14 chars)
     * @param inchiKey
     * @return
     */
    static String getConnectivityLayer(String inchiKey){
        return inchiKey.substring(0,14);
    }


}

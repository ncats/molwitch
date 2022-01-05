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

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * An object representation of an inchi key
 * with helper methods for pulling out various parts of an inchi key.
 */
public class InchiKey {
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

    private final String data;

    private final String connectivity;

    /**
     * Compute the InchiKey from the given mol file.
     * @param mol the mol formatted record to parse as a String.
     * @return an Optional wrapped InchiKey
     * @throws IOException if there is an error compting the inchi or inchi key.
     * @throws NullPointerException if mol is null.
     */
    public Optional<InchiKey> computeFromMol(String mol) throws IOException {
        return Inchi.computeInchiFromMol(mol).getInchiKey();
    }

    /**
     * Create an InchiKey object from an inchi key as a String.
     * @param data inchi key as a String in the format <pre>XXXXXXXXXXXXXX-YYYYYYYYFV-P</pre>.
     * @throws NullPointerException if data is null.
     * @throws IllegalArgumentException if data does not match the inchi key format
     */
    public InchiKey(String data) {

        if(!PATTERN.matcher(data).matches()){
            throw new IllegalArgumentException("not in inchi key format: "+ data);
        }
        this.data = Objects.requireNonNull(data);
        this.connectivity = data.substring(0, 14);

    }

    @Override
    public String toString() {
        return data;
    }

    /**
     * Get the Inchi version
     * @return
     */
    public int getVersion(){
        //A is 1, B is 2 etc
        return data.charAt(24) -'@';
    }

    /**
     * Is this a "standard inchi".
     * @return {@code true} if it is a standard inchi; {@code false} otherwise.
     */
    public boolean isStandard(){
        return data.charAt(23)=='S';
    }

    /**
     * Does this inchi contain stereo elements.
     * @return {@code true} if it has stereo; {@code false} otherwise.
     */
    public boolean hasStereo(){
        return !"UHFFFAOYSA".equals(data.substring(15, 25));
    }
    public int getProtonation(){
        //N is no protonation and any letter lower is - the difference and any letter after is + the difference
         return data.charAt(26) -'N';
    }

    public String getConnectivityLayer(){
        return connectivity;
    }

    public boolean hasSameConnectivity(InchiKey other){
        return getConnectivityLayer().equals(other.getConnectivityLayer());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InchiKey inchiKey = (InchiKey) o;
        return data.equals(inchiKey.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}

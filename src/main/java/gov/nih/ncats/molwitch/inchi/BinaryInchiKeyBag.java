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

import gov.nih.ncats.common.io.InputStreamSupplier;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.*;

/**
 * A collection of InchiKeys stored in an in-memory compact binary way.
 */
public class BinaryInchiKeyBag implements InchiKeyBag {
    /**
     * Our sorted array of inchis in big integer form for easy searching.
     */
    private final BigInteger[] array;

    /**
     * Parse the given file of sorted inchi keys, one per line
     *
     * @param sortedInchikeyFile the file or sorted inchi keys
     * @return a new BinaryInchiKeyBag object containing all those inchi keys.
     * @throws IOException if there is a problem reading the file.
     */
    public static BinaryInchiKeyBag fromSortedFile(File sortedInchikeyFile) throws IOException {
        BigInteger[] a = parseInchiKeyFile(sortedInchikeyFile);
        return new BinaryInchiKeyBag(a);
    }

    /**
     * Parse the given file of UNsorted inchi keys, one per line.  Note
     * that this method will have a big performance hit as it must sort
     * the inchi keys it parses so if you can presort the file that should be the
     * preferred way.
     *
     * @param unsortedInchikeyFile the file or unsorted inchi keys
     * @return a new BinaryInchiKeyBag object containing all those inchi keys.
     * @throws IOException if there is a problem reading the file.
     * @see #fromUnsortedFile(File)
     */
    public static BinaryInchiKeyBag fromUnsortedFile(File unsortedInchikeyFile) throws IOException {
        BigInteger[] a = parseInchiKeyFile(unsortedInchikeyFile);
        Arrays.sort(a);
        return new BinaryInchiKeyBag(a);
    }

    private static BigInteger[] parseInchiKeyFile(File sortedInchikeyFile) throws IOException {
        InputStreamSupplier supplier = InputStreamSupplier.forFile(sortedInchikeyFile);
        //read the whole file 2x - first to get the number of records to efficiently size our array
        //TODO should we check for blank lines or commented out lines maybe with # ?
        int count = 0;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(supplier.get()))) {

            while (in.readLine() != null) {
                count++;
            }
        }
        BigInteger[] a = new BigInteger[count];
        count = 0;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(supplier.get()))) {
            String line;
            while ((line = in.readLine()) != null) {
                a[count++] = BinaryInchiKeyUtil.encode(line);
            }
        }
        return a;
    }

    /**
     * Create a new BinaryInchiKeyBag from the given Sorted Set of
     * inchi keys as Strings.
     *
     * @param inchiKeys a Set of inchi keys no elements in the set can be null.
     * @return a new BinaryInchiKeyBag object containing all those inchi keys.
     * @throws NullPointerException if any elements in set are null.
     */
    public static BinaryInchiKeyBag fromSortedSet(SortedSet<String> inchiKeys) {
        BigInteger[] a = new BigInteger[inchiKeys.size()];
        Iterator<String> iter = inchiKeys.iterator();
        int i = 0;
        while (iter.hasNext()) {
            a[i++] = BinaryInchiKeyUtil.encode(iter.next());
        }
        return new BinaryInchiKeyBag(a);
    }

    /**
     * Create a new BinaryInchiKeyBag from the given of presorted
     * inchi keys as Strings.  Note that this method doesn't check
     * if the list is sorted as a performance improvement,
     * so you must make sure the list is sorted otherwise the inchiKeyBag
     * will be corrupted and might return incorrect values.
     *
     * @param inchiKeys a Set of inchi keys no elements in the set can be null.
     * @return a new BinaryInchiKeyBag object containing all those inchi keys.
     * @throws NullPointerException if any elements in set are null.
     */
    public static BinaryInchiKeyBag fromSortedList(List<String> inchiKeys) {
        BigInteger[] a = new BigInteger[inchiKeys.size()];
        Iterator<String> iter = inchiKeys.iterator();
        int i = 0;
        while (iter.hasNext()) {
            a[i++] = BinaryInchiKeyUtil.encode(iter.next());
        }
        return new BinaryInchiKeyBag(a);
    }

    /**
     * Helper method that is the same as:
     * {@code return fromSortedSet(new TreeSet<>(inchiKeys));}.
     *
     * @param inchiKeys a Set of inchi keys no elements in the set can be null.
     * @return a new BinaryInchiKeyBag object containing all those inchi keys.
     * @throws NullPointerException if any elements in set are null.
     */
    public static BinaryInchiKeyBag fromUnSortedCollection(Collection<String> inchiKeys) {
        return fromSortedSet(new TreeSet<>(inchiKeys));
    }

    private BinaryInchiKeyBag(BigInteger[] array) {
        this.array = array;
    }

    /**
     * Look to see if the given inchi key is contained in this Bag.
     *
     * @param inchiKey the inchi key to look for; can not be null.
     * @return an {@link InchiKeySearchResult} which will have the passed in key
     * and the ResultType if it's found or not found and if it's exact or inexact.
     * @throws NullPointerException if inchiKey is null.
     */
    @Override
    public InchiKeySearchResult find(String inchiKey) {
        BigInteger exact = BinaryInchiKeyUtil.encode(inchiKey);
        if (Arrays.binarySearch(array, exact) >= 0) {
            return new InchiKeySearchResult(inchiKey, InchiKeySearchResultType.EXACT);
        }
        BigInteger lowInsensitive = exact.and(BinaryInchiKeyUtil.MASK);
        BigInteger highInsensitive = lowInsensitive.add(BinaryInchiKeyUtil.SHIFT);
        if (containsInsensitive(lowInsensitive, highInsensitive)) {
            return new InchiKeySearchResult(inchiKey, InchiKeySearchResultType.STEREO_INSENSITIVE);
        }
        return new InchiKeySearchResult(inchiKey, InchiKeySearchResultType.NO_MATCH);

    }

    @Override
    public boolean contains(String inchiKey) {
        BigInteger value = BinaryInchiKeyUtil.encode(inchiKey);
        return Arrays.binarySearch(array, value) >= 0;
    }

    @Override
    public boolean containsInsensitive(String inchiKey) {
        //stereo insensitive
        BigInteger[] pair = BinaryInchiKeyUtil.encodeInsensitive(inchiKey);
        return containsInsensitive(pair[0], pair[1]);
    }

    private boolean containsInsensitive(BigInteger low, BigInteger high) {
        int lowResult = Arrays.binarySearch(array, low);
        int highResult = Arrays.binarySearch(array, high);
        if (lowResult == highResult) {
            //no match
            return false;
        }
        if (Math.abs(lowResult) < Math.abs(highResult)) {
            //there is something in between low and high so we have an insensitive match
            return true;
        }
        return false;
    }

}

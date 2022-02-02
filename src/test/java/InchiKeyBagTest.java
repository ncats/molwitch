/*
 * NCATS-MOLWITCH
 *
 * Copyright 2021 NIH/NCATS
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

import gov.nih.ncats.molwitch.inchi.BinaryInchiKeyBag;
import gov.nih.ncats.molwitch.inchi.DefaultInchiKeyBag;
import gov.nih.ncats.molwitch.inchi.InchiKeyBag;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;
@RunWith(Parameterized.class)
public class InchiKeyBagTest {

    //Junit 4 Rule/ CLassRule is not invoked until AFTER parameters method
    //so we can't use the rule annotations here so we invoke the create and delete
    //directly in parameters method
    private static TemporaryFolder tmpDir = new TemporaryFolder();
    private InchiKeyBag sut;

    public InchiKeyBagTest(String usedByParameterTestName, InchiKeyBag b){
        this.sut = b;
    }

    @Parameterized.Parameters(name= "{0}")
    public static List<Object[]> setup() throws IOException{
        tmpDir.create();
        try {
            TreeSet<String> set = createDataSet();
            List<Object[]> list = new ArrayList<>();

            list.add(new Object[]{"Binary sortedSet", BinaryInchiKeyBag.fromSortedSet(set)});

            list.add(new Object[]{"Binary sortedList", BinaryInchiKeyBag.fromSortedList(new ArrayList<>(set))});

            list.add(new Object[]{"Binary sorted File", BinaryInchiKeyBag.fromSortedFile(writeToFile(set))});

            List<String> shuffledList = new ArrayList<>(set);
            Collections.shuffle(shuffledList);

            list.add(new Object[]{"Binary unsortedSet", BinaryInchiKeyBag.fromUnSortedCollection(shuffledList)});


            list.add(new Object[]{"Binary unsortedFile", BinaryInchiKeyBag.fromUnsortedFile(writeToFile(shuffledList))});
            list.add(new Object[]{"Default sortedSet", DefaultInchiKeyBag.fromCollection(set)});

            return list;
        }finally{
            tmpDir.delete();
        }
    }

    private static File writeToFile(Collection<String> collection) throws IOException {
        File f = tmpDir.newFile();
        try(PrintWriter writer = new PrintWriter(new FileWriter(f))){
            for(String s : collection){
                writer.println(s);
            }
        }
        return f;
    }

    private static TreeSet<String> createDataSet() {
        TreeSet<String> set = new TreeSet<>();
        /*
        GLDQAMYCGOIJDV-UHFFFAOYSA-N
QZDSXQJWBGMRLU-UHFFFAOYSA-N
VUQLHQFKACOHNZ-UHFFFAOYSA-N
NMDWGEGFJUBKLB-UHFFFAOYSA-N
         */
        set.add("GLDQAMYCGOIJDV-UHFFFAOYSA-N");
        set.add("QZDSXQJWBGMRLU-UHFFFAOYSA-N");
        set.add("VUQLHQFKACOHNZ-UHFFFAOYSA-N");
        set.add("NMDWGEGFJUBKLB-UHFFFAOYSA-N");

        /*
        AAAACIDXVXFWLZ-IIZQIQNSSA-N
AAAACIDXVXFWLZ-NTFBLANLSA-N
AAAACIDXVXFWLZ-UHFFFAOYSA-N
AAAACIDXVXFWLZ-WIMVFMHDSA-N
AAAACIDXVXFWLZ-YGJKTCCFSA-N
         */
        set.add("AAAACIDXVXFWLZ-IIZQIQNSSA-N");
        set.add("AAAACIDXVXFWLZ-NTFBLANLSA-N");
        set.add("AAAACIDXVXFWLZ-UHFFFAOYSA-N");
//        set.add("AAAACIDXVXFWLZ-WIMVFMHDSA-N");
        set.add("AAAACIDXVXFWLZ-YGJKTCCFSA-N");
        return set;
    }

    @Test
    public void containsStereo(){
        assertTrue(sut.contains("GLDQAMYCGOIJDV-UHFFFAOYSA-N"));
        assertTrue(sut.contains("QZDSXQJWBGMRLU-UHFFFAOYSA-N"));
        assertTrue(sut.contains("VUQLHQFKACOHNZ-UHFFFAOYSA-N"));
        assertTrue(sut.contains("NMDWGEGFJUBKLB-UHFFFAOYSA-N"));

        assertTrue(sut.contains("AAAACIDXVXFWLZ-IIZQIQNSSA-N"));
        assertTrue(sut.contains("AAAACIDXVXFWLZ-NTFBLANLSA-N"));
        assertTrue(sut.contains("AAAACIDXVXFWLZ-UHFFFAOYSA-N"));

        assertTrue(sut.contains("AAAACIDXVXFWLZ-YGJKTCCFSA-N"));
    }

    @Test
    public void containsStereoInsensitive(){
        assertTrue(sut.containsInsensitive("GLDQAMYCGOIJDV-UHFFFAOYSA-N"));
        assertTrue(sut.containsInsensitive("QZDSXQJWBGMRLU-UHFFFAOYSA-N"));
        assertTrue(sut.containsInsensitive("VUQLHQFKACOHNZ-UHFFFAOYSA-N"));
        assertTrue(sut.containsInsensitive("NMDWGEGFJUBKLB-UHFFFAOYSA-N"));

        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-IIZQIQNSSA-N"));
        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-NTFBLANLSA-N"));
        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-UHFFFAOYSA-N"));
        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-WIMVFMHDSA-N"));
        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-YGJKTCCFSA-N"));
    }

    @Test
    public void containsStereoInsensitiveProton(){
        assertTrue(sut.containsInsensitive("GLDQAMYCGOIJDV-UHFFFAOYSA-O"));
        assertTrue(sut.containsInsensitive("QZDSXQJWBGMRLU-UHFFFAOYSA-M"));
        assertTrue(sut.containsInsensitive("VUQLHQFKACOHNZ-UHFFFAOYSA-L"));
        assertTrue(sut.containsInsensitive("NMDWGEGFJUBKLB-UHFFFAOYSA-P"));

        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-IIZQIQNSSA-N"));
        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-NTFBLANLSA-M"));
        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-UHFFFAOYSA-O"));
        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-WIMVFMHDSA-P"));
    }

    @Test
    public void doesNotContainStereo(){
        assertFalse(sut.contains("RDJUHLUBPADHNP-UHFFFAOYSA-N"));
        //
        assertFalse(sut.contains("OVPRPPOVAXRCED-UHFFFAOYSA-N"));

        assertFalse(sut.contains("AAAACIDXVXFWLZ-WIMVFMHDSA-N"));
    }
    @Test
    public void doesNotContainStereoInsensitive(){
        assertFalse(sut.containsInsensitive("RDJUHLUBPADHNP-UHFFFAOYSA-N"));
        assertFalse(sut.containsInsensitive("OVPRPPOVAXRCED-UHFFFAOYSA-N"));

    }
    @Test
    public void doesNotContainStereoInsensitiveProtonLayer(){
        assertFalse(sut.containsInsensitive("RDJUHLUBPADHNP-UHFFFAOYSA-M"));
        assertFalse(sut.containsInsensitive("OVPRPPOVAXRCED-UHFFFAOYSA-L"));
        assertFalse(sut.containsInsensitive("OVPRPPOVAXRCED-UHFFFAOYSA-O"));
        assertFalse(sut.containsInsensitive("OVPRPPOVAXRCED-UHFFFAOYSA-P"));
    }

    @Test
    public void containsCheckResult(){
        assertEquals(BinaryInchiKeyBag.InchiKeySearchResultType.EXACT, sut.find("GLDQAMYCGOIJDV-UHFFFAOYSA-N").getResultType());
        assertEquals(BinaryInchiKeyBag.InchiKeySearchResultType.EXACT, sut.find("QZDSXQJWBGMRLU-UHFFFAOYSA-N").getResultType());
        assertEquals(BinaryInchiKeyBag.InchiKeySearchResultType.EXACT, sut.find("VUQLHQFKACOHNZ-UHFFFAOYSA-N").getResultType());
        assertEquals(BinaryInchiKeyBag.InchiKeySearchResultType.EXACT, sut.find("NMDWGEGFJUBKLB-UHFFFAOYSA-N").getResultType());

        assertEquals(BinaryInchiKeyBag.InchiKeySearchResultType.EXACT, sut.find("AAAACIDXVXFWLZ-IIZQIQNSSA-N").getResultType());
        assertEquals(BinaryInchiKeyBag.InchiKeySearchResultType.EXACT, sut.find("AAAACIDXVXFWLZ-NTFBLANLSA-N").getResultType());
        assertEquals(BinaryInchiKeyBag.InchiKeySearchResultType.EXACT, sut.find("AAAACIDXVXFWLZ-UHFFFAOYSA-N").getResultType());

        assertEquals(BinaryInchiKeyBag.InchiKeySearchResultType.EXACT, sut.find("AAAACIDXVXFWLZ-YGJKTCCFSA-N").getResultType());
            //these are fake stereo parts
        assertEquals(BinaryInchiKeyBag.InchiKeySearchResultType.STEREO_INSENSITIVE, sut.find("GLDQAMYCGOIJDV-SDFDSFDFDF-N").getResultType());
        assertEquals(BinaryInchiKeyBag.InchiKeySearchResultType.STEREO_INSENSITIVE, sut.find("QZDSXQJWBGMRLU-SDFDSFDFDF-N").getResultType());
        assertEquals(BinaryInchiKeyBag.InchiKeySearchResultType.STEREO_INSENSITIVE, sut.find("VUQLHQFKACOHNZ-SDFDSFDFDF-N").getResultType());
        assertEquals(BinaryInchiKeyBag.InchiKeySearchResultType.STEREO_INSENSITIVE, sut.find("NMDWGEGFJUBKLB-SDFDSFDFDF-N").getResultType());

        assertEquals(BinaryInchiKeyBag.InchiKeySearchResultType.STEREO_INSENSITIVE, sut.find("AAAACIDXVXFWLZ-SDFDSFDFDF-N").getResultType());
       assertEquals(BinaryInchiKeyBag.InchiKeySearchResultType.STEREO_INSENSITIVE, sut.find("AAAACIDXVXFWLZ-WIMVFMHDSA-N").getResultType());


        assertEquals(BinaryInchiKeyBag.InchiKeySearchResultType.NO_MATCH, sut.find("RDJUHLUBPADHNP-UHFFFAOYSA-N").getResultType());
        assertEquals(BinaryInchiKeyBag.InchiKeySearchResultType.NO_MATCH, sut.find("OVPRPPOVAXRCED-UHFFFAOYSA-N").getResultType());
    }


}

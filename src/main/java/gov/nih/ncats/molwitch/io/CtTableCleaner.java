/*
 * NCATS-MOLWITCH
 *
 * Copyright 2020 NIH/NCATS
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

package gov.nih.ncats.molwitch.io;

import gov.nih.ncats.common.io.InputStreamSupplier;
import gov.nih.ncats.common.iter.CloseableIterator;
import gov.nih.ncats.molwitch.SGroup;

import java.io.*;
import java.util.Map;

/**
 * Utility class to fix or "clean up" v2000 mol and sdfile formatted
 * data by fixing common problems that happen from
 * copy and paste errors or known tools and produce invalid results.
 *
 * <p/>
 * Current Fixes include:
 * <ul>
 *     <li>Correcting header if it has too few lines</li>
 *     <li>fix additional whitespace in leading or trailing records</li>
 *     <li>fix incorrect leading whitespace in Atom Block</li>
 *     <li>fix incorrect leading whitespace in Bond List</li>
 *     <li>Correctly format the Counts Header Line</li>
 *     <li>remove additional whitespace between M  END block and the property block</li>
 *     <li>Remove S-Group lines that reference non-existent S-Groups</li>
 *     <li>Remove S-Group lines that reference invalid  S-Group types</li>
 *     <li>Remove S-Group parent atoms if S-Group type isn't MUL</li>
 *     <li>Remove STY group types that have already been declared</li>
 *     <li>break M  SAL lines with more than 15 atoms into multiple lines with at most 8 atoms per line</li>
 *     <li>break M  SAL lines with atom positions &lt; 1 are removed from the atom list</li>
 *     <li>break M  CHG lines with more than 8 charges into multiple lines with at most 8 charges per line</li>
 *     <li>break M  STY lines with more than 8 Sgroups into multiple lines with at most 8 S-groups per line</li>
 *     <li>break S-Group EXP lines with more than 15 into multiple lines with at most 15 per line</li>
 *     <li>Removes final newline after $$$$ in Sdfiles if any.</li>
 *     <li>the M  END line is forced to have 2 spaces between M and END.</li>
 *     <li>Data Sgroups with a SCD line without a SED line have the last SCD line converted into an SED line</li>
 *     <li>Data Sgroups SCD and SED lines are reformatted to be max 69 characters of data per line</li>
 *     <li>ignore anything after the M  END line start, as it sometimes is added by accident in a few tools</li>
 * </ul>
 */
public final class CtTableCleaner {

    private CtTableCleaner(){
        //can not instantiate
    }


    public static String clean(String molOrSdText) throws IOException{
        StringWriter out = new StringWriter(molOrSdText.length());
        try(BufferedReader reader = new BufferedReader(new StringReader(molOrSdText));
            BufferedWriter writer = new BufferedWriter(out);
        ){
            SdfUtil.copyClean(reader, writer);
        }
        return out.toString();
    }
    public static CloseableIterator<String> clean(File molOrSdFile) throws IOException{
        return clean(InputStreamSupplier.forFile(molOrSdFile).get());

    }
    public static CloseableIterator<String> clean(InputStream molOrSdInputStream) throws IOException{

        return new SdfUtil.CleanSdfIterator(new BufferedReader(new InputStreamReader(molOrSdInputStream)));

    }
    public static CloseableIterator<String> clean(BufferedReader molOrSdReader) throws IOException{

        return new SdfUtil.CleanSdfIterator(molOrSdReader);

    }
}

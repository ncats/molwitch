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

import java.io.*;

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
 *     <li>remove additional whitespace between M  END block and the property block</li>
 *     <li>Remove S-Group lines that reference non-existent S-Groups</li>
 *     <li>Remove S-Group lines that reference invalid  S-Group types</li>
 *     <li>Remove S-Group parent atoms if S-Group type isn't MUL</li>
 *     <li>break M  CHG lines with more than 8 charges into multiple lines with at most 8 charges per line</li>
 *     <li>break M  STY lines with more than 8 Sgroups into multiple lines with at most 8 S-groups per line</li>
 *     <li>Removes final newline after $$$$ in Sdfiles if any.</li>
 *     <li>the M  END line is forced to have 2 spaces between M and END.</li>
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
}

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

import java.io.*;

import gov.nih.ncats.molwitch.io.ChemFormat.ChemFormatWriterSpecification;
import org.apache.commons.io.Charsets;

public interface ChemicalWriterImplFactory {
	/**
	 * Write the given Chemical with the given format specification
	 * and return the result as a String.
	 *
	 * @implNote  by default this executes the code below
	 * but should be overridden if there is a more efficient way:
	 * <pre>
	 * {@code
	 * ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
	 * try(ChemicalWriterImpl writer = newInstance(out, spec)){
	 *     writer.write(chemicalImpl);
	 * }
	 * return new String(out.toByteArray());
	 * }
	 * </pre>
	 * @param chemicalImpl
	 * @param spec
	 * @return
	 * @throws IOException
	 * @since 0.6.0
	 */
	default String writeAsString(ChemicalImpl chemicalImpl, ChemFormatWriterSpecification spec) throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		try(ChemicalWriterImpl writer = newInstance(out, spec)){
			writer.write(chemicalImpl);
		}
		return new String(out.toByteArray(), Charsets.UTF_8);
	}
	ChemicalWriterImpl newInstance(OutputStream out, ChemFormatWriterSpecification spec) throws IOException;
	

	default ChemicalWriterImpl newInstance(File outputFile, ChemFormatWriterSpecification spec) throws IOException{
		return newInstance(new BufferedOutputStream(new FileOutputStream(outputFile)), spec);
	}

	boolean supports(ChemFormatWriterSpecification spec);
}

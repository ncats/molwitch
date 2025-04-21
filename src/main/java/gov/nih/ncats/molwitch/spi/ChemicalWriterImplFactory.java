/*
 * NCATS-MOLWITCH
 *
 * Copyright 2025 NIH/NCATS
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

import gov.nih.ncats.common.io.IOUtil;
import gov.nih.ncats.molwitch.io.ChemFormat.ChemFormatWriterSpecification;
import org.apache.commons.io.Charsets;

/**
 * A Factory for writing out {@link gov.nih.ncats.molwitch.Chemical}s.
 */
public interface ChemicalWriterImplFactory {
	/**
	 * Write the given Chemical with the given format specification
	 * and return the result as a String.
	 *
	 * <strong>Implementation Note</strong>: by default this executes the code below
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
	 * @param chemicalImpl the {@link ChemicalImpl} to write; should never be null.
	 * @param spec the {@link ChemFormatWriterSpecification} for how to write this chemical out.
	 * @return the encoded data as a String.
	 * @throws IOException if there is a problem writing or encoding the given chemical to the given spec.
	 * @since 0.6.0
	 */
	default String writeAsString(ChemicalImpl chemicalImpl, ChemFormatWriterSpecification spec) throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		try(ChemicalWriterImpl writer = newInstance(out, spec)){
			writer.write(chemicalImpl);
		}
		return new String(out.toByteArray(), Charsets.UTF_8);
	}

	/**
	 * Create a new {@link ChemicalWriterImpl} that will write out Chemicals
	 * using the encoding specified by the {@link ChemFormatWriterSpecification}
	 * that will write to the given OutputStream.
	 * @param out the OutputStream to write to; will never be null.
	 * @param spec the {@link ChemFormatWriterSpecification} to use for encoding; will never be null.
	 * @return a {@link ChemicalWriterImpl} should never be null.
	 * @throws IOException if there is a problem creating the writer.
	 */
	ChemicalWriterImpl newInstance(OutputStream out, ChemFormatWriterSpecification spec) throws IOException;

	/**
	 * Create a new {@link ChemicalWriterImpl} that will write out Chemicals
	 * using the encoding specified by the {@link ChemFormatWriterSpecification}
	 * that will write to the given File.
	 * @param outputFile the File to write to; will never be null.  If the given file or any parent directories
	 *                   do not exist, then they will try to be created.
	 * @param spec the {@link ChemFormatWriterSpecification} to use for encoding; will never be null.
	 * @return a {@link ChemicalWriterImpl} should never be null.
	 * @throws IOException if there is a problem creating the writer.
	 */
	default ChemicalWriterImpl newInstance(File outputFile, ChemFormatWriterSpecification spec) throws IOException{

		return newInstance(IOUtil.newBufferedOutputStream(outputFile), spec);
	}

	/**
	 * Does this factory support creating writers of the given specification.
	 * @param spec the {@link ChemFormatWriterSpecification} the user wants use to to encode {@link gov.nih.ncats.molwitch.Chemical}s.
	 *
	 * @return {@code true} if this factory supports the given spec; {@code false} otherwise.
	 */
	boolean supports(ChemFormatWriterSpecification spec);
}

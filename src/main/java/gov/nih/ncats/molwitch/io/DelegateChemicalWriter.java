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

package gov.nih.ncats.molwitch.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.molwitch.io.ChemFormat.ChemFormatWriterSpecification;
import gov.nih.ncats.molwitch.spi.ChemicalWriterImpl;
import gov.nih.ncats.molwitch.spi.ChemicalWriterImplFactory;

class DelegateChemicalWriter implements ChemicalWriter{

	private final ChemicalWriterImpl delegate;
	
	

	public DelegateChemicalWriter(ChemicalWriterImplFactory factory, OutputStream out, ChemFormatWriterSpecification spec) throws IOException{
		Objects.requireNonNull(factory);
		Objects.requireNonNull(out);
		delegate = factory.newInstance(out, spec);
	}
	
	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public void write(Chemical chemical) throws IOException {
		try{
			delegate.write(chemical.getImpl());
		}catch(Throwable t){
		    t.printStackTrace();
			//sometimes the impl writer throws unchecked
			//exceptions which slip through so catch all throwables
			throw new IOException(t);
		}
	}

	

}

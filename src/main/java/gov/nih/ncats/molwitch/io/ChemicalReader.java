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

package gov.nih.ncats.molwitch.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.common.io.IOUtil;
import gov.nih.ncats.common.stream.ThrowingStream;
/**
 * Read {@link Chemical}s from some kind of datasource,
 * often a file.  You must always close
 * the {@link ChemicalReader} when you are finished with it
 * in case the datasource has to clean up any open resources.
 * <p>
 * It is advised you use a try-with-resource block.
 * 
 * @author katzelda
 *
 */
public interface ChemicalReader extends Closeable{
	/**
	 * Can more {@link Chemical}s be read.
	 * 
	 * @return {@code true} if there are more
	 * {@link Chemical}s to be read;
	 * {@code false} otherwise.
	 */
	boolean canRead();
	/**
	 * Read the next {@link Chemical}.
	 * 
	 * @return a {@link Chemical} object.
	 * 
	 * @throws IOException if there is a problem reading the next {@link Chemical}.
	 * 
	 * @throws NoSuchElementException if there are no more {@link Chemical}s left
	 * ( {@link #canRead()} returns {@code false}.
	 * 
	 * #see {@link #canRead()}
	 */
	Chemical read() throws IOException, NoSuchElementException;
	
	default ThrowingStream<Chemical> stream(){
		Spliterator<Chemical> spliterator = new Spliterator<Chemical>(){

			@Override
			public boolean tryAdvance(Consumer<? super Chemical> action) {
				if(!canRead()){
					return false;
				}
				Chemical c;
				try {
					c = read();
					action.accept(c);
					return true;
				} catch (NoSuchElementException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				
			}

			@Override
			public Spliterator<Chemical> trySplit() {
				return null;
			}

			@Override
			public long estimateSize() {
				return Long.MAX_VALUE;
			}

			@Override
			public int characteristics() {
				return Spliterator.ORDERED;
			}
			
		};
		
		return ThrowingStream.createFrom(spliterator, false)
				.onClose(() -> IOUtil.closeQuitely(this));
	}
}

/*
 * NCATS-WITCH
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

package gov.nih.ncats.witch;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FileSource implements ChemicalSource{

	private final File f;
	private final Type type;
	private final Map<String, String> properties = new HashMap<>();
	public FileSource(File f, Type type) {
		this.f = f;
		this.type = type;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String getData() {
		return f.getAbsolutePath();
	}

	@Override
	public Map<String, String> getProperties() {
		return properties;
	}

}

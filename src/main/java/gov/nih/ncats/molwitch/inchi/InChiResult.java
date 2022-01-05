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

package gov.nih.ncats.molwitch.inchi;

import java.util.Objects;
import java.util.Optional;

public final class InChiResult {

	private final String key;
	private final String auxInfo;
	private final String inchi;
	private final String message;
	private final Status status;
	
	public enum Status{
		VALID,
		WARNING,
		ERROR
	}

	@Override
	public String toString() {
		return "InChiResult{" +
				"key='" + key + '\'' +
				", auxInfo='" + auxInfo + '\'' +
				", inchi='" + inchi + '\'' +
				", message='" + message + '\'' +
				", stuatus=" + status +
				'}';
	}

	private InChiResult(Status status, String key, String inchi, String auxInfo, String message) {
		Objects.requireNonNull(status);
		Objects.requireNonNull(key);
		Objects.requireNonNull(inchi);
		this.status = status;
		
		this.key = key;
		this.inchi = inchi;
		this.auxInfo = auxInfo;
		this.message = message;
	}
	
	
	public String getMessage() {
		return message;
	}

	/**
	 * Get the {@link InchiKey} object.
	 * @return an Optional wrapped {@link InchiKey} object as for this inchi key.
	 * @see #getKey()
	 */
	public Optional<InchiKey> getInchiKey(){
		if(this.status == Status.ERROR ||  key==null || key.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(new InchiKey(key));
	}
	/**
	 * Get the Inchi key as a String.
	 * @return the inchi key as a String.
	 * @see #getInchiKey()
	 */
	public String getKey() {
		return key;
	}
	public String getAuxInfo() {
		return auxInfo;
	}
	public String getInchi() {
		return inchi;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + inchi.hashCode();
		result = prime * result + key.hashCode();
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof InChiResult)) {
			return false;
		}
		InChiResult other = (InChiResult) obj;
		if (inchi == null) {
			if (other.inchi != null) {
				return false;
			}
		} else if (!inchi.equals(other.inchi)) {
			return false;
		}
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!key.equals(other.key)) {
			return false;
		}
		return true;
	}


	public static final class Builder{
		private String key;
		private String auxInfo;
		private String inchi;
		private final Status status;
		private String message;
		
		public Builder(Status status){
			Objects.requireNonNull(status);
			this.status = status;
			
		}
		
		public Builder setKey(String key) {
			Objects.requireNonNull(key);
			this.key = key;
			
			return this;
		}
		public Builder setAuxInfo(String auxInfo) {
			Objects.requireNonNull(auxInfo);
			this.auxInfo = auxInfo;
			
			return this;
		}
		public Builder setInchi(String inchi) {
			Objects.requireNonNull(inchi);
			this.inchi = inchi;
			
			return this;
		}
		
		public Builder setMessage(String message) {
			this.message = message;
			
			return this;
		}
		
		public InChiResult build(){
			return new InChiResult(status, key, inchi, auxInfo, message);
		}
	}
	
}

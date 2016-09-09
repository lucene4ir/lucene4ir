/*
 *  Copyright 2008 Joaquin Perez-Iglesias
 *  Copyright 2010 Diego Ceccarelli
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package lucene4ir.bm25f;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Parameters needed to calculate the BM25F relevance score.
 * 
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * @since 13/dec/2010
 */

public class BM25FParameters {

	public String mainField;

	private String[] fields;
	/*
	 * boosts on fields, you can boost more the match on a field rather than
	 * another
	 */
	private Map<String, Float> boosts;
	/*
	 * boosts on length, you can boost a record if a field length is similar to
	 * the average field length in the collection
	 */
	private Map<String, Float> bParams;

	float k1 = 1;

	@Override
	public BM25FParameters clone() {
		BM25FParameters clone = new BM25FParameters();
		clone.setK1(k1);
		clone.boosts = new HashMap<String, Float>(boosts);
		clone.bParams = new HashMap<String, Float>(bParams);
		clone.fields = fields;
		clone.mainField = mainField;
		return clone;
	}

	public BM25FParameters() {
		// default params
		boosts = new HashMap<String, Float>();
		bParams = new HashMap<String, Float>();
		fields = new String[0];
	};

	public float getBoost(String field) {
		return boosts.get(field);
	}

	/**
	 * @param fields
	 *            - the fields to set
	 */
	public void setFields(String[] fields) {
		this.fields = fields;
	}

	/**
	 * @return the fields
	 */
	public String[] getFields() {
		return fields;
	}

	/**
	 * @param boosts
	 *            - the boosts to set (see bm25f formula)
	 */
	public void setBoosts(Float[] boosts) {
		this.boosts = new HashMap<String, Float>();
		for (int i = 0; i < fields.length; i++) {
			this.boosts.put(fields[i], boosts[i]);
		}
	}

	/**
	 * @return the boosts (see bm25f formula)
	 */
	public Map<String, Float> getBoosts() {
		return boosts;
	}

	/**
	 * @param bParams
	 *            the bParams to set (see bm25f formula)
	 */
	public void setbParams(Float[] bParams) {
		this.bParams = new HashMap<String, Float>();
		for (int i = 0; i < fields.length; i++) {
			this.bParams.put(fields[i], bParams[i]);
		}
	}

	/**
	 * @return the bParams (see bm25f formula)
	 */
	public Map<String, Float> getbParams() {
		return bParams;
	}

	/**
	 * @return the k1
	 */
	public float getK1() {
		return k1;
	}

	/**
	 * @param k1
	 *            the k1 to set
	 */
	public void setK1(float k1) {
		this.k1 = k1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bParams == null) ? 0 : bParams.hashCode());
		result = prime * result + ((boosts == null) ? 0 : boosts.hashCode());
		result = prime * result + Arrays.hashCode(fields);
		result = prime * result + Float.floatToIntBits(k1);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BM25FParameters other = (BM25FParameters) obj;
		if (bParams == null) {
			if (other.bParams != null)
				return false;
		} else if (!bParams.equals(other.bParams))
			return false;
		if (boosts == null) {
			if (other.boosts != null)
				return false;
		} else if (!boosts.equals(other.boosts))
			return false;
		if (!Arrays.equals(fields, other.fields))
			return false;
		if (Float.floatToIntBits(k1) != Float.floatToIntBits(other.k1))
			return false;
		return true;
	}

	public String getMainField() {
		return mainField;
	}

	public void setMainField(String mainField) {
		this.mainField = mainField;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BM25FParameters [fields=" + Arrays.toString(fields)
				+ ", boosts=" + boosts + ", bParams=" + bParams + ", k1=" + k1
				+ "]";
	}

}

/**
 * Copyright (c) 2012-2013 "Indexia Technologies, ltd."
 *
 * This file is part of Antiquity.
 *
 * Antiquity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package co.indexia.antiquity.graph;

/**
 * Configuraiton provides a list of properties which defines the behavior of the associated {@link VersionedGraph}.
 */
public class Configuration {
	/**
	 * If true a private hash is calculated per vertex creation / modification
	 */
	public final Boolean privateVertexHashEnabled;

	/**
	 * If true an index is maintained for the id property of the vertex
	 */
	public final Boolean autoIndexVertexIdProperty;

	/**
	 * If true an empty transaction won't be versioned.
	 * 
	 * This is only relevant to transactional graphs
	 */
	public final Boolean doNotVersionEmptyTransactions;

	/**
	 * Create an instance of this class with the specified configuration properties.
	 * 
	 * @param privateVertexHashEnabled
	 * @param autoIndexVertexIdProperty
	 * @param doNotVersionEmptyTransactions
	 */
	public Configuration(Boolean privateVertexHashEnabled,
			Boolean autoIndexVertexIdProperty,
			Boolean doNotVersionEmptyTransactions) {
		this.privateVertexHashEnabled = privateVertexHashEnabled;
		this.autoIndexVertexIdProperty = autoIndexVertexIdProperty;
		this.doNotVersionEmptyTransactions = doNotVersionEmptyTransactions;
	}

	/**
	 * Create an instance of this class with default configuration.
	 */
	public Configuration() {
		privateVertexHashEnabled = true;
		autoIndexVertexIdProperty = false;
		doNotVersionEmptyTransactions = true;
	}

	/**
	 * Private vertex hash calculation.
	 * 
	 * The hash is calculated only for the vertex properties without taking its edges into account.
	 * 
	 * @return true if private hash should be calculated for vertices
	 */
	public Boolean getPrivateVertexHashEnabled() {
		return privateVertexHashEnabled;
	}

	/**
	 * Auto index for vertex identifier property key.
	 * 
	 * @return True if auto index for vertices property key should be enabled
	 */
	public Boolean getAutoIndexVertexIdProperty() {
		return autoIndexVertexIdProperty;
	}

	/**
	 * Skip versioning of empty transactions.
	 * 
	 * @return true if empty transactions should not be versioned.
	 */
	public Boolean getDoNotVersionEmptyTransactions() {
		return doNotVersionEmptyTransactions;
	}
}

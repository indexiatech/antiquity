/**
 * Copyright (c) 2012-2013 "Vertix Technologies, ltd."
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
package com.vertixtech.antiquity.graph;

/**
 * Configuraiton provides a list of properties which defines the behavior of the associated {@link VersionedGraph}.
 */
public class Configuration {
	/**
	 * If true a private hash is calculated per vertex creation / modification
	 */
	public final Boolean privateHashEnabled;

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

	public Configuration(Boolean privateHashEnabled,
			Boolean autoIndexVertexIdProperty,
			Boolean doNotVersionEmptyTransactions) {
		this.privateHashEnabled = privateHashEnabled;
		this.autoIndexVertexIdProperty = autoIndexVertexIdProperty;
		this.doNotVersionEmptyTransactions = doNotVersionEmptyTransactions;
	}

	public Configuration() {
		privateHashEnabled = true;
		autoIndexVertexIdProperty = false;
		doNotVersionEmptyTransactions = true;
	}

	public Boolean getPrivateHashEnabled() {
		return privateHashEnabled;
	}

	public Boolean getAutoIndexVertexIdProperty() {
		return autoIndexVertexIdProperty;
	}

	public Boolean getDoNotVersionEmptyTransactions() {
		return doNotVersionEmptyTransactions;
	}
}

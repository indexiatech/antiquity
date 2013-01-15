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
	public final Boolean privateHashEnabled;

	public Configuration(Boolean privateHashEnabled) {
		this.privateHashEnabled = privateHashEnabled;
	}

	public Configuration() {
		privateHashEnabled = true;
	}

	public Boolean getPrivateHashEnabled() {
		return privateHashEnabled;
	}
}

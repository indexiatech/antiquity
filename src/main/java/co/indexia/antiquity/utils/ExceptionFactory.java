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
package co.indexia.antiquity.utils;

import co.indexia.antiquity.graph.NotFoundException;

/**
 * A centralized location to instantiate application exceptions.
 */
public class ExceptionFactory {
	/**
	 * An exception that should be thrown when a property could not be found.
	 * 
	 * @param msg
	 *            The message of the exception
	 * @return An exception instance
	 */
	public static NotFoundException notFoundException(String msg) {
		return new NotFoundException(msg);
	}
}

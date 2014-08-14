/**
 * Copyright (c) 2012-2014 "Indexia Technologies, ltd."
 *
 * This file is part of Antiquity.
 *
 * Antiquity is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package co.indexia.antiquity.graph;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied on string fields that indicates a reserved property key.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ReservedKey {
    /**
     * true if this key should be copied from active to corresponding historic
     * element
     * 
     * @return true if this key should be copied from active to historic
     */
    boolean copiable();

    /**
     * Indicate that this key should be applied only to internal core elements
     * which are not user elements.
     * 
     * @return true if the key is internally used by antiquity
     */
    boolean internal();

    /**
     * The element class type this key is related to, Can be
     * {@link com.tinkerpop.blueprints.Vertex},
     * {@link com.tinkerpop.blueprints.Edge} or if applied to both:
     * {@link com.tinkerpop.blueprints.Element}
     * 
     * @return The element type this key is applied to
     */
    Class elementType();

    /**
     * true if the property is automatically indexed.
     */
    boolean indexed() default false;

    /**
     * Key element relevance: Can be ACTIVE / HISTORIC or INTERNAL elements
     * 
     * @return The relevance value
     */
    RestrictionType relevance();

    public enum RestrictionType {
        ACTIVE, HISTORIC, BOTH
    }
}

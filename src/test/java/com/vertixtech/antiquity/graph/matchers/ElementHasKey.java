/**
 * Copyright (c) 2012-2013 "Vertix Technologies, ltd."
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
package com.vertixtech.antiquity.graph.matchers;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Element;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher for element key existence
 */
public class ElementHasKey<T extends Element> extends TypeSafeMatcher<T> {
    private final String expected;

    protected ElementHasKey(String expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(T t) {
        return t.getPropertyKeys().contains(expected);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("expected: " + expected);
    }

    @Factory
    public static <T> Matcher<T> elementHasKey(String expected) {
        return new ElementHasKey(expected);
    }
}

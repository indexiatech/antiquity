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
package co.indexia.antiquity.graph.matchers;

import com.tinkerpop.blueprints.Element;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher for element key existence
 */
public class ElementHasProperty<T extends Element> extends TypeSafeMatcher<T> {
    private final String key;
    private final Object value;
    private Object got;

    protected ElementHasProperty(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    protected boolean matchesSafely(T t) {
        got = t.getProperty(key);
        if (got == null) {
            return false;
        }

        return got.equals(value);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("expected: ").appendText(key).appendText(":").appendText(value.toString())
                .appendText(" property.");
    }

    @Override
    protected void describeMismatchSafely(T item, org.hamcrest.Description mismatchDescription) {
        mismatchDescription.appendText("but got: ").appendText(got == null ? "null" : got.toString());
    }


    @Factory
    public static <T> Matcher<T> elementHasProperty(String key, Object value) {
        return new ElementHasProperty(key, value);
    }
}

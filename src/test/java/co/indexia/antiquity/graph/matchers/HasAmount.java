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

import com.google.common.collect.Lists;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher for elements amount
 */
public class HasAmount<T extends Iterable> extends TypeSafeMatcher<T> {
    private final int expected;
    private int got;

    protected HasAmount(int expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(T t) {
        this.got = Lists.newArrayList(t).size();
        return got == expected;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("amount: " + expected);
    }

    @Override
    protected void describeMismatchSafely(T item, org.hamcrest.Description mismatchDescription) {
        mismatchDescription.appendText("was: " + got);
    }

    @Factory
    public static <T> Matcher<T> hasAmount(int expected) {
        return new HasAmount(expected);
    }
}

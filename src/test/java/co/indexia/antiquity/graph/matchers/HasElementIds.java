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
import com.tinkerpop.blueprints.Element;
import co.indexia.antiquity.graph.HistoricVersionedElement;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import java.util.ArrayList;
import java.util.List;

/**
 * Matcher for element key existence
 * Note: comparison is done by IDs as string.
 */
public class HasElementIds<T extends Iterable<? extends Element>> extends TypeSafeMatcher<T> {
    private final TYPE type;
    private final ID id;
    private final List<Object> expected;
    private List<Object> ids = new ArrayList<Object>();

    public enum TYPE {
        CONTAINS, EXACTLY_MATCHES
    };

    public enum ID {
        ID, HARD_ID
    }

    protected HasElementIds(TYPE type, Object... expected) {
        this(ID.ID, type, expected);
    }

    protected HasElementIds(ID id, TYPE type, Object... expected) {
        this.type = type;
        this.id = id;
        this.expected = Lists.newArrayList(expected);
    }

    @Override
    protected boolean matchesSafely(T t) {
        for (Element e : t) {
            if (id == ID.ID) {
                ids.add(e.getId());
            } else {
                if (!(e instanceof HistoricVersionedElement)) {
                    throw new IllegalStateException("HARD_ID must be used with historic elements only");
                } else {
                    HistoricVersionedElement he = (HistoricVersionedElement)e;
                    ids.add(((HistoricVersionedElement) e).getHardId());
                }
            }
        }

        if (type == TYPE.CONTAINS) {
            return ids.containsAll(expected);
        } else if (type == TYPE.EXACTLY_MATCHES) {
            return (ids.containsAll(expected) && expected.containsAll(ids));
        }

        throw new IllegalStateException(("comparison type is unsupported."));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(expected.toString());
    }

    @Override
    protected void describeMismatchSafely(T item, org.hamcrest.Description mismatchDescription) {
        mismatchDescription.appendText(ids.toString());
    }

    @Factory
    public static <T> Matcher<T> elementIds(TYPE type, Object... expected) {
        return new HasElementIds(type, expected);
    }

    @Factory
    public static <T> Matcher<T> elementIds(ID id, TYPE type, Object... expected) {
        return new HasElementIds(id, type, expected);
    }
}

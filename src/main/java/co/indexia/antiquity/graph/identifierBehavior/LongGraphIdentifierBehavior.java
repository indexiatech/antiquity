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
package co.indexia.antiquity.graph.identifierBehavior;

import co.indexia.antiquity.graph.VEProps;

/**
 * A graph identifier behavior implementation for incremental version based on
 * {@link Long} type.
 */
public class LongGraphIdentifierBehavior extends BaseGraphIdentifierBehavior<Long> {
    @Override
    public Long getMinPossibleGraphVersion() {
        return (long) 1;
    }

    @Override
    public Long getMaxPossibleGraphVersion() {
        return Long.MAX_VALUE;
    }

    @Override
    public Long getLatestGraphVersion() {
        Long lastVer =
                (Long) getGraph().getRootVertex(VEProps.GRAPH_TYPE.HISTORIC).getProperty(
                        VEProps.LATEST_GRAPH_VERSION_PROP_KEY);
        if (lastVer == null) {
            return 0L;
        } else {
            return lastVer;
        }
    }

    @Override
    public Long getNextGraphVersion(Long currentVersion) {
        if (currentVersion == Long.MAX_VALUE)
            throw new IllegalStateException("Cannot get next version, long range has ended ");

        return currentVersion + 1;
    }
}

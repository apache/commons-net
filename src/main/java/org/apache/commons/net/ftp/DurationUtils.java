/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.ftp;

import java.time.Duration;

/** Temporary until Commons Lang 3.12.0. */
class DurationUtils {

    /**
     * Tests whether the given Duration is positive (&gt;0).
     *
     * @param duration the value to test
     * @return whether the given Duration is positive (&gt;0).
     */
    static boolean isPositive(final Duration duration) {
        return duration != null && !duration.isNegative() && !duration.isZero();
    }

    static int toMillisInt(final Duration duration) {
        final long millis = duration.toMillis();
        return millis > 0 ? (int) Math.min(millis, Integer.MAX_VALUE) : (int) Math.max(millis, Integer.MIN_VALUE);
    }

    static Duration zeroIfNull(final Duration controlIdle) {
        return controlIdle == null ? Duration.ZERO : controlIdle;
    }
}

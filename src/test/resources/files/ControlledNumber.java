/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.cli2.option;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An implementation of an Argument.
 */
public class ControlledNumber
        extends OptionImpl implements Argument {

    private static final char NUL = '\0';

    /**
     * The default value for the initial separator char.
     */
    public static final char DEFAULT_INITIAL_SEPARATOR = NUL;

    /**
     * The default value for the subsequent separator char.
     */
    public static final char DEFAULT_SUBSEQUENT_SEPARATOR = NUL;


    /**
     * Creates a new Argument instance.
     *
     * @param name                The name of the argument
     * @param description         A description of the argument
     * @param minimum             The minimum number of values needed to be valid
     * @param maximum             The maximum number of values allowed to be valid
     * @param initialSeparator    The char separating option from value
     * @param subsequentSeparator The char separating values from each other
     * @param validator           The object responsible for validating the values
     * @param consumeRemaining    The String used for the "consuming option" group
     * @param valueDefaults       The values to be used if none are specified.
     * @param id                  The id of the option, 0 implies automatic assignment.
     * @see OptionImpl#OptionImpl(int, boolean)
     */
    public ArgumentImpl(final String name,
                        final String description,
                        final int minimum,
                        final int maximum,
                        final char initialSeparator,
                        final char subsequentSeparator,
                        final Validator validator,
                        final String consumeRemaining,
                        final List valueDefaults,
                        final int id) {
        super(id, false);

        this.name = (name == null) ? "arg" : name;
        this.description = description;

        if ((valueDefaults != null) && (valueDefaults.size() > 0)) {
            if (valueDefaults.size() < minimum) {
                throw new IllegalArgumentException(resources.getMessage(ResourceConstants.ARGUMENT_TOO_FEW_DEFAULTS));
            }

            if (valueDefaults.size() > maximum) {
                throw new IllegalArgumentException(resources.getMessage(ResourceConstants.ARGUMENT_TOO_MANY_DEFAULTS));
            }
        }
    }


}


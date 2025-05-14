/*
 * Copyright (c) 2025 Robert Bosch Manufacturing Solutions GmbH
 *
 * See the AUTHORS file(s) distributed with this work for additional
 * information regarding authorship.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package com.boschsemanticstack.rql.model.v1.impl.preprocessor;

import java.util.List;

import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.model.v1.RqlFilterPreProcessor;
import com.boschsemanticstack.rql.model.v1.impl.RqlFilterImpl;

/**
 * This preprocessor replaces a NOT filter with NE operator with a filter
 * using EQ operator.
 *
 * <p>
 * Example:
 * <pre>
 * not(
 *    ne(attribute, "value1")
 * )
 * </pre>
 * becomes
 * <pre>
 *    eq(attribute, "value1")
 * </pre>
 */
public class NotNeToEqRqlFilterPreProcessor
      extends BaseRqlFilterPreProcessor implements RqlFilterPreProcessor {

   @Override
   public boolean matches( final RqlFilter filter ) {
      return filter.getFilterType() == RqlFilter.FilterType.NOT
             && isUnary( filter )
             && firstChildOperator( filter ) == RqlFilter.Operator.NE;
   }

   @Override
   public RqlFilter replaceSubTree( final RqlFilter filter ) {
      final String attribute = firstChildAttribute( filter );
      final List<Object> values = childValuesAsList( filter );
      return new RqlFilterImpl( attribute, RqlFilter.Operator.EQ, values );
   }
}

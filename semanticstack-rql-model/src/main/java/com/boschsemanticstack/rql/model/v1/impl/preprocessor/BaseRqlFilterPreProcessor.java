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

/**
 * Base class for RQL filter pre-processors.
 *
 * <p>
 * This class provides convenience methods to check the structure of RQL
 * filters and to extract information from them. It is intended to be
 * extended by specific pre-processor implementations.
 */
public class BaseRqlFilterPreProcessor {

   protected boolean isUnary( final RqlFilter filter ) {
      return filter.getChildCount() == 1;
   }

   protected boolean isBinary( final RqlFilter filter ) {
      return filter.getChildCount() == 2;
   }

   protected boolean allChildrenHaveOperator(
         final RqlFilter filter,
         final RqlFilter.Operator operator ) {
      return filter.getChildren().stream()
            .allMatch( child -> child.getOperator() == operator );
   }

   protected boolean allChildrenHaveSameAttribute( final RqlFilter filter ) {
      return filter.getChildren().stream()
                   .map( RqlFilter::getAttribute )
                   .distinct()
                   .count() == 1L;
   }

   protected RqlFilter.Operator firstChildOperator( final RqlFilter filter ) {
      return filter.getChildren().get( 0 ).getOperator();
   }

   protected String firstChildAttribute( final RqlFilter filter ) {
      return filter.getChildren().get( 0 ).getAttribute();
   }

   protected List<Object> childValuesAsList( final RqlFilter filter ) {
      return filter.getChildren().stream()
            .map( RqlFilter::getValue )
            .toList();
   }
}

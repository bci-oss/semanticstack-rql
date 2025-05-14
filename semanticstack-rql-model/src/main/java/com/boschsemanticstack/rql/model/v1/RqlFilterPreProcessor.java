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

package com.boschsemanticstack.rql.model.v1;

import java.util.List;

import com.boschsemanticstack.rql.model.v1.impl.RqlFilterImpl;

/**
 * A preprocessor for RQL filter expressions.
 * This interface is used to conditionally transform RQL filters by
 * replacing matching subtrees with the result of a predefined transformation.
 */
public interface RqlFilterPreProcessor {

   boolean matches( RqlFilter filter );

   /**
    * The entry point of the transformation.
    * This method will visit the subtree recursively and replace the matching parts.
    *
    * @param filter the subtree to visit
    * @return the transformed subtree.
    */
   default RqlFilter visit( final RqlFilter filter ) {
      if ( matches( filter ) ) {
         return replaceSubTree( filter );
      }
      if ( filter.getChildCount() == 0 ) {
         return filter;
      }
      final List<RqlFilter> originalChildren = filter.getChildren();
      final List<RqlFilter> visitedChildren = originalChildren.stream().map( this::visit ).toList();
      if ( originalChildren.equals( visitedChildren ) ) {
         return filter;
      }
      return new RqlFilterImpl( filter.getFilterType(), visitedChildren );
   }

   RqlFilter replaceSubTree( RqlFilter filter );

}

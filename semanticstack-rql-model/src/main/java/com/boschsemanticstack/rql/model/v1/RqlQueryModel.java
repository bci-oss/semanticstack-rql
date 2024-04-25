/*
 * Copyright (c)2024 Robert Bosch Manufacturing Solutions GmbH
 *
 *  See the AUTHORS file(s) distributed with this work for additional
 *  information regarding authorship.
 *
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 *  SPDX-License-Identifier: MPL-2.0
 */
package com.boschsemanticstack.rql.model.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.NotNull;

public interface RqlQueryModel extends RqlModelNode {

   @NotNull
   RqlSelect getSelect();

   Optional<RqlFilter> getFilter();

   @NotNull
   RqlOptions getOptions();

   @Override
   default <T> T accept( final RqlModelVisitor<? extends T> visitor ) {
      return visitor.visitModel( this );
   }

   @Override
   default List<? extends RqlModelNode> getChildren() {
      final List<RqlModelNode> result = new ArrayList<>();
      if ( !getSelect().isEmpty() ) {
         result.add( getSelect() );
      }
      getFilter().ifPresent( result::add );
      if ( !getOptions().isEmpty() ) {
         result.add( getOptions() );
      }
      return result;
   }

   @Override
   default int getChildCount() {
      return getChildren().size();
   }

   default boolean isEmpty() {
      return getChildren().isEmpty();
   }
}

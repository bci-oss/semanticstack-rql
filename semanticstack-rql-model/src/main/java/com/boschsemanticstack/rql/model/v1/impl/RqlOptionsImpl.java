/*
 * Copyright (c) 2024 Robert Bosch Manufacturing Solutions GmbH
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

package com.boschsemanticstack.rql.model.v1.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.boschsemanticstack.rql.model.v1.RqlCursor;
import com.boschsemanticstack.rql.model.v1.RqlModelNode;
import com.boschsemanticstack.rql.model.v1.RqlOptions;
import com.boschsemanticstack.rql.model.v1.RqlOrder;
import com.boschsemanticstack.rql.model.v1.RqlSlice;

import jakarta.validation.constraints.NotNull;

public class RqlOptionsImpl implements RqlOptions {

   private final RqlSlice slice;

   private final RqlCursor cursor;

   private final RqlOrder order;

   public RqlOptionsImpl( final RqlSlice slice, final RqlOrder order, final RqlCursor cursor ) {
      this.slice = slice;
      this.cursor = cursor;
      this.order = null == order ? new RqlOrderImpl( null ) : order;
   }

   @NotNull
   @Override
   public RqlOrder getOrder() {
      return order;
   }

   @Override
   public Optional<RqlSlice> getSlice() {
      return Optional.ofNullable( slice );
   }

   @Override
   public Optional<RqlCursor> getCursor() {
      return Optional.ofNullable( cursor );
   }

   @Override
   public boolean isEmpty() {
      return slice == null && cursor == null && order.isEmpty();
   }

   @Override
   public List<? extends RqlModelNode> getChildren() {
      final List<RqlModelNode> result = new ArrayList<>( 3 );
      if ( slice != null ) {
         result.add( slice );
      }
      if ( cursor != null ) {
         result.add( cursor );
      }
      if ( !order.fieldDirections().isEmpty() ) {
         result.add( order );
      }
      return result;
   }

   @Override
   public int getChildCount() {
      return getChildren().size();
   }

   public static RqlOptions emptyOptions() {
      return new RqlOptionsImpl( null, null, null );
   }
}

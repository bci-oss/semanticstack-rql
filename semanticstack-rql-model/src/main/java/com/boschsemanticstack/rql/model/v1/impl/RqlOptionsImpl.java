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

import com.boschsemanticstack.rql.model.v1.RqlModelNode;
import com.boschsemanticstack.rql.model.v1.RqlModelVisitor;
import com.boschsemanticstack.rql.model.v1.RqlOptions;
import com.boschsemanticstack.rql.model.v1.RqlOrder;
import com.boschsemanticstack.rql.model.v1.RqlSlice;

import jakarta.validation.constraints.NotNull;

public class RqlOptionsImpl implements RqlOptions {

   private final RqlSlice slice;

   private final RqlOrder order;

   public RqlOptionsImpl( final RqlSlice slice, final RqlOrder order ) {
      this.slice = slice;
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
   public boolean isEmpty() {
      return null == slice && order.isEmpty();
   }

   @Override
   public <T> T accept( final RqlModelVisitor<? extends T> visitor ) {
      return visitor.visitOptions( this );
   }

   @Override
   public List<? extends RqlModelNode> getChildren() {
      final List<RqlModelNode> result = new ArrayList<>( 2 );
      if ( slice != null ) {
         result.add( slice );
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
      return new RqlOptionsImpl( null, null );
   }
}

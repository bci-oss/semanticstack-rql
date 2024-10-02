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

package com.boschsemanticstack.rql.model.v1;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.boschsemanticstack.rql.model.v1.impl.RqlFieldDirectionImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlFilterImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlOptionsImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlOrderImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlQueryModelImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlSelectImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlSliceImpl;

public class RqlBuilder {

   private RqlSelect select;
   private RqlFilter filter;
   private RqlSlice slice;
   private RqlOrder order = new RqlOrderImpl( Collections.emptyList() );
 
   public RqlBuilder select( final String... attributes ) {
      select = null == attributes
            ? null
            : new RqlSelectImpl( Arrays.asList( attributes )
      );
      return this;
   }

   public RqlBuilder filter( final RqlFilter filter ) {
      this.filter = filter;
      return this;
   }

   public RqlBuilder sort( final RqlFieldDirection... fieldOrders ) {
      order = new RqlOrderImpl( fieldOrders == null
            ? null
            : Arrays.asList( fieldOrders )
      );
      return this;
   }

   public RqlBuilder limit( final long offset, final long limit ) {
      slice = new RqlSliceImpl( offset, limit );
      return this;
   }

   public RqlQueryModel build() {
      return new RqlQueryModelImpl( select, filter, new RqlOptionsImpl( slice, order ) );
   }

   public static RqlFilter eq( final String attribute, final Object value ) {
      return new RqlFilterImpl( attribute, RqlFilter.Operator.EQ, value );
   }

   public static RqlFilter ne( final String attribute, final Object value ) {
      return new RqlFilterImpl( attribute, RqlFilter.Operator.NE, value );
   }

   public static RqlFilter gt( final String attribute, final Object value ) {
      return new RqlFilterImpl( attribute, RqlFilter.Operator.GT, value );
   }

   public static RqlFilter ge( final String attribute, final Object value ) {
      return new RqlFilterImpl( attribute, RqlFilter.Operator.GE, value );
   }

   public static RqlFilter lt( final String attribute, final Object value ) {
      return new RqlFilterImpl( attribute, RqlFilter.Operator.LT, value );
   }

   public static RqlFilter le( final String attribute, final Object value ) {
      return new RqlFilterImpl( attribute, RqlFilter.Operator.LE, value );
   }

   public static RqlFilter in( final String attribute, final List<?> values ) {
      return new RqlFilterImpl( attribute, RqlFilter.Operator.IN,
            values == null
                  ? Collections.emptyList()
                  : values
      );
   }

   public static RqlFilter in( final String attribute, final Object... values ) {
      if ( values != null && values.length == 1 && values[0] instanceof final List list ) {
         // double dispatch to catch object-casted lists
         return new RqlFilterImpl( attribute, RqlFilter.Operator.IN, list );
      }

      return new RqlFilterImpl( attribute, RqlFilter.Operator.IN,
            values == null || values.length == 0
                  ? Collections.emptyList()
                  : Arrays.asList( values )
      );
   }

   public static RqlFilter like( final String attribute, final String value ) {
      return new RqlFilterImpl( attribute, RqlFilter.Operator.LIKE, value );
   }

   public static RqlFilter likeIgnoreCase( final String attribute, final String value ) {
      return new RqlFilterImpl( attribute, RqlFilter.Operator.LIKE_IGNORE_CASE, value );
   }

   public static RqlFilter not( final RqlFilter expression ) {
      return new RqlFilterImpl( RqlFilter.FilterType.NOT, expression );
   }

   public static RqlFilter and( final RqlFilter... expressions ) {
      return new RqlFilterImpl( RqlFilter.FilterType.AND, expressions );
   }

   public static RqlFilter or( final RqlFilter... expressions ) {
      return new RqlFilterImpl( RqlFilter.FilterType.OR, expressions );
   }

   public static RqlFieldDirection asc( final String attributeName ) {
      return new RqlFieldDirectionImpl( attributeName, RqlFieldDirection.Direction.ASCENDING );
   }

   public static RqlFieldDirection desc( final String attributeName ) {
      return new RqlFieldDirectionImpl( attributeName, RqlFieldDirection.Direction.DESCENDING );
   }
}

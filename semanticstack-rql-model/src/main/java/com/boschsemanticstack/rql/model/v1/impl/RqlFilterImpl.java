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
package com.boschsemanticstack.rql.model.v1.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.model.v1.RqlModelVisitor;

/**
 * Parsing RQL expressions results in a RqlFilter object or a tree of RqlFilter
 * objects. Every RqlFilter represents a condition of the RQL expression.
 */
public class RqlFilterImpl implements RqlFilter {

   private final List<RqlFilter> subFilters;

   private final RqlFilter.FilterType filterType;
   private final String attribute;
   private final Operator operator;
   private final List<Object> values = new ArrayList<>();

   public RqlFilterImpl( final String name, final Operator comparisonType, final Object value ) {
      filterType = FilterType.VALUE;
      attribute = name;
      operator = comparisonType;
      if ( value instanceof List ) {
         values.addAll( (Collection<?>) value );
      } else {
         values.add( value );
      }
      subFilters = Collections.emptyList();
   }

   @SuppressWarnings( "unchecked" )
   public RqlFilterImpl( final String name, final Operator comparisonType, final List<?> values ) {
      this( name, comparisonType, (Object) values );
   }

   public RqlFilterImpl( final FilterType filterType, final RqlFilter... subFilters ) {
      this.filterType = filterType;
      attribute = null;
      operator = null;

      this.subFilters = null == subFilters
            ? Collections.emptyList()
            : List.of( subFilters ); // defensive copy
   }

   public RqlFilterImpl( final FilterType filterType, final List<RqlFilter> subFilters ) {
      this.filterType = filterType;
      attribute = null;
      operator = null;

      this.subFilters = null == subFilters
            ? Collections.emptyList()
            : List.copyOf( subFilters ); // defensive copy
   }

   @Override
   public FilterType getFilterType() {
      return filterType;
   }

   @Override
   public String getAttribute() {
      return attribute;
   }

   @Override
   public List<RqlFilter> getChildren() {
      return subFilters;
   }

   @Override
   public Operator getOperator() {
      return operator;
   }

   @Override
   public Object getValue() {
      if ( values.size() > 1 ) {
         return values;
      }
      if ( !values.isEmpty() ) {
         return values.get( 0 );
      }
      return null;
   }

   @Override
   public List<Object> getValues() {
      return Collections.unmodifiableList( values );
   }

   @Override
   public <T> T accept( final RqlModelVisitor<? extends T> visitor ) {
      return visitor.visitFilter( this );
   }

   @Override
   public int getChildCount() {
      return subFilters.size();
   }
}

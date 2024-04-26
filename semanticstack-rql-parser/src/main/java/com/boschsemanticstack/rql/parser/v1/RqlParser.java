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

package com.boschsemanticstack.rql.parser.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.boschsemanticstack.rql.model.v1.RqlBuilder;
import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.model.v1.RqlSlice;
import com.boschsemanticstack.rql.model.v1.impl.RqlFilterImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlOptionsImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlQueryModelImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlSliceImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlToStringWriter;

public class RqlParser {

   private RqlParser() {
      // utility class
   }

   public static RqlQueryModel from( final String rqlQuery ) {
      return new RqlParserApi().parseFullQuery( rqlQuery );
   }

   public static RqlBuilder builder() {
      return new RqlBuilder();
   }

   public static String toString( final RqlQueryModel model ) {
      return new RqlToStringWriter().visitModel( model );
   }

   public static Map<String, String> toQueryParameters( final RqlQueryModel model ) {
      final RqlToStringWriter writer = new RqlToStringWriter();
      final Map<String, String> result = new HashMap<>();
      if ( !model.getSelect().isEmpty() ) {
         result.put( "select", writer.visitSelect( model.getSelect() ) );
      }
      model.getFilter()
            .map( writer::visitFilter )
            .ifPresent( filter -> result.put( "filter", filter ) );

      if ( !model.getOptions().isEmpty() ) {
         result.put( "option", writer.visitOptions( model.getOptions() ) );
      }
      return result;
   }

   public static RqlQueryModel addRestriction( final RqlQueryModel model, final RqlFilter... filters ) {
      if ( filters == null || filters.length == 0 ) {
         return model;
      }
      final RqlFilter newFilter = model.getFilter()
            .map( originalFilter -> addOrWrap( originalFilter, filters ) )
            .orElse( RqlBuilder.and( filters ) );

      return new RqlQueryModelImpl(
            model.getSelect(),
            newFilter,
            model.getOptions()
      );
   }

   private static RqlFilter addOrWrap( final RqlFilter currentFilter, final RqlFilter... additionalFilters ) {
      if ( currentFilter.getFilterType() == RqlFilter.FilterType.AND ) {
         final List<RqlFilter> combinedList = new ArrayList<>( currentFilter.getChildren().size() + additionalFilters.length );
         combinedList.addAll( currentFilter.getChildren() );
         combinedList.addAll( Arrays.asList( additionalFilters ) );
         return new RqlFilterImpl( RqlFilter.FilterType.AND, combinedList );
      }
      final List<RqlFilter> combinedList = new ArrayList<>( 1 + additionalFilters.length );
      combinedList.add( currentFilter );
      combinedList.addAll( Arrays.asList( additionalFilters ) );
      return new RqlFilterImpl( RqlFilter.FilterType.AND, combinedList );
   }

   public static Iterator<RqlQueryModel> getPagedQuery( final RqlQueryModel model, final long pageSize ) {
      return new PagingIterator( model, pageSize );
   }

   private static class PagingIterator implements Iterator<RqlQueryModel> {

      private final RqlQueryModel originalModel;
      private final boolean unbounded;
      private final Long maxIndex;
      private final long pageSize;
      private RqlSliceImpl nextSlice;

      public PagingIterator( final RqlQueryModel originalModel, final long pageSize ) {
         this.originalModel = originalModel;
         this.pageSize = pageSize;
         unbounded = originalModel.getOptions().getSlice().isEmpty();
         maxIndex = originalModel.getOptions().getSlice().map( originalSlice -> originalSlice.offset() + originalSlice.limit() )
               .orElse( -1L );
         nextSlice = new RqlSliceImpl(
               originalModel.getOptions().getSlice().map( RqlSlice::offset ).orElse( 0L ),
               Math.min( originalModel.getOptions().getSlice().map( RqlSlice::limit ).orElse( pageSize ), pageSize )
         );
      }

      @Override
      public boolean hasNext() {
         return nextSlice != null;
      }

      @Override
      public RqlQueryModel next() {
         if ( nextSlice == null ) {
            throw new NoSuchElementException( "No more elements!" );
         }
         final RqlQueryModel result = new RqlQueryModelImpl(
               originalModel.getSelect(),
               originalModel.getFilter().orElse( null ),
               new RqlOptionsImpl( nextSlice, originalModel.getOptions().getOrder() )
         );
         computeNextSlice();
         return result;
      }

      private void computeNextSlice() {
         final long nextOffset = nextSlice.offset() + pageSize;
         final long nextPageSize = unbounded
               ? pageSize
               : Math.min( maxIndex - nextOffset, pageSize );

         nextSlice = nextPageSize > 0
               ? new RqlSliceImpl( nextOffset, nextPageSize )
               : null;
      }
   }

   protected static Stream<RqlQueryModel> getPagedQueryStream( final RqlQueryModel model, final long pageSize ) {
      return StreamSupport.stream( Spliterators.spliteratorUnknownSize( getPagedQuery( model, pageSize ), Spliterator.ORDERED ), false );
   }
}

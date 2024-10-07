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

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.stream.Collectors;

import com.boschsemanticstack.rql.model.v1.RqlFieldDirection;
import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.model.v1.RqlModelVisitor;
import com.boschsemanticstack.rql.model.v1.RqlOptions;
import com.boschsemanticstack.rql.model.v1.RqlOrder;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.model.v1.RqlSelect;
import com.boschsemanticstack.rql.model.v1.RqlSlice;

public abstract class AbstractRqlWriter extends RqlModelVisitor<String> {
   @Override
   public abstract String visitModel( RqlQueryModel model );

   @Override
   public String visitSelect( final RqlSelect selectModel ) {
      return String.join( ",", selectModel.attributes() );
   }

   @Override
   public String visitFilter( final RqlFilter model ) {
      return switch ( model.getFilterType() ) {
         case VALUE -> visitComparison( model );
         case OR, AND, NOT -> visitLogicOperation( model );
      };
   }

   @Override
   public String visitLogicOperation( final RqlFilter filter ) {
      return switch ( filter.getFilterType() ) {
         case OR -> filter.getChildren().stream()
               .map( this::visitFilter )
               .collect( Collectors.joining( ",", "or(", ")" ) );
         case AND -> filter.getChildren().stream()
               .map( this::visitFilter )
               .collect( Collectors.joining( ",", "and(", ")" ) );
         case NOT -> filter.getChildren().stream()
               .map( this::visitFilter )
               .collect( Collectors.joining( ",", "not(", ")" ) );
         default -> throw new IllegalArgumentException( filter.getFilterType() + "-filter is not a "
               + "valid argument for visitLogicOperation!" );
      };
   }
 
   @Override
   public String visitComparison( final RqlFilter filter ) {
      return filter.getOperator().getName() + "(" + filter.getAttribute() + "," + valuesToString( filter ) + ")";
   }

   private String valuesToString( final RqlFilter filter ) {
      return filter.getValues().stream()
            .map( this::getValueAsString )
            .collect( Collectors.joining( "," ) );
   }

   private String getValueAsString( final Object value ) {
      if ( value == null ) {
         return "null";
      }
      if ( value instanceof Boolean ) {
         return value.toString();
      }
      if ( value instanceof Number ) {
         return value.toString();
      }
      if ( value instanceof OffsetDateTime ) {
         return value.toString();
      }
      return "\"" + value + "\"";
   }

   @Override
   public String visitOptions( final RqlOptions model ) {
      return model.isEmpty()
            ? null
            : model.getChildren().stream()
            .map( node -> node.accept( this ) )
            .filter( Objects::nonNull )
            .collect( Collectors.joining( "," ) );
   }

   @Override
   public String visitSlice( final RqlSlice model ) {
      return model == null
            ? null
            : String.format( "limit(%d,%d)", model.offset(), model.limit() );
   }

   @Override
   public String visitOrder( final RqlOrder model ) {
      return model.fieldDirections().stream()
            .map( this::visitFieldDirection )
            .collect( Collectors.joining( ",", "sort(", ")" ) );
   }

   @Override
   public String visitFieldDirection( final RqlFieldDirection model ) {
      return ( model.direction() == RqlFieldDirection.Direction.ASCENDING ? "+" : "-" ) + model.attribute();
   }
}

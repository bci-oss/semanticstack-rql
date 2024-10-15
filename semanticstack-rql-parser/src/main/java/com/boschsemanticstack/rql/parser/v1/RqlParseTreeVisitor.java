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

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.boschsemanticstack.rql.exceptions.ParseException;
import com.boschsemanticstack.rql.exceptions.SourceLocation;
import com.boschsemanticstack.rql.model.v1.RqlCursor;
import com.boschsemanticstack.rql.model.v1.RqlFieldDirection;
import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.model.v1.RqlModelNode;
import com.boschsemanticstack.rql.model.v1.RqlOptions;
import com.boschsemanticstack.rql.model.v1.RqlOrder;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.model.v1.RqlSelect;
import com.boschsemanticstack.rql.model.v1.RqlSlice;
import com.boschsemanticstack.rql.model.v1.impl.RqlCursorImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlFieldDirectionImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlFilterImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlOptionsImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlOrderImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlQueryModelImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlSelectImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlSliceImpl;
import com.boschsemanticstack.rql.parser.v1.internal.InternalRqlBaseVisitor;
import com.boschsemanticstack.rql.parser.v1.internal.InternalRqlParser;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

class RqlParseTreeVisitor extends InternalRqlBaseVisitor<Object> {

   @Override
   public RqlQueryModel visitQuery( final InternalRqlParser.QueryContext ctx ) {

      final ModelCombiner combiner = new ModelCombiner();

      ctx.children.stream()
            .map( this::visit )
            .map( RqlModelNode.class::cast )
            .forEachOrdered( combiner::add );

      return combiner.build();
   }

   private static class ModelCombiner {
      RqlSelect select;
      RqlFilter filter;
      RqlOptions options;

      private void add( final RqlModelNode node ) {
         if ( node instanceof final RqlSelect rqlSelect ) {
            trySetSelect( rqlSelect );
            return;
         }
         if ( node instanceof final RqlFilter rqlFilter ) {
            trySetFilter( rqlFilter );
            return;
         }
         if ( node instanceof final RqlOptions rqlOptions ) {
            trySetOptions( rqlOptions );
            return;
         }
         if ( node != null ) {
            throw new IllegalArgumentException( "RqlModelNode " + node.getClass() + " not supported!" );
         }
      }

      private void trySetOptions( final RqlOptions node ) {
         if ( options == null ) {
            options = node;
            return;
         }
         throw new ParseException( "No more than one options statement allowed" );
      }

      private void trySetSelect( final RqlSelect node ) {
         if ( select == null ) {
            select = node;
            return;
         }
         throw new ParseException( "No more than one select statement allowed" );
      }

      private void trySetFilter( final RqlFilter node ) {
         if ( filter == null ) {
            filter = node;
            return;
         }
         throw new ParseException( "No more than one filter statement allowed" );
      }

      public RqlQueryModel build() {
         return new RqlQueryModelImpl( select, filter, options );
      }
   }

   @Override
   public RqlModelNode visitQueryParameter( final InternalRqlParser.QueryParameterContext ctx ) {
      return ctx.children.stream()
            .map( this::visit )
            .map( RqlModelNode.class::cast )
            .findFirst()
            .orElse( null );
   }

   @Override
   public Object visitSelectDeclaration( final InternalRqlParser.SelectDeclarationContext ctx ) {
      return null == ctx.fieldList() ? null : new RqlSelectImpl( visitFieldList( ctx.fieldList() ) );
   }

   @Override
   public Object visitFilterDeclaration( final InternalRqlParser.FilterDeclarationContext ctx ) {
      return visitFilterExpression( ctx.filterExpression() );
   }

   @Override
   public List<String> visitFieldList( final InternalRqlParser.FieldListContext ctx ) {
      if ( ctx.FieldIdentifier() != null ) {
         return ctx.FieldIdentifier().stream()
               .map( terminalNode -> terminalNode.getSymbol().getText() )
               .toList();
      }
      return Collections.emptyList();
   }

   @Override
   public RqlOptions visitOptionList( final InternalRqlParser.OptionListContext ctx ) {

      final InternalRqlParser.OptionExpressionContext optionExpression = ctx.optionExpression();
      if ( optionExpression == null ) {
         return RqlOptionsImpl.emptyOptions();
      }

      visitInvalidExpression( optionExpression.invalidExpression() );

      final RqlOrder order = visitSortExpression( optionExpression.sortExpression() );
      if ( optionExpression.limitOrCursorExpression() == null ) {
         return new RqlOptionsImpl( null, order, null );
      }
      final RqlSlice slice = visitLimitExpression( optionExpression.limitOrCursorExpression().limitExpression() );
      final RqlCursor cursor = visitCursorExpression( optionExpression.limitOrCursorExpression().cursorExpression() );

      return new RqlOptionsImpl( slice, order, cursor );
   }

   @Override
   public RqlOrder visitSortExpression( final InternalRqlParser.SortExpressionContext ctx ) {
      return null == ctx || null == ctx.sortFieldIdentifier()
            ? new RqlOrderImpl( null )
            : new RqlOrderImpl( ctx.sortFieldIdentifier().stream()
            .map( this::visitSortFieldIdentifier )
            .toList() );
   }

   @Override
   public Object visitInvalidExpression( final InternalRqlParser.InvalidExpressionContext ctx ) {
      if ( ctx == null ) {
         return null;
      }
      throw new ParseException( "Cursor and Limit cannot be used together" );
   }

   @Override
   public RqlFieldDirection visitSortFieldIdentifier( final InternalRqlParser.SortFieldIdentifierContext ctx ) {
      final RqlFieldDirection.Direction direction = "+".equals( ctx.getChild( 0 ).getText() )
            ? RqlFieldDirection.Direction.ASCENDING
            : RqlFieldDirection.Direction.DESCENDING;
      return new RqlFieldDirectionImpl( unescapeFieldIdentifier( ctx.FieldIdentifier() ), direction );
   }

   @Override
   public RqlSlice visitLimitExpression( final InternalRqlParser.LimitExpressionContext ctx ) {
      if ( ctx == null ) {
         return null;
      }
      return new RqlSliceImpl( Long.parseLong( ctx.IntLiteral( 0 ).getText() ), Long.parseLong( ctx.IntLiteral( 1 ).getText() ) );
   }

   @Override
   public RqlCursor visitCursorExpression( final InternalRqlParser.CursorExpressionContext ctx ) {
      if ( ctx == null ) {
         return null;
      }
      if ( ctx.StringLiteral() == null ) {
         return new RqlCursorImpl( Long.parseLong( ctx.IntLiteral().getText() ) );
      }
      return new RqlCursorImpl( Optional.ofNullable( unescapeStringLiteral( ctx.StringLiteral() ) ),
            Long.parseLong( ctx.IntLiteral().getText() ) );
   }

   @Override
   public RqlFilter visitFilterExpression( final InternalRqlParser.FilterExpressionContext ctx ) {
      return (RqlFilter) super.visitFilterExpression( ctx );
   }

   @Override
   public List<RqlFilter> visitFilterList( final InternalRqlParser.FilterListContext ctx ) {
      return ctx.filterExpression().stream()
            .map( this::visitFilterExpression )
            .toList();
   }

   @Override
   public RqlFilter visitLogicalOperator( final InternalRqlParser.LogicalOperatorContext ctx ) {
      final RqlFilterImpl filter;

      final ParseTree token = ctx.children.get( 0 );
      filter = switch ( token.getText() ) {
         case "not" -> new RqlFilterImpl( RqlFilter.FilterType.NOT, visitFilterExpression( ctx.filterExpression() ) );
         case "and" -> new RqlFilterImpl( RqlFilter.FilterType.AND, visitFilterList( ctx.filterList() ) );
         case "or" -> new RqlFilterImpl( RqlFilter.FilterType.OR, visitFilterList( ctx.filterList() ) );
         default -> throw createOperationUnknownParseException( ctx );
      };
      return filter;
   }

   @Override
   public RqlFilter visitMultiComparison( final InternalRqlParser.MultiComparisonContext ctx ) {
      final String fieldIdentifier = unescapeFieldIdentifier( ctx.FieldIdentifier() );
      final List<Object> comparisonList = visitLiteralList( ctx.literalList() );
      return switch ( ctx.getChild( 0 ).getText() ) {
         case "in" -> new RqlFilterImpl( fieldIdentifier, RqlFilter.Operator.IN, comparisonList );
         case "out" -> throw createOperationUnknownParseException( ctx );
         default -> throw new ParseException( "Syntax error parsing!", getSourceLocation( ctx ) );
      };
   }

   @Override
   public RqlFilter visitEqualityComparison( final InternalRqlParser.EqualityComparisonContext ctx ) {
      final String fieldIdentifier = unescapeFieldIdentifier( ctx.FieldIdentifier() );
      final Object literal = visitLiteral( ctx.literal() );

      return switch ( ctx.getChild( 0 ).getText() ) {
         case "eq" -> new RqlFilterImpl( fieldIdentifier, RqlFilter.Operator.EQ, literal );
         case "ne" -> new RqlFilterImpl( fieldIdentifier, RqlFilter.Operator.NE, literal );
         default -> throw createOperationUnknownParseException( ctx );
      };
   }

   @Override
   public RqlFilter visitOrderRelation( final InternalRqlParser.OrderRelationContext ctx ) {
      final String fieldIdentifier = unescapeFieldIdentifier( ctx.FieldIdentifier() );
      final Object literal = visitLinearilyOrderableLiteral( ctx.linearilyOrderableLiteral() );

      return switch ( ctx.getChild( 0 ).getText() ) {
         case "lt" -> new RqlFilterImpl( fieldIdentifier, RqlFilter.Operator.LT, literal );
         case "le" -> new RqlFilterImpl( fieldIdentifier, RqlFilter.Operator.LE, literal );
         case "gt" -> new RqlFilterImpl( fieldIdentifier, RqlFilter.Operator.GT, literal );
         case "ge" -> new RqlFilterImpl( fieldIdentifier, RqlFilter.Operator.GE, literal );
         default -> throw new ParseException( "Operation unknown: '" + ctx.getChild( 0 ).getText() + "'.", getSourceLocation( ctx ) );
      };
   }

   @Override
   public RqlFilter visitStringMatch( final InternalRqlParser.StringMatchContext ctx ) {
      final String fieldIdentifier = unescapeFieldIdentifier( ctx.FieldIdentifier() );
      final Object literal = unescapeStringLiteral( ctx.StringLiteral() );

      return switch ( ctx.getChild( 0 ).getText() ) {
         case "like" -> new RqlFilterImpl( fieldIdentifier, RqlFilter.Operator.LIKE, literal );
         case "likeIgnoreCase" -> new RqlFilterImpl( fieldIdentifier, RqlFilter.Operator.LIKE_IGNORE_CASE, literal );
         default -> throw createOperationUnknownParseException( ctx );
      };
   }

   @Override
   public List<Object> visitLiteralList( final InternalRqlParser.LiteralListContext ctx ) {
      if ( ctx.floatLiteralList() != null ) {
         return visitFloatLiteralList( ctx.floatLiteralList() );
      }
      if ( ctx.stringLiteralList() != null ) {
         return visitStringLiteralList( ctx.stringLiteralList() );
      }
      return visitIntLiteralList( ctx.intLiteralList() );
   }

   @Override
   public List<Object> visitFloatLiteralList( final InternalRqlParser.FloatLiteralListContext ctx ) {
      return ctx.FloatLiteral().stream()
            .map( literal -> Double.valueOf( literal.getText() ) )
            .collect( Collectors.toList() );
   }

   @Override
   public List<Object> visitIntLiteralList( final InternalRqlParser.IntLiteralListContext ctx ) {
      return ctx.IntLiteral().stream()
            .map( literal -> Integer.valueOf( literal.getText() ) )
            .collect( Collectors.toList() );
   }

   @Override
   public List<Object> visitStringLiteralList( final InternalRqlParser.StringLiteralListContext ctx ) {
      return ctx.StringLiteral().stream()
            .map( this::unescapeStringLiteral )
            .collect( Collectors.toList() );
   }

   @Override
   public Object visitLiteral( final InternalRqlParser.LiteralContext ctx ) {
      return switch ( ctx.getChild( 0 ).getText() ) {
         case "null" -> null;
         case "true" -> Boolean.TRUE;
         case "false" -> Boolean.FALSE;
         default -> visitLinearilyOrderableLiteral( ctx.linearilyOrderableLiteral() );
      };
   }

   @Override
   public Object visitLinearilyOrderableLiteral( final InternalRqlParser.LinearilyOrderableLiteralContext ctx ) {
      return switch ( ctx.getStart().getType() ) {
         case InternalRqlParser.StringLiteral -> unescapeStringLiteral( ctx.StringLiteral() );
         case InternalRqlParser.FloatLiteral -> parseIntoSmallestFloatType( ctx );
         case InternalRqlParser.IntLiteral -> parseIntoSmallestIntegerType( ctx );
         case InternalRqlParser.TimeLiteral -> parseOffsetDateTime( ctx );
         default -> throw new ParseException( "TokenType unknown: '" + ctx.getChild( 0 ).getText() + "'.", getSourceLocation( ctx ) );
      };
   }

   private OffsetDateTime parseOffsetDateTime( final InternalRqlParser.LinearilyOrderableLiteralContext ctx ) {
      try {
         return OffsetDateTime.parse( ctx.getText().replace( ",", "." ) );
      } catch ( final DateTimeParseException e ) {
         throw new ParseException( e.getMessage(),
               new SourceLocation( ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine() + 1 ), e );
      }
   }

   private Number parseIntoSmallestIntegerType( final InternalRqlParser.LinearilyOrderableLiteralContext ctx ) {
      final String integerString = ctx.IntLiteral().getText();
      try {
         return Integer.parseInt( integerString );
      } catch ( final NumberFormatException e ) {
         try {
            return Long.parseLong( integerString );
         } catch ( final NumberFormatException e2 ) {
            try {
               return new BigInteger( integerString );
            } catch ( final NumberFormatException e3 ) {
               throw new NumberFormatException( "Can not parse '" + integerString + "' as Integer, Long or BigInteger!" );
            }
         }
      }
   }

   private Number parseIntoSmallestFloatType( final InternalRqlParser.LinearilyOrderableLiteralContext ctx ) {
      final String floatString = ctx.FloatLiteral().getText();
      try {
         return new BigDecimal( floatString );
      } catch ( final NumberFormatException e3 ) {
         throw new NumberFormatException( "Can not parse '" + floatString + "' as Float, Double or BigDecimal!" );
      }
   }

   /**
    * {@inheritDoc}
    *
    * <p>Does use the default if node is null</p>
    */
   @Override
   public Object visitChildren( final RuleNode node ) {
      return ( node == null )
            ? defaultResult()
            : super.visitChildren( node );
   }

   private SourceLocation getSourceLocation( final ParserRuleContext ctx ) {
      return new SourceLocation( ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine() + 1 );
   }

   private SourceLocation getSourceLocation( final TerminalNode token ) {
      return new SourceLocation( token.getSymbol().getLine(), token.getSymbol().getCharPositionInLine() + 1 );
   }

   private ParseException createOperationUnknownParseException( final ParserRuleContext ctx ) {
      return new ParseException( "Operation unknown: '" + ctx.getText() + "'.", getSourceLocation( ctx ) );
   }

   private String unescapeStringLiteral( final TerminalNode stringLiteral ) {
      final StreamTokenizer parser = new StreamTokenizer( new StringReader( stringLiteral.getSymbol().getText() ) );
      try {
         parser.nextToken();
      } catch ( final IOException e ) {
         throw new ParseException( "Cannot untokenize string", getSourceLocation( stringLiteral ), e );
      }
      return parser.sval;
   }

   private String unescapeFieldIdentifier( final TerminalNode fieldIdentifier ) {
      return fieldIdentifier.getSymbol().getText();
   }
}

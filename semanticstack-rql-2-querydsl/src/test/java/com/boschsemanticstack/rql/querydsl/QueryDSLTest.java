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

package com.boschsemanticstack.rql.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.boschsemanticstack.rql.exceptions.IllegalValueTypeQueryException;
import com.boschsemanticstack.rql.exceptions.NoSuchFieldQueryException;
import com.boschsemanticstack.rql.exceptions.ParseException;
import com.boschsemanticstack.rql.model.v1.RqlBuilder;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.model.v1.impl.RqlSliceImpl;
import com.boschsemanticstack.rql.parser.v1.RqlParser;
import com.boschsemanticstack.rql.querydsl.entities.QEntity;
import com.boschsemanticstack.rql.querydsl.entities.QWildcardEntity;
import com.querydsl.core.types.Predicate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class QueryDSLTest {

   @Test
   void filterWithoutFilterShouldNotCreatePredicate() {
      final String queryParams = "";
      final QueryModelToQueryDSL query = getQuery( queryParams );
      assertThat( query.getPredicate() ).isNotPresent();
   }
 
   /**
    * Generates Simple in => out tests
    */
   static Stream<Arguments> generateFilterToPredicateCases() {
      return Stream.of(
            arguments( "RqlFilterImpl with property equals string should return predicate with name equals", "eq(name, \"a\")",
                  "entity.name = a" ),
            arguments( "testFilter_withPropertyEqualsTrue_shouldReturnPredicateWithNameEquals", "eq(special, true)",
                  "entity.special = true" ),
            arguments( "testFilter_withProperty_shouldBeCaseInsensitive", "eq(NAME, \"a\")", "entity.name = a" ),
            arguments( "testFilter_withOr_shouldReturnPredicateWithOr", "or(eq(id,\"a\"),eq(id,\"b\"))", "entity.id = a || entity.id = b" ),
            arguments( "testFilter_withAnd_shouldReturnPredicateWithAnd", "and(eq(id,\"a\"),eq(name,\"b\"))",
                  "entity.id = a && entity.name = b" ),
            arguments( "testFilter_withNestedAnds_shouldReturnPredicateWithAnds", "and(eq(id,\"a\"),and(eq(name,\"b\"),eq(type,\"c\")))",
                  "entity.id = a && entity.name = b && entity.type = c" ),
            arguments( "testFilter_withOrInAnd_shouldReturnPredicateWithAndAndSeparateOr",
                  "and(eq(id,\"a\"),or(eq(name,\"b\"),eq(name,\"c\")))",
                  "entity.id = a && (entity.name = b || entity.name = c)" ),
            arguments( "testFilter_withPropertyNull_shouldReturnIsNullPredicate", "eq(name,null)", "entity.name is null" ),
            arguments( "testFilter_withPropertyNotNull_shouldReturnIsNotNullPredicate", "ne(name,null)", "entity.name is not null" ),
            arguments( "testFilter_withSubResourceMapPropertyDotSeparated_shouldReturnPredicateForEntityWithSuchASubEntity",
                  "ne(subEntity.metadata.key1,\"value1\")", "entity.subEntity.metadata.get(key1) != value1" ),
            arguments( "testFilter_withSubResourceMapPropertySlashSeparated_shouldReturnPredicateForEntityWithSuchASubEntity",
                  "ne(subEntity.metadata.key1,\"value1\")", "entity.subEntity.metadata.get(key1) != value1" ),
            arguments( "testFilter_withSubResourcePropertyEqualsString_shouldReturnPredicateForEntityWithSuchASubEntity",
                  "ne(subEntity.name,\"value1\")",
                  "entity.subEntity.name != value1" ),
            arguments( "testFilter_withPropertyInString_shouldReturnInPredicateWithTwoValues", "in(name,\"a\",\"b\")",
                  "entity.name in [a, b]" ),
            arguments( "testFilter_withSubResourcePropertyInString_shouldReturnInPredicateWithTwoValues", "in(subEntity.name,\"a\",\"b\")",
                  "entity.subEntity.name in [a, b]" ),
            arguments( "testFilter_withPropertyNotInString_shouldReturnPredicateWithNotInWithTwoValues", "not(in(name,\"a\",\"b\"))",
                  "!(entity.name in [a, b])" ),
            arguments( "testFilter_withPropertyInStringSingleValue_shouldReturnPredicateForSingleValueEquals", "in(name,\"a\")",
                  "entity.name = a" ),
            arguments( "testFilter_withLikeWithWildcard_shouldReturnLikeWithWildcards", "like(name,\"compl*Patt?rn\")",
                  "entity.name like compl%Patt_rn" ),
            arguments( "testFilter_withLikeIgnoreCaseWithWildcard_shouldReturnLikeIgnoreCaseWithWildcards",
                  "likeIgnoreCase(name,\"compl*Patt?rn\")",
                  "lower(entity.name) like compl%patt_rn" )
      );
   }

   @DisplayName( "Testing filter to predicate conversion: " )
   @ParameterizedTest( name = "{index}:{0} ==> ''{1}''" )
   @MethodSource( { "generateFilterToPredicateCases" } )
   void predicateByValue( final String testName, final String filterValue, final String predicateToStringExpectation ) {
      final QueryModelToQueryDSL query = getQuery( "filter=" + filterValue );
      final Predicate predicate = query.getPredicate().orElseThrow( () -> new IllegalStateException( "Predicate Expected" ) );
      assertThat( predicate )
            .describedAs( "$s expected '%s' to convert to '%s'", testName, filterValue, predicateToStringExpectation )
            .hasToString( predicateToStringExpectation );
   }

   @Test
   void filterWithInvalidOperatorOnNullShouldNotBeValid() {
      final String queryParams = "filter=lt(name,null)";
      assertThatThrownBy( () -> getQuery( queryParams ) )
            .isInstanceOf( ParseException.class )
            .hasMessageStartingWith( "mismatched input 'null' expecting {" )
            .hasMessageContaining( "FloatLiteral" )
            .hasMessageContaining( "IntLiteral" )
            .hasMessageContaining( "StringLiteral" );
   }

   @Test
   void filterWithStringComparedToIntegerShouldFail() {
      final String queryParams = "filter=eq(name,42)";
      assertThatThrownBy( () -> getQuery( queryParams ) )
            .isInstanceOf( IllegalValueTypeQueryException.class )
            .hasMessage( "Invalid value type Integer for property entity.name" );
   }

   @Test
   void filterWithPropertyInLargeSetOracleORA01795ShouldReturnPartitionedInPredicate() {
      final String queryParams = "filter=in(name,"
            + IntStream.range( 0, 1000 ).boxed().map( i -> "\"a\"" ).collect( Collectors.joining( "," ) )
            + "," + IntStream.range( 0, 10 ).boxed().map( i -> "\"b\"" ).collect( Collectors.joining( "," ) )
            + ")";
      final QueryModelToQueryDSL query = getQuery( queryParams );
      final Predicate predicate = query.getPredicate().orElseThrow( () -> new IllegalStateException( "Predicate Expected" ) );
      assertThat( predicate ).hasToString(
            "entity.name in [a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, "
                  + "a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a,"
                  + " a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, "
                  + "a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a,"
                  + " a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, "
                  + "a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a,"
                  + " a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, "
                  + "a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a,"
                  + " a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, "
                  + "a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a,"
                  + " a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, "
                  + "a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a,"
                  + " a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, "
                  + "a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a,"
                  + " a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, "
                  + "a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a,"
                  + " a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, "
                  + "a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a,"
                  + " a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, "
                  + "a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a,"
                  + " a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, "
                  + "a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a,"
                  + " a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, "
                  + "a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a,"
                  + " a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, "
                  + "a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a,"
                  + " a] "
                  + "|| entity.name in [b, b, b, b, b, b, b, b, b, b]" );
   }

   @Test
   void filterWithNotExistingPropertyEqualsStringAndSortingAscAndDescShouldReturnError() {
      final String queryParams = "filter=eq(name, \"a\")&option=sort(+a,-b)";
      assertThatThrownBy( () -> getQuery( queryParams ) ).isInstanceOf( NoSuchFieldQueryException.class )
            .hasMessage( "Field 'a' does not exist." );
   }

   @Test
   void filterWithPropertyEqualsStringAndSortingAscAndDescShouldReturnLikeWithWildcardsAndSortingAscAndDesc() {
      final String queryParams = "filter=eq(name, \"a\")&option=sort(+name,-type)";
      final QueryModelToQueryDSL query = getQuery( queryParams );
      final Predicate predicate = query.getPredicate().orElseThrow( () -> new IllegalStateException( "Predicate Expected" ) );
      assertThat( predicate ).hasToString( "entity.name = a" );
      assertThat( query.getOrdering().get( 0 ) ).hasToString( "entity.name ASC" );
      assertThat( query.getOrdering().get( 1 ) ).hasToString( "entity.type DESC" );
   }

   @Test
   void filterWithChildPropertyEqualsStringAndSortingAscAndDescShouldReturnError() {
      final String queryParams = "filter=eq(subEntities.name, \"a\")&option=sort(+subEntities.name)";
      assertThatThrownBy( () -> getQuery( queryParams ) ).isInstanceOf( NoSuchFieldQueryException.class )
            .hasMessage( "Sorting by collection child entity is not supported." );
   }

   @Test
   void filterWithChildPropertyEqualsStringAndSortingAscAndDescShouldReturnOk() {
      final String queryParams = "filter=eq(name, \"a\")&option=sort(+subEntity.name)";
      final QueryModelToQueryDSL query = getQuery( queryParams );
      final Predicate predicate = query.getPredicate().orElseThrow( () -> new IllegalStateException( "Predicate Expected" ) );
      assertThat( predicate ).hasToString( "entity.name = a" );
      assertThat( query.getOrdering().get( 0 ) ).hasToString( "entity.subEntity.name ASC" );
   }

   @Test
   void filterWithPropertyEqualsStringAndPaginationOffsetShouldReturnPredicateWithNameEqualsAndPaginationStartAndStop() {
      final String queryParams = "filter=eq(name, \"a\")&option=limit(1,2)";
      final QueryModelToQueryDSL query = getQuery( queryParams );
      final Predicate predicate = query.getPredicate().orElseThrow( () -> new IllegalStateException( "Predicate Expected" ) );
      assertThat( predicate ).hasToString( "entity.name = a" );
      Assertions.assertThat( query.getPagination() ).isPresent().hasValueSatisfying(
            v -> Assertions.assertThat( v ).usingRecursiveComparison().isEqualTo( new RqlSliceImpl( 1, 2 ) )
      );
   }

   @Test
   void filterWithPropertyEqualsStringAndSortingAscAndPaginationOffsetShouldReturnPredicateWithNameEqualsAndSortingAscAndDescAndPaginationStartAndStop() {
      final String queryParams = "filter=eq(name, \"a\")&option=sort(+name,-type),limit(1,2)";
      final QueryModelToQueryDSL query = getQuery( queryParams );
      final Predicate predicate = query.getPredicate().orElseThrow( () -> new IllegalStateException( "Predicate Expected" ) );
      assertThat( predicate ).hasToString( "entity.name = a" );

      Assertions.assertThat( query.getPagination() )
            .isPresent()
            .hasValueSatisfying(
                  v -> Assertions.assertThat( v ).usingRecursiveComparison().isEqualTo( new RqlSliceImpl( 1, 2 ) )
            );

      assertThat( query.getOrdering().get( 0 ) ).hasToString( "entity.name ASC" );
      assertThat( query.getOrdering().get( 1 ) ).hasToString( "entity.type DESC" );
   }

   /**
    * Generates testcases where the field is unknown
    */
   static Stream<Arguments> generateFieldNotExistingCases() {
      return Stream.of(
            arguments( "testFilter_withUnknownEntityField_shouldFail", "eq(doesnotexist,\"sowhat\")", "doesnotexist" ),
            arguments( "testFilter_withUnknownSubEntityField_shouldFail", "eq(subentity.doesnotexist,\"sowhat\")", "doesnotexist" ),
            arguments( "testFilter_withUnknownCollectionEntryField_shouldFail", "eq(subentities.doesnotexist,\"sowhat\")", "doesnotexist" )
      );
   }

   @DisplayName( "Testing filter to predicate conversion: " )
   @ParameterizedTest( name = "{index}:{0} ==> ''{1}''" )
   @MethodSource( { "generateFieldNotExistingCases" } )
   void fieldNotExistingByValue( final String testName, final String filterValue, final String expectedErrorField ) {
      assertThatThrownBy( () -> getQuery( "filter=" + filterValue ) )
            .describedAs( "$s expected '%s' to raise an exception that %s does not exist", testName, filterValue, expectedErrorField )
            .isInstanceOf( NoSuchFieldQueryException.class )
            .hasMessage( "Field '" + expectedErrorField + "' does not exist." );
   }

   @Test
   void genericFilterWithMissingConverterForSpecialTypeShouldFail() {
      assertThatThrownBy( this::getGenericQuery )
            .isInstanceOf( IllegalValueTypeQueryException.class )
            .hasMessage( "Invalid value type String for property entity.specialId" );
   }

   @Test
   void genericFilterWithConverterForSpecialTypeShouldReturnPredicateWithSpecialIdEquals() {
      final QueryModelToQueryDSL query = getGenericQueryWithConverter( UUID::fromString );
      final Predicate predicate = query.getPredicate().orElseThrow( () -> new IllegalStateException( "Predicate Expected" ) );
      assertThat( predicate ).hasToString( "entity.specialId = 6a54409e-0323-4ec3-a628-e2b76dbc1b5d" );
   }

   @Test
   void genericFilterWithConverterForSpecialTypeShouldNotConvertAlreadyCorrectType() {
      final RqlQueryModel specialIdFilter = RqlParser.builder()
            .filter( RqlBuilder.eq( "specialId", UUID.fromString( "6a54409e-0323-4ec3-a628-e2b76dbc1b5d" ) ) )
            .build();

      final Function<Object, UUID> converter = spy( new SpyableUuidConverter() );

      getGenericQueryWithConverter( specialIdFilter, converter );

      verify( converter, never() ).apply( any() ); // should not execute converter because object is of correct type
   }

   @Test
   void genericFilterWithConverterForSpecialTypeShouldConvertElementsOfInStatement() {
      final RqlQueryModel specialIdFilter = RqlParser.builder()
            .filter( RqlBuilder
                  .in( "specialId", "6a54409e-0323-4ec3-a628-e2b76dbc1b5d", "5da41f30-5f00-40a3-bdf0-67faf887707d",
                        "43a12c5a-b9a8-4e8f-b7d8-cb5a5ddf957d" ) )
            .build();

      final Function<Object, UUID> converter = spy( new SpyableUuidConverter() );

      getGenericQueryWithConverter( specialIdFilter, converter );

      verify( converter, times( 3 ) ).apply( any() ); // should not execute converter because object is of correct type
   }

   @Test
   void genericFilterWithConverterForSpecialTypeShouldNotConvertElementsOfInStatementIfAlreadyConverted() {
      final RqlQueryModel specialIdFilter = RqlParser.builder()
            .filter( RqlBuilder.in( "specialId",
                  UUID.fromString( "6a54409e-0323-4ec3-a628-e2b76dbc1b5d" ),
                  UUID.fromString( "5da41f30-5f00-40a3-bdf0-67faf887707d" ),
                  "43a12c5a-b9a8-4e8f-b7d8-cb5a5ddf957d" ) // intentionally NOT uuid
            )
            .build();

      final Function<Object, UUID> converter = spy( new SpyableUuidConverter() );

      getGenericQueryWithConverter( specialIdFilter, converter );

      verify( converter, times( 1 ) ).apply( "43a12c5a-b9a8-4e8f-b7d8-cb5a5ddf957d" );
      verifyNoMoreInteractions( converter );
   }

   //TODO gt, lt, lte, gte, edge cases

   private QueryModelToQueryDSL getQuery( final String queryParams ) {
      return QueryModelToQueryDSL.forJpa( QEntity.entity, RqlParser.from( queryParams ) );
   }

   private void getGenericQuery() {
      QueryModelToQueryDSL.forGenericStore( QEntity.entity, RqlParser.from(
            "filter=eq(specialId, \"6a54409e-0323-4ec3-a628-e2b76dbc1b5d\")" ) );
   }

   private <F, T> QueryModelToQueryDSL getGenericQueryWithConverter( final Function<F, T> converter ) {
      return RqlToQueryDslConverterBuilder.forGenericStore( QEntity.entity ).withTypeConverter( (Class<T>) UUID.class, converter )
            .build().applyTo( RqlParser.from( "filter=eq(specialId, \"6a54409e-0323-4ec3-a628-e2b76dbc1b5d\")" ) );
   }

   private <F, T> void getGenericQueryWithConverter( final RqlQueryModel model,
         final Function<F, T> converter ) {
      RqlToQueryDslConverterBuilder.forGenericStore( QEntity.entity ).withTypeConverter( (Class<T>) UUID.class, converter )
            .build().applyTo( model );
   }

   @ParameterizedTest
   @MethodSource( { "generateLikeExpressionWithAllowedWildcardCount" } )
   void allowedLikeExpressionCount( final String query ) {
      final QueryModelToQueryDSL queryModelToQueryDS1 = QueryModelToQueryDSL.forJpa( QWildcardEntity.entity, RqlParser.from( query ) );
      assertThat( queryModelToQueryDS1.getPredicate() ).isPresent();
   }

   @ParameterizedTest
   @MethodSource( { "generateLikeExpressionWithNotAllowedWildcardCount" } )
   void notAllowedLikeExpressionCount( final String query ) {
      final RqlQueryModel rqlQueryModel = RqlParser.from( query );
      assertThatThrownBy( () -> QueryModelToQueryDSL.forJpa( QWildcardEntity.entity, rqlQueryModel ) ).isInstanceOf(
            NoSuchFieldQueryException.class ).hasMessageContaining( "Too many wildcards" );
   }

   @ParameterizedTest
   @MethodSource( { "generateLikeExpressionWithAllowedWildcardPattern" } )
   void allowedLikeExpressionPattern( final String query ) {
      final QueryModelToQueryDSL queryModelToQueryDS1 = QueryModelToQueryDSL.forJpa( QWildcardEntity.entity, RqlParser.from( query ) );
      assertThat( queryModelToQueryDS1.getPredicate() ).isPresent();
   }

   @ParameterizedTest
   @MethodSource( { "generateLikeExpressionWithNotAllowedWildcardPattern" } )
   void notAllowedLikeExpressionPattern( final String query ) {
      final RqlQueryModel rqlQueryModel = RqlParser.from( query );
      assertThatThrownBy( () -> QueryModelToQueryDSL.forJpa( QWildcardEntity.entity, rqlQueryModel ) ).isInstanceOf(
                  NoSuchFieldQueryException.class ).hasMessageContaining( "Wildcard for" )
            .hasMessageContaining( "is not allowed on this position." );
   }

   @Test
   void customErrorMessage() {
      final RqlQueryModel rqlQueryModel = RqlParser.from( "filter=like(error1, \"*a***\")" );
      assertThatThrownBy(
            () -> QueryModelToQueryDSL.forJpa( QWildcardEntity.entity, rqlQueryModel ) ).isInstanceOf(
            NoSuchFieldQueryException.class ).hasMessageContaining( "my error" );

      final RqlQueryModel rqlQueryModel2 = RqlParser.from( "filter=like(error2, \"*a***\")" );
      assertThatThrownBy(
            () -> QueryModelToQueryDSL.forJpa( QWildcardEntity.entity, rqlQueryModel2 ) ).isInstanceOf(
            NoSuchFieldQueryException.class ).hasMessageContaining( "my error" );
   }

   /**
    * Generates testcases where the like wildcard is allowed
    */
   static Stream<Arguments> generateLikeExpressionWithAllowedWildcardCount() {
      return Stream.of(
            arguments( "filter=like(count1, \"%a%\")" ),
            arguments( "filter=like(count1, \"%a\")" ),
            arguments( "filter=like(count1, \"a%\")" ),
            arguments( "filter=like(count1, \"a\")" ),
            arguments( "filter=like(type, \"%a%\")" ),
            arguments( "filter=like(type, \"%a\")" ),
            arguments( "filter=like(type, \"a%\")" ),
            arguments( "filter=like(type, \"a\")" ),
            arguments( "filter=like(count2, \"a%\")" ),
            arguments( "filter=like(count2, \"%a\")" ),
            arguments( "filter=like(count2, \"a\")" ),
            arguments( "filter=and(like(subEntities.name,\"%a\"),like(subEntities.id,\"b%\"))" ),
            arguments( "filter=and(like(type,\"%a%\"),like(id,\"b%\"))" )
      );
   }

   /**
    * Generates testcases where the like wildcard is not allowed
    */
   static Stream<Arguments> generateLikeExpressionWithNotAllowedWildcardCount() {
      return Stream.of(
            arguments( "filter=like(count1, \"%ab%bbb%\")" ),
            arguments( "filter=like(count2, \"%a%\")" ),
            arguments( "filter=like(count2, \"%add%dd%\")" ),
            arguments( "filter=and(like(subEntities.id,\"a%\"),like(subEntities.name,\"%ddd%b%\"))" ),
            arguments( "filter=and(like(type,\"%a%\"),like(count2,\"%b%\"))" ),
            arguments( "filter=like(count1, \"*ab*bbb*\")" ),
            arguments( "filter=like(count2, \"*a*\")" ),
            arguments( "filter=like(count2, \"*add*dd*\")" ),
            arguments( "filter=and(like(subEntities.id,\"a*\"),like(subEntities.name,\"*ddd*b*\"))" ),
            arguments( "filter=and(like(type,\"*a*\"),like(count2,\"*b*\"))" ),
            arguments( "filter=like(count1, \"%ab*bbb*\")" ),
            arguments( "filter=like(count2, \"*a%\")" ),
            arguments( "filter=like(count2, \"%add*dd*\")" ),
            arguments( "filter=and(like(subEntities.id,\"a*\"),like(subEntities.name,\"%ddd*b*\"))" ),
            arguments( "filter=and(like(type,\"*a*\"),like(count2,\"*b*\"))" )
      );
   }

   /**
    * Generates testcases where the like wildcard is allowed
    */
   static Stream<Arguments> generateLikeExpressionWithAllowedWildcardPattern() {
      return Stream.of(
            arguments( "filter=like(regex1, \"*a\")" ),
            arguments( "filter=like(regex1, \"*abccb\")" ),
            arguments( "filter=like(regex1, \"aabd\")" ),
            arguments( "filter=like(regex1, \"a*\")" ),
            arguments( "filter=like(type, \"*a*\")" ),
            arguments( "filter=like(type, \"*a\")" ),
            arguments( "filter=like(type, \"a*\")" ),
            arguments( "filter=like(type, \"a\")" ),
            arguments( "filter=like(regex2, \"ad*\")" ),
            arguments( "filter=like(regex2, \"*a*\")" ),
            arguments( "filter=and(like(subEntities.type,\"*a\"),like(subEntities.id,\"*b\"))" ),
            arguments( "filter=and(like(type,\"*a*\"),like(id,\"*b\"))" )
      );
   }

   /**
    * Generates testcases where the like wildcard is not allowed
    */
   static Stream<Arguments> generateLikeExpressionWithNotAllowedWildcardPattern() {
      return Stream.of(
            arguments( "filter=like(regex1, \"*a*\")" ),
            arguments( "filter=like(regex1, \"ab*ccb*\")" ),
            arguments( "filter=like(regex1, \"a*abd*\")" ),
            arguments( "filter=like(regex2, \"*a\")" ),
            arguments( "filter=like(regex2, \"a*vvv\")" ),
            arguments( "filter=and(like(subEntities.type,\"*a*s*\"),like(id,\"*b*\"))" ),
            arguments( "filter=and(like(type,\"*a*\"),like(regex1,\"*b*\"))" )
      );
   }

   /**
    * Needed because mockito does not spy on lambdas
    */
   private static class SpyableUuidConverter implements Function<Object, UUID> {
      @Override
      public UUID apply( final Object o ) {
         return UUID.fromString( (String) o );
      }
   }
}

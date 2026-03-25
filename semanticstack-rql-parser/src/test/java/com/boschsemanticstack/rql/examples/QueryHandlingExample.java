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

package com.boschsemanticstack.rql.examples;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.parser.v1.RqlParser;

public class QueryHandlingExample {

   private final RestClient someRestClient = null;

   public RestResponse getSomeResourceWithRqlUsingQueryParameters( final RqlQueryModel query ) {

      final Map<String, String> queryParameters = RqlParser.toQueryParameters( query );

      return someRestClient.post( "someAddress" )
            .addQueryParam( "select", queryParameters.get( "select" ) ) // <1>
            .addQueryParam( "filter", queryParameters.get( "filter" ) ) // <1>
            .addQueryParam( "option", queryParameters.get( "option" ) ) // <1>
            .execute();
   }

   public void someRestEndpoint(
         final String selectParam, // <2>
         final String filterParam, // <2>
         final String optionParam ) { //<2>

      final String queryString = Stream.of(
                  Optional.ofNullable( selectParam ).map( s -> "select=" ), // <1>
                  Optional.ofNullable( filterParam ).map( s -> "filter=" ), // <1>
                  Optional.ofNullable( optionParam ).map( s -> "option=" )  // <1>
            )
            .filter( Optional::isPresent )
            .map( Optional::get )
            .collect( Collectors.joining( "&" ) );

      final RqlQueryModel from = RqlParser.from( queryString );

      doSomethingWithModel( from );
   }

   private static void doSomethingWithModel( RqlQueryModel from ) {
      // do something with query model
      if( from.getChildren().isEmpty()){
         return;
      }
      System.out.println( from );
   }

   public void someRestEndpoint( final String theWholeQuery ) {//<1>

      // this highly depends on your rest backend therefore no api call to do this is provided
      final RqlQueryModel from = RqlParser.from( Optional.ofNullable( theWholeQuery ).orElse( "" ) );

      doSomethingWithModel( from );
   }

   public interface RestClient {
      RestClient post( String address );

      RestResponse execute();

      RestClient addQueryParam( String select, String select1 );
   }

   public interface RestResponse {

   }
}

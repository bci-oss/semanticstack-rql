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

package com.boschsemanticstack.rql.examples;

import java.util.Iterator;
import java.util.List;

import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.model.v1.impl.RqlToStringWriter;
import com.boschsemanticstack.rql.parser.v1.RqlParser;
import io.reactivex.Observable;

public class AccessPagedResourceExample {

   private final RestClient someRestClient = null;

   // tag::recursive_restrequest[]
   public Observable<String> getSomethingRemote( final RqlQueryModel query ) { // <1>
      final int pageSize = 170;  // entirely dependent on remote Service
      return getSomeResourceRecursive( RqlParser.getPagedQuery( query, pageSize ) );
   }

   private Observable<String> getSomeResourceRecursive( final Iterator<RqlQueryModel> pagedQuery ) {

      if ( pagedQuery.hasNext() ) {
         final RqlQueryModel next = pagedQuery.next();
         return someRestClient.post( "someAddress" )
               .withBody( new RqlToStringWriter().visitModel( next ) )
               .toObservableResponse()
               .flatMap( response -> doRecursionIfMoreMeasurementsAvailable(
                     pagedQuery,
                     next.getOptions().getSlice().get().limit(), //<2>
                     response )
               );
      }
      return Observable.empty();
   }

   private Observable<String> doRecursionIfMoreMeasurementsAvailable(
         final Iterator<RqlQueryModel> pagedQuery,
         final long pageSize,
         final RestResponse response ) {

      if ( response.getResponseCode() != 200 ) {
         return Observable.error( new RuntimeException( "Remote service responded with " + response.getResponseCode() ) );
      }

      final List<String> results = response.getBodyAsList();
      Observable<String> observableResults = Observable.fromIterable( results );

      if ( results.size() == pageSize && pagedQuery.hasNext() ) { //<3>
         observableResults = observableResults.concatWith( Observable.defer(//<4>
               () -> getSomeResourceRecursive( pagedQuery ) )
         );
      }

      return observableResults;
   }
   // end::recursive_restrequest[]

   private interface RestClient {
      RestClient post( String address );

      RestClient withBody( String body );

      Observable<RestResponse> toObservableResponse();
   }

   private interface RestResponse {

      int getResponseCode();

      List<String> getBodyAsList();
   }
}

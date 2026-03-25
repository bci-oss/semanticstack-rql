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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.model.v1.impl.RqlToStringWriter;
import com.boschsemanticstack.rql.parser.v1.RqlParser;

public class AccessPagedResourceExample {

   private final RestClient someRestClient = null;

   public List<String> getSomethingRemote( final RqlQueryModel query ) { // <1>
      final int pageSize = 170;  // entirely dependent on remote Service
      return getSomeResourceRecursive( RqlParser.getPagedQuery( query, pageSize ) );
   }

   private List<String> getSomeResourceRecursive( final Iterator<RqlQueryModel> pagedQuery ) {
      final List<String> allResults = new ArrayList<>();

      while ( pagedQuery.hasNext() ) {
         final RqlQueryModel next = pagedQuery.next();
         final RestResponse response = someRestClient.post( "someAddress" )
               .withBody( new RqlToStringWriter().visitModel( next ) )
               .execute();

         if ( response.getResponseCode() != 200 ) {
            throw new RuntimeException( "Remote service responded with " + response.getResponseCode() );
         }

         final List<String> results = response.getBodyAsList();
         allResults.addAll( results );

         final long pageLimit = next.getOptions().getSlice().get().limit(); //<2>
         if ( results.size() < pageLimit ) { //<3>
            break; // last page reached, no more data available
         }
      }

      return allResults;
   }

   private interface RestClient {
      RestClient post( String address );

      RestClient withBody( String body );

      RestResponse execute();
   }

   private interface RestResponse {

      int getResponseCode();

      List<String> getBodyAsList();
   }
}

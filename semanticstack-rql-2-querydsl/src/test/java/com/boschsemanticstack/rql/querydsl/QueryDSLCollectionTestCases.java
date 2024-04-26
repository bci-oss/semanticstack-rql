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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class QueryDSLCollectionTestCases {
   private final XmlMapper mapper = new XmlMapper();
   private static final String SUBENTITY_SELECT_FROM = "\n"
         + "where entity in (select entity\n"
         + "from Entity entity\n"
         + "  left join entity.subEntities as subEntity\n";
   private static final String SUBENTITY_SELECT_FROM_SUFFIX = ")";

   public List<Case> getCases() {
      try {
         final Cases cases = mapper.readValue( getClass().getClassLoader().getResource( "collection_test_cases.xml" ), Cases.class );
         return cases.getCases();
      } catch ( final IOException e ) {
         throw new RuntimeException( e );
      }
   }

   public static class Cases {
      private List<Case> cases = new ArrayList<>();

      public void setCases( final List<Case> cases ) {
         this.cases = cases;
      }

      public List<Case> getCases() {
         return cases;
      }
   }

   public static class Case {

      private String description;

      private String jpa;
      private String rql;

      public Case() {
         // required for deserialization
      }

      public String getDescription() {
         return description;
      }

      public Case setDescription( final String description ) {
         this.description = description;
         return this;
      }

      public String getRql() {
         return rql;
      }

      public Case setRql( final String rql ) {
         this.rql = rql;
         return this;
      }

      public String getJpa() {
         return jpa;
      }

      public Case setJpa( final String jpa ) {
         this.jpa = jpa;
         return this;
      }
   }
}

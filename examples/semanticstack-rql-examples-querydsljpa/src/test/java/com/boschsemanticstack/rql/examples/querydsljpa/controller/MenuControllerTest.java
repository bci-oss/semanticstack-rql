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

package com.boschsemanticstack.rql.examples.querydsljpa.controller;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.boschsemanticstack.rql.examples.querydsljpa.controller.model.Rql;

@SpringBootTest
@WebAppConfiguration
class MenuControllerTest {

   @Autowired
   private WebApplicationContext context;

   private MockMvc mvc;

   @BeforeEach
   public void setup() {
      mvc = MockMvcBuilders.webAppContextSetup( context ).build();
   }

   @Test
   void shouldReturnQueryInSelfLink() throws Exception {
      final Rql expected = Rql.withOption( "sort(+name),limit(0,2)" );
      mvc.perform( get( "/api/v1/menu" ) ) //
            .andExpect( status().isOk() )
            .andExpect( jsonPath( "$.count" ).value( 2 ) )
            .andExpect( jsonPath( "$.links[0].rel" ).value( "self" ) )
            .andExpect( jsonPath( "$.links[0].href" ).value( endsWith( expected.encode().toString() ) ) );
   }

   @Test
   void shouldAcceptFilterExpression() throws Exception {
      final Rql expected = new Rql( "like(name,\"*Rql\")", "limit(0,20)" );
      mvc.perform( get( "/api/v1/menu?filter=like(name,\"*Rql\")&option=limit(0,20)" ) ) //
            .andExpect( status().isOk() )
            .andExpect( jsonPath( "$.pageSize" ).value( 20 ) )
            .andExpect( jsonPath( "$.links[0].rel" ).value( "self" ) )
            .andExpect( jsonPath( "$.links[0].href" ).value( endsWith( expected.encode().toString() ) ) );
   }
}

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

import static com.boschsemanticstack.rql.examples.querydsljpa.controller.QueryDslRepositoryFilter.inRepository;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boschsemanticstack.rql.examples.querydsljpa.controller.model.PageResource;
import com.boschsemanticstack.rql.examples.querydsljpa.controller.model.Rql;
import com.boschsemanticstack.rql.examples.querydsljpa.model.Burger;
import com.boschsemanticstack.rql.examples.querydsljpa.model.QBurger;
import com.boschsemanticstack.rql.examples.querydsljpa.repository.BurgerRepository;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.parser.v1.RqlParser;
import com.boschsemanticstack.rql.querydsl.QueryModelToQueryDSL;

import lombok.SneakyThrows;

@RestController
@RequestMapping( value = "/api/v1/menu", produces = "application/hal+json" )
public class MenuController {

   private final BurgerRepository burgerRepo;

   public MenuController( final BurgerRepository burgerRepo ) {
      this.burgerRepo = burgerRepo;
   }

   @GetMapping
   @SneakyThrows
   public ResponseEntity<PageResource<Burger>> getMenu( @RequestParam( value = "filter", required = false ) final String filter,
         @RequestParam( value = "option", defaultValue = "sort(+name),limit(0,2)" ) final String option ) {
      final Rql rql = new Rql( filter, option );

      final RqlQueryModel queryModel = RqlParser.from( rql.toString() );
      final QueryModelToQueryDSL bridge = QueryModelToQueryDSL.forJpa( QBurger.burger, queryModel );

      final Page<Burger> page = inRepository( burgerRepo ).findWithQuery( bridge );

      final Link selfRel = Link.of( linkTo( MenuController.class ) + "?" + rql.encode(), "self" );
      return new ResponseEntity<>( PageResource.of( page ).withLink( selfRel ), HttpStatus.OK );
   }
}

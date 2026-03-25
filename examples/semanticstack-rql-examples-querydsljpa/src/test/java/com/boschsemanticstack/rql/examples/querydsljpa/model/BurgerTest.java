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

package com.boschsemanticstack.rql.examples.querydsljpa.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BurgerTest {

   @Test
   void shouldCreateBurgerWithDefaultConstructor() {
      final Burger burger = new Burger();
      assertThat( burger )
            .returns( 0L, Burger::getId )
            .returns( null, Burger::getName )
            .returns( null, Burger::getDressing );
   }

   @Test
   void shouldCreateBurgerWithAllArgsConstructor() {
      final Burger burger = new Burger( 1L, "TestBurger", Dressing.MUSTARD );
      assertThat( burger )
            .returns( 1L, Burger::getId )
            .returns( "TestBurger", Burger::getName )
            .returns( Dressing.MUSTARD, Burger::getDressing );
   }

   @Test
   void shouldSetId() {
      final Burger burger = new Burger();
      burger.setId( 42L );
      assertThat( burger.getId() ).isEqualTo( 42L );
   }

   @Test
   void shouldSetName() {
      final Burger burger = new Burger();
      burger.setName( "CheeseBurger" );
      assertThat( burger.getName() ).isEqualTo( "CheeseBurger" );
   }

   @Test
   void shouldSetDressing() {
      final Burger burger = new Burger();
      burger.setDressing( Dressing.CHILI_CHEESE_SAUCE );
      assertThat( burger.getDressing() ).isEqualTo( Dressing.CHILI_CHEESE_SAUCE );
   }

   @Test
   void shouldBeEqualWhenSameValues() {
      final Burger burger1 = new Burger( 1L, "TestBurger", Dressing.MUSTARD );
      final Burger burger2 = new Burger( 1L, "TestBurger", Dressing.MUSTARD );
      assertThat( burger1 ).isEqualTo( burger2 );
   }

   @Test
   void shouldNotBeEqualToNull() {
      final Burger burger = new Burger( 1L, "TestBurger", Dressing.MUSTARD );
      assertThat( burger ).isNotEqualTo( null );
   }

   @Test
   void shouldNotBeEqualWhenDifferentId() {
      final Burger burger1 = new Burger( 1L, "TestBurger", Dressing.MUSTARD );
      final Burger burger2 = new Burger( 2L, "TestBurger", Dressing.MUSTARD );
      assertThat( burger1 ).isNotEqualTo( burger2 );
   }

   @Test
   void shouldNotBeEqualWhenDifferentName() {
      final Burger burger1 = new Burger( 1L, "TestBurger", Dressing.MUSTARD );
      final Burger burger2 = new Burger( 1L, "OtherBurger", Dressing.MUSTARD );
      assertThat( burger1 ).isNotEqualTo( burger2 );
   }

   @Test
   void shouldNotBeEqualWhenDifferentDressing() {
      final Burger burger1 = new Burger( 1L, "TestBurger", Dressing.MUSTARD );
      final Burger burger2 = new Burger( 1L, "TestBurger", Dressing.TOMATO_SAUCE );
      assertThat( burger1 ).isNotEqualTo( burger2 );
   }

   @Test
   void shouldHaveSameHashCodeWhenEqual() {
      final Burger burger1 = new Burger( 1L, "TestBurger", Dressing.MUSTARD );
      final Burger burger2 = new Burger( 1L, "TestBurger", Dressing.MUSTARD );
      assertThat( burger1 ).hasSameHashCodeAs( burger2 );
   }

   @Test
   void shouldHaveDifferentHashCodeWhenNotEqual() {
      final Burger burger1 = new Burger( 1L, "TestBurger", Dressing.MUSTARD );
      final Burger burger2 = new Burger( 2L, "OtherBurger", Dressing.TOMATO_SAUCE );
      assertThat( burger1 ).doesNotHaveSameHashCodeAs( burger2 );
   }

   @Test
   void shouldReturnMeaningfulToString() {
      final Burger burger = new Burger( 1L, "TestBurger", Dressing.MUSTARD );
      assertThat( burger ).hasToString( "Burger(id=1, name=TestBurger, dressing=MUSTARD)" );
   }

   @Test
   void shouldBuildBurgerWithBuilder() {
      final Burger burger = Burger.builder()
            .id( 5L )
            .name( "BuilderBurger" )
            .dressing( Dressing.CHILI_CHEESE_SAUCE )
            .build();
      assertThat( burger.getId() ).isEqualTo( 5L );
      assertThat( burger.getName() ).isEqualTo( "BuilderBurger" );
      assertThat( burger.getDressing() ).isEqualTo( Dressing.CHILI_CHEESE_SAUCE );
   }

   @Test
   void shouldBuildBurgerWithDefaultValues() {
      final Burger burger = Burger.builder().build();
      assertThat( burger.getId() ).isZero();
      assertThat( burger.getName() ).isNull();
      assertThat( burger.getDressing() ).isNull();
   }
}

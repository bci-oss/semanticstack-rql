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

package com.boschsemanticstack.rql.examples.querydsljpa.controller.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RqlTest {

   @Test
   void shouldDecodeEncodedFilter() {
      final Rql original = new Rql( "like(name,\"*Rql\")", "limit(0,20)" );
      final Rql encoded = original.encode();
      final Rql decoded = encoded.decode();
      assertThat( decoded.toString() ).isEqualTo( original.toString() );
   }

   @Test
   void shouldDecodeWithNullFilter() {
      final Rql encoded = new Rql( null, "limit%280%2C20%29" );
      final Rql decoded = encoded.decode();
      assertThat( decoded.toString() ).isEqualTo( "option=limit(0,20)" );
   }

   @Test
   void shouldDecodeWithNullOption() {
      final Rql encoded = new Rql( "like%28name%2C%22*Rql%22%29", null );
      final Rql decoded = encoded.decode();
      assertThat( decoded.toString() ).contains( "like(name,\"*Rql\")" );
   }

   @Test
   void shouldEncodeAndDecodeRoundTrip() {
      final Rql original = new Rql( "eq(dressing,\"MUSTARD\")", "sort(+name),limit(0,10)" );
      final Rql roundTripped = original.encode().decode();
      assertThat( roundTripped.toString() ).isEqualTo( original.toString() );
   }

   @Test
   void shouldDecodeAlreadyDecodedValues() {
      final Rql rql = new Rql( "like(name,\"*Burger\")", "limit(0,5)" );
      final Rql decoded = rql.decode();
      assertThat( decoded.toString() ).isEqualTo( rql.toString() );
   }

   @Test
   void shouldCreateWithOptionOnly() {
      final Rql rql = Rql.withOption( "sort(+name),limit(0,2)" );
      assertThat( rql ).hasToString( "option=sort(+name),limit(0,2)" );
   }

   @Test
   void shouldEncodeWithNullValues() {
      final Rql rql = new Rql( null, null );
      final Rql encoded = rql.encode();
      assertThat( encoded ).isNotNull();
   }

   @Test
   void shouldDecodeWithNullValues() {
      final Rql rql = new Rql( null, null );
      final Rql decoded = rql.decode();
      assertThat( decoded ).isNotNull();
   }

   @Test
   void shouldFormatToStringWithFilter() {
      final Rql rql = new Rql( "eq(name,\"Test\")", "limit(0,10)" );
      assertThat( rql ).hasToString( "filter=eq(name,\"Test\")&option=limit(0,10)" );
   }

   @Test
   void shouldFormatToStringWithoutFilter() {
      final Rql rql = new Rql( null, "limit(0,10)" );
      assertThat( rql ).hasToString( "option=limit(0,10)" );
   }

   @Test
   void shouldFormatToStringWithEmptyFilter() {
      final Rql rql = new Rql( "", "limit(0,10)" );
      assertThat( rql ).hasToString( "option=limit(0,10)" );
   }
}

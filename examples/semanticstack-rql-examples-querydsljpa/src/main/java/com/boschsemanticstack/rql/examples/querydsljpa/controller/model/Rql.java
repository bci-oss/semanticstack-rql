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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.util.StringUtils;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor
public class Rql {
   private String filter;
   private String option;

   @Override
   public String toString() {
      return StringUtils.hasLength( filter ) ? "filter=" + filter + "&option=" + option : "option=" + option;
   }

   @SneakyThrows
   public Rql encode() {
      return new Rql( //
            filter != null ? URLEncoder.encode( filter, StandardCharsets.UTF_8 ) : null,
            option != null ? URLEncoder.encode( option, StandardCharsets.UTF_8 ) : null );
   }

   @SneakyThrows
   public Rql decode() {
      return new Rql( //
            filter != null ? URLDecoder.decode( filter, StandardCharsets.UTF_8 ) : null,
            option != null ? URLDecoder.decode( option, StandardCharsets.UTF_8 ) : null );
   }

   public static Rql withOption( final String option ) {
      return new Rql( null, option );
   }
}

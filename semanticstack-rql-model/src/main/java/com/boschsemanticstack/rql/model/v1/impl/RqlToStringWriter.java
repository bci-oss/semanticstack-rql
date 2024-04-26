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

package com.boschsemanticstack.rql.model.v1.impl;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.boschsemanticstack.rql.model.v1.RqlQueryModel;

public class RqlToStringWriter extends AbstractRqlWriter {

   @Override
   public String visitModel( final RqlQueryModel model ) {
      return Stream.of(
                  Optional.of( model.getSelect() )
                        .filter( select -> !select.isEmpty() )
                        .map( this::visitSelect )
                        .map( filterString -> String.format( "select=%s", filterString ) ),

                  model.getFilter()
                        .map( this::visitFilter )
                        .map( filterString -> String.format( "filter=%s", filterString ) ),

                  Optional.ofNullable( model.getOptions().accept( this ) )
                        .map( filterString -> String.format( "option=%s", filterString ) )
            )
            .filter( Optional::isPresent )
            .map( Optional::get )
            .collect( Collectors.joining( "&" ) );
   }
}

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
package com.boschsemanticstack.rql.model.v1;

import java.util.Optional;

/**
 * contains parameters for paging support if not the whole resultset is wanted.
 *
 * @see RqlQueryModel
 */
public interface RqlOptions extends RqlModelNode {

   RqlOrder getOrder();

   Optional<RqlSlice> getSlice();

   boolean isEmpty();

   @Override
   default <T> T accept( final RqlModelVisitor<? extends T> visitor ) {
      return visitor.visitOptions( this );
   }
}

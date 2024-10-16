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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * contains parameters for cursor paging support if not the whole result set is wanted.
 *
 * @see RqlQueryModel
 */
public interface RqlCursor extends RqlModelNode {

   Optional<String> cursor();

   long limit();

   @Override
   default <T> T accept( final RqlModelVisitor<? extends T> visitor ) {
      return visitor.visitCursor( this );
   }

   @Override
   default List<? extends RqlModelNode> getChildren() {
      return Collections.emptyList();
   }

   @Override
   default int getChildCount() {
      return 0;
   }
}

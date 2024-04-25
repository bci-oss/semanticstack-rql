/*
 * Copyright (c)2024 Robert Bosch Manufacturing Solutions GmbH
 *
 *  See the AUTHORS file(s) distributed with this work for additional
 *  information regarding authorship.
 *
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 *  SPDX-License-Identifier: MPL-2.0
 */
package com.boschsemanticstack.rql.model.v1;

import java.util.Collections;
import java.util.List;

/**
 * contains a attribute name and a order direction. Used to create order clauses
 * of SQL statements.
 *
 * @see RqlQueryModel
 */
public interface RqlFieldDirection extends RqlModelNode {

   enum Direction {
      ASCENDING, DESCENDING
   }

   String attribute();

   Direction direction();

   @Override
   default <T> T accept( final RqlModelVisitor<? extends T> visitor ) {
      return visitor.visitFieldDirection( this );
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

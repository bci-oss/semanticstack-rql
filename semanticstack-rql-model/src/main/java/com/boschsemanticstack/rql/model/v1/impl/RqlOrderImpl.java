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
package com.boschsemanticstack.rql.model.v1.impl;

import java.util.Collections;
import java.util.List;

import com.boschsemanticstack.rql.model.v1.RqlFieldDirection;
import com.boschsemanticstack.rql.model.v1.RqlModelNode;
import com.boschsemanticstack.rql.model.v1.RqlModelVisitor;
import com.boschsemanticstack.rql.model.v1.RqlOrder;

public record RqlOrderImpl( List<RqlFieldDirection> fieldDirections ) implements RqlOrder {

   /**
    * @param fieldDirections has to be not-null not-empty
    */
   public RqlOrderImpl( final List<RqlFieldDirection> fieldDirections ) {
      this.fieldDirections = null == fieldDirections
            ? Collections.emptyList()
            : List.copyOf( fieldDirections );
   }

   @Override
   public <T> T accept( final RqlModelVisitor<? extends T> visitor ) {
      return visitor.visitOrder( this );
   }

   @Override
   public List<? extends RqlModelNode> getChildren() {
      return Collections.emptyList();
   }

   @Override
   public int getChildCount() {
      return 0;
   }
}

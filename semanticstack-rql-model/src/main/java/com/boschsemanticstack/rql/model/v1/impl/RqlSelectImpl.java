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

import java.util.Collections;
import java.util.List;

import com.boschsemanticstack.rql.model.v1.RqlModelNode;
import com.boschsemanticstack.rql.model.v1.RqlModelVisitor;
import com.boschsemanticstack.rql.model.v1.RqlSelect;

public record RqlSelectImpl(List<String> attributes) implements RqlSelect {

   public RqlSelectImpl( final List<String> attributes ) {
      this.attributes = null == attributes
            ? Collections.emptyList()
            : List.copyOf( attributes );
   }

   /*
    * (non-Javadoc)
    *
    * @see com.bosch.inl.core.common.rql.RqlSelect#contains(java.lang.String)
    */
   @Override
   public boolean contains( final String attribute ) {
      return attributes.stream().anyMatch( attribute::startsWith );
   }

   @Override
   public boolean hasAttributeStartingWith( final String attribute ) {
      return attributes.stream().anyMatch( a -> a.startsWith( attribute ) );
   }

   /*
    * (non-Javadoc)
    *
    * @see com.bosch.inl.core.common.rql.RqlSelect#isEmpty()
    */
   @Override
   public boolean isEmpty() {
      return attributes.isEmpty();
   }

   @Override
   public boolean isExplicitSelected( final String attributeName ) {
      if ( isEmpty() ) {
         return true;
      }
      for ( final String selected : attributes ) {
         if ( selected.equals( attributeName ) ) {
            return true;
         }
      }
      return false;
   }

   @Override
   public boolean isImplicitSelected( final String attributeNamePrefix ) {
      if ( isEmpty() ) {
         return true;
      }
      for ( final String selected : attributes ) {
         if ( attributeNamePrefix.startsWith( selected ) ) {
            return true;
         }
      }
      return false;
   }

   @Override
   public <T> T accept( final RqlModelVisitor<? extends T> visitor ) {
      return visitor.visitSelect( this );
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

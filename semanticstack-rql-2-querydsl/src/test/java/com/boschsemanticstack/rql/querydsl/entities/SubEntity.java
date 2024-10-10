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

package com.boschsemanticstack.rql.querydsl.entities;

import java.util.Map;

import com.boschsemanticstack.rql.annotation.RqlPattern;
import com.boschsemanticstack.rql.annotation.WildcardCount;

public class SubEntity {

   private String id;
   @WildcardCount( count = 2 )
   private String name;

   @RqlPattern( regex = "^\\*?[^\\*]+$|^[^\\*]+?\\*?$" )
   @WildcardCount( count = 3 )
   private String type;
   private SubEntity3 entity;
   private Map<String, String> metadata;

   protected String getId() {
      return id;
   }
 
   protected SubEntity setId( final String id ) {
      this.id = id;
      return this;
   }

   protected String getName() {
      return name;
   }

   protected SubEntity setName( final String name ) {
      this.name = name;
      return this;
   }

   protected String getType() {
      return type;
   }

   protected SubEntity setType( final String type ) {
      this.type = type;
      return this;
   }

   protected Map<String, String> getMetadata() {
      return metadata;
   }

   protected SubEntity setMetadata( final Map<String, String> metadata ) {
      this.metadata = metadata;
      return this;
   }

   public SubEntity3 getEntity() {
      return entity;
   }

   public void setEntity( final SubEntity3 entity ) {
      this.entity = entity;
   }
}

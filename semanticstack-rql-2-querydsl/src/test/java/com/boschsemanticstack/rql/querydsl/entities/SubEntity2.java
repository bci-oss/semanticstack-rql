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

public class SubEntity2 {

   private String id;
   private String name;
   private String type;
   private Map<String, String> metadata;

   protected String getId() {
      return id;
   }

   protected SubEntity2 setId( final String id ) {
      this.id = id;
      return this;
   }

   protected String getName() {
      return name;
   }

   protected SubEntity2 setName( final String name ) {
      this.name = name;
      return this;
   }

   protected String getType() {
      return type;
   }

   protected SubEntity2 setType( final String type ) {
      this.type = type;
      return this;
   }

   protected Map<String, String> getMetadata() {
      return metadata;
   }

   protected SubEntity2 setMetadata( final Map<String, String> metadata ) {
      this.metadata = metadata;
      return this;
   }
}

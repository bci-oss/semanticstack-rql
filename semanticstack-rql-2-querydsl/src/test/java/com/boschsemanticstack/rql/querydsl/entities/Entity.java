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

import java.util.List;
import java.util.UUID;

public class Entity {

   private String id;
   private UUID specialId;
   private String name;
   private String type;
   private Boolean special;
   private SubEntity subEntity;
   private List<SubEntity> subEntities;
   private List<SubEntity2> subEntities2;
   private List<String> stringList;

   protected String getId() {
      return id;
   }

   protected Entity setId( final String id ) {
      this.id = id;
      return this;
   }

   protected UUID getSpecialId() {
      return specialId;
   }

   protected void setSpecialId( final UUID specialId ) {
      this.specialId = specialId;
   }

   protected String getName() {
      return name;
   }

   protected Entity setName( final String name ) {
      this.name = name;
      return this;
   }

   protected String getType() {
      return type;
   }

   protected Entity setType( final String type ) {
      this.type = type;
      return this;
   }

   protected Boolean isSpecial() {
      return special;
   }

   protected Entity setSpecial( final Boolean special ) {
      this.special = special;
      return this;
   }

   protected List<SubEntity> getSubEntities() {
      return subEntities;
   }

   protected Entity setSubEntities( final List<SubEntity> subEntities ) {
      this.subEntities = subEntities;
      return this;
   }

   protected SubEntity getSubEntity() {
      return subEntity;
   }

   protected Entity setSubEntity( final SubEntity subEntity ) {
      this.subEntity = subEntity;
      return this;
   }

   protected List<SubEntity2> getSubEntities2() {
      return subEntities2;
   }

   protected Entity setSubEntities2( final List<SubEntity2> subEntities2 ) {
      this.subEntities2 = subEntities2;
      return this;
   }

   protected List<String> getStringList() {
      return stringList;
   }

   protected Entity setStringList( final List<String> stringList ) {
      this.stringList = stringList;
      return this;
   }
}

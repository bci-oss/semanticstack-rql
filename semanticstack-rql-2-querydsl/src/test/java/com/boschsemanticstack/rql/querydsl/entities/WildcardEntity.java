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

import com.boschsemanticstack.rql.annotation.RqlPattern;
import com.boschsemanticstack.rql.annotation.WildcardCount;

public class WildcardEntity {

   @WildcardCount( count = 2 )
   private String count1;
   @WildcardCount
   private String count2;

   @RqlPattern( regex = "^\\*?[^\\*]+$|^[^\\*]+?\\*?$" )
   private String regex1;
   @RqlPattern( regex = ".+[^\\*]\\*$" )
   private String regex2;

   private String id;
   private String type;

   private List<SubEntity> subEntities;

   @WildcardCount( errorMessage = "my error" )
   private String error1;

   @RqlPattern( regex = "^\\*?[^\\*]+$|^[^\\*]+?\\*?$", errorMessage = "my error" )
   private String error2;

   public String getCount1() {
      return count1;
   }

   public void setCount1( String count1 ) {
      this.count1 = count1;
   }

   public String getCount2() {
      return count2;
   }

   public void setCount2( String count2 ) {
      this.count2 = count2;
   }

   public String getRegex1() {
      return regex1;
   }

   public void setRegex1( String regex1 ) {
      this.regex1 = regex1;
   }

   public String getRegex2() {
      return regex2;
   }

   public void setRegex2( String regex2 ) {
      this.regex2 = regex2;
   }

   public String getId() {
      return id;
   }

   public void setId( String id ) {
      this.id = id;
   }

   public String getType() {
      return type;
   }

   public void setType( String type ) {
      this.type = type;
   }

   public List<SubEntity> getSubEntities() {
      return subEntities;
   }

   public void setSubEntities( List<SubEntity> subEntities ) {
      this.subEntities = subEntities;
   }

   public String getError1() {
      return error1;
   }

   public void setError1( String error1 ) {
      this.error1 = error1;
   }

   public String getError2() {
      return error2;
   }

   public void setError2( String error2 ) {
      this.error2 = error2;
   }
}

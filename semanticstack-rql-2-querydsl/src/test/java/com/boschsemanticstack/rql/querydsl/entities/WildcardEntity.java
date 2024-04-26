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

}

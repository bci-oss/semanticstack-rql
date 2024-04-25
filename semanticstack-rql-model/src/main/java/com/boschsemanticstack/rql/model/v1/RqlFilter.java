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

import java.util.List;

/**
 * @see RqlFilter
 * @see RqlQueryModel
 */
public interface RqlFilter extends RqlModelNode {

   enum FilterType {
      AND,
      OR,
      NOT,
      VALUE
   }

   enum OperatorGroup {
      IN( "in" ),
      COMPARE( "compare" ),
      LIKE( "like" );

      private final String name;

      OperatorGroup( final String name ) {
         this.name = name;
      }

      public String getName() {
         return name;
      }
   }

   enum Operator {
      EQ( "eq", OperatorGroup.COMPARE ),
      NE( "ne", OperatorGroup.COMPARE ),
      GT( "gt", OperatorGroup.COMPARE ),
      GE( "ge", OperatorGroup.COMPARE ),
      LT( "lt", OperatorGroup.COMPARE ),
      LE( "le", OperatorGroup.COMPARE ),
      LIKE( "like", OperatorGroup.LIKE ),
      LIKE_IGNORE_CASE( "likeIgnoreCase", OperatorGroup.LIKE ),
      IN( "in", OperatorGroup.IN );

      private final String name;
      private final OperatorGroup group;

      Operator( final String name, final OperatorGroup group ) {
         this.name = name;
         this.group = group;
      }

      public String getName() {
         return name;
      }

      public OperatorGroup getGroup() {
         return group;
      }
   }

   String getAttribute();

   Operator getOperator();

   FilterType getFilterType();

   @Override
   List<RqlFilter> getChildren();

   List<Object> getValues();

   Object getValue();
}

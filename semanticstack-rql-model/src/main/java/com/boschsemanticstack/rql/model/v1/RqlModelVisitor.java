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

public abstract class RqlModelVisitor<T> {

   public abstract T visitModel( RqlQueryModel model );
 
   public abstract T visitSelect( RqlSelect selectModel );

   public abstract T visitFilter( RqlFilter model );

   public abstract T visitOptions( RqlOptions model );

   public abstract T visitSlice( RqlSlice model );

   public abstract T visitCursor( RqlCursor model );

   public abstract T visitOrder( RqlOrder model );

   public abstract T visitLogicOperation( RqlFilter filter );

   public abstract T visitComparison( RqlFilter filter );

   public abstract T visitFieldDirection( RqlFieldDirection model );
}

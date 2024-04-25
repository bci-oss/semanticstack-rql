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
import java.util.Optional;

import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.model.v1.RqlModelVisitor;
import com.boschsemanticstack.rql.model.v1.RqlOptions;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.model.v1.RqlSelect;

public class RqlQueryModelImpl implements RqlQueryModel {
   private final RqlSelect select;
   private final RqlFilter filter;
   private final RqlOptions options;

   public RqlQueryModelImpl( final RqlSelect select, final RqlFilter filter, final RqlOptions options ) {
      this.select = null == select ? new RqlSelectImpl( Collections.emptyList() ) : select;
      this.filter = filter;
      this.options = null == options ? RqlOptionsImpl.emptyOptions() : options;
   }

   @Override
   public RqlSelect getSelect() {
      return select;
   }

   @Override
   public Optional<RqlFilter> getFilter() {
      return Optional.ofNullable( filter );
   }

   @Override
   public RqlOptions getOptions() {
      return options;
   }

   @Override
   public <T> T accept( final RqlModelVisitor<? extends T> visitor ) {
      return visitor.visitModel( this );
   }
}

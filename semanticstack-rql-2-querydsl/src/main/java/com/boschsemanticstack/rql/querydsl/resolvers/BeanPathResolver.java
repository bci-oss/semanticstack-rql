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

package com.boschsemanticstack.rql.querydsl.resolvers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Queue;

import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BeanPath;
import com.querydsl.core.types.dsl.SimpleExpression;

/**
 * Recursive {@link Predicate} resolver that simply continues the resolution process when the property path enters a
 * nested metamodel class.
 */
@SuppressWarnings( { "java:S3740" } )
// java:S3740 parameterized types - thy are not known for the expressions handled here so they cannot be given explicitely
public class BeanPathResolver extends RecursiveResolver<BeanPath<?>> {
   public BeanPathResolver( final AbstractPathPredicateResolver parentResolver ) {
      super( parentResolver );
   }
 
   @Override
   public Predicate resolve( final SimpleExpression root, final Field field, final Queue<String> pathElements, final RqlFilter filter )
         throws ReflectiveOperationException {
      return super.resolve( (SimpleExpression) field.get( root ), field, pathElements, filter );
   }

   @Override
   public Predicate resolveMethod( final SimpleExpression root, final Method method, final Queue<String> pathElements,
         final RqlFilter filter )
         throws ReflectiveOperationException {
      return super.resolveMethod( (SimpleExpression) method.invoke( root ), method, pathElements, filter );
   }
}

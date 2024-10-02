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
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.SimpleExpression;

/**
 * Interface for any class that can resolve a property path to a predicate type.
 *
 * @param <T> the type of property path expression this class can handle
 * @param <P> the type of the Predicate, which is the WHERE clause into the SQL statement
 * @see AbstractPathPredicateResolver
 */
@SuppressWarnings( { "java:S3740" } )
// java:S3740 parameterized types - thy are not known for the expressions handled here so they cannot be given explicitely
public interface PathPredicateResolver<T extends Expression<?>, P> {
   /**
    * @return the type of property path this class handles. This type is automatically captured when using the {@link
    * AbstractPathPredicateResolver}, so there should never be the need to implement this method yourself.
    */
   Class<T> getHandledPathType();
 
   /**
    * Performs the actual resolution to a Predicate type.
    *
    * @param root the current root of the evaluation
    * @param field the respective field of the current path element
    * @param pathElements all remaining path elements. May be empty, but never {@code null}.
    * @param filter the filter for which the current resolution is performed
    * @return the resolved predicate
    * @throws ReflectiveOperationException if any of the fields referenced through the property chain given in
    * {@code pathElements} cannot be accessed
    */
   P resolve( SimpleExpression root, Field field, Queue<String> pathElements, RqlFilter filter )
         throws ReflectiveOperationException;

   /**
    * Performs the actual resolution to a Predicate type.
    *
    * @param root the current root of the evaluation
    * @param method the respective method of the current path element
    * @param pathElements all remaining path elements. May be empty, but never {@code null}.
    * @param filter the filter for which the current resolution is performed
    * @return the resolved predicate
    * @throws ReflectiveOperationException if any of the method referenced through the property chain given in
    * {@code pathElements} cannot be accessed
    */
   P resolveMethod( SimpleExpression root, Method method, Queue<String> pathElements, RqlFilter filter )
         throws ReflectiveOperationException;

   /**
    * Post process generated query predicate.
    *
    * @param predicate the queries predicate
    * @return the processed predicate
    */
   default P postProcess( final P predicate ) {
      return predicate;
   }
}

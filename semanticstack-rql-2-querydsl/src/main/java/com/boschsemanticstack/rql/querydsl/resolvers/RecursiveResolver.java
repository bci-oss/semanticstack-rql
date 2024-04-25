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

package com.boschsemanticstack.rql.querydsl.resolvers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Queue;

import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.querydsl.QueryModelToQueryDSL;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.SimpleExpression;

/**
 * A predicate resolver that can perform an indirect recursive call to a parent (usually {@link
 * QueryModelToQueryDSL}). This is needed to break up the direct recursion of the
 * resolution process so that also recursive {@link PathPredicateResolver}s can be extracted into an own class.
 *
 * @param <T> the type of property path expression this class can handle
 * @see BeanPathResolver
 * @see JpaCollectionPathResolver
 */
@SuppressWarnings( { "java:S3740" } )
// java:S3740 parameterized types - thy are not known for the expressions handled here so they cannot be given explicitely
public class RecursiveResolver<T extends Expression<?>> extends AbstractPathPredicateResolver<T, Predicate> {
   private final AbstractPathPredicateResolver parentResolver;

   private final MethodHandle delegatingResolve;

   /**
    * Instantiates a {@link RecursiveResolver} that will delegate the actual resolution back to the {@link
    * PathPredicateResolver#resolve(SimpleExpression, Field, Queue, RqlFilter)} method of the given parent resolver.
    *
    * @param parentResolver the parent resolver to delegate to when performing the actual resolution
    * @throws IllegalStateException if the delegating {@link MethodHandle} that handles the indirection can not be
    * created. This can only be the case if the name does not match anymore or the method signature was changed
    * without changing this constructor.
    */
   public RecursiveResolver( final AbstractPathPredicateResolver parentResolver ) {
      this.parentResolver = parentResolver;
      try {
         delegatingResolve = MethodHandles.publicLookup().findVirtual( parentResolver.getClass(),
               "resolve", MethodType.methodType( Predicate.class, SimpleExpression.class, Field.class, Queue.class,
                     RqlFilter.class ) );
      } catch ( final NoSuchMethodException | IllegalAccessException e ) {
         throw new IllegalStateException( e );
      }
   }

   @Override
   @SuppressWarnings( "squid:S1181" )
   // do not catch Error/Throwable: API of MethodHandle does not allow anything else - mitigation is to rethrow Error and RuntimeEx
   public Predicate resolve( final SimpleExpression root, final Field field, final Queue<String> pathElements, final RqlFilter filter )
         throws ReflectiveOperationException {
      try {
         return (Predicate) delegatingResolve.invokeWithArguments( parentResolver, root, field, pathElements, filter );
      } catch ( final Error | RuntimeException | IllegalAccessException e ) {
         throw e; // do not try to handle Errors, RuntimeExceptions or the specific IllegalAccessException!
      } catch ( final Throwable e ) {
         throw new InvocationTargetException( e );
      }
   }

   @Override
   @SuppressWarnings( "squid:S1181" )
   // do not catch Error/Throwable: API of MethodHandle does not allow anything else - mitigation is to rethrow Error and RuntimeEx
   public Predicate resolveMethod( final SimpleExpression root, final Method method, final Queue<String> pathElements, final RqlFilter filter )
         throws ReflectiveOperationException {
      try {
         return (Predicate) delegatingResolve.invokeWithArguments( parentResolver, root, method, pathElements, filter );
      } catch ( final Error | RuntimeException | IllegalAccessException e ) {
         throw e; // do not try to handle Errors, RuntimeExceptions or the specific IllegalAccessException!
      } catch ( final Throwable e ) {
         throw new InvocationTargetException( e );
      }
   }
}

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

package com.boschsemanticstack.rql.querydsl.resolvers.converters;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.boschsemanticstack.rql.querydsl.resolvers.SimpleValueResolver;

/**
 * Container for all registered type converters.
 *
 * @see SimpleValueResolver
 */
public class TypeConverters {
   private static final UnaryOperator<Object> IDENTITY = UnaryOperator.identity();

   private final Map<Class<?>, Function<Object, ?>> converters = new HashMap<>();

   /**
    * Registers a converter for a target class.
    *
    * @param targetClass the class the converter can convert values to
    * @param converter the converter
    */
   public <F, T> void register( final Class<T> targetClass, final Function<F, T> converter ) {
      converters.put( targetClass, (Function<Object, ?>) converter );
   }

   /**
    * Tries to convert a value to the given target class.
    *
    * This method never fails. If no converter can be found to perform the conversion, the given input value is
    * returned as-is.
    *
    * @param targetClass the class the value should be converted to
    * @param value the value to convert
    * @return the converted value or the original value if no converter could be found
    */
   public Object convertTo( final Class<?> targetClass, final Object value ) {
      if ( null == value || targetClass.isAssignableFrom( value.getClass() ) ) {
         return value; // no conversion necessary
      }
      return getConverter( targetClass ).apply( value );
   }

   private Function<Object, Object> getConverter( final Class<?> clazz ) {
      return (Function<Object, Object>) converters.getOrDefault( clazz, IDENTITY );
   }
}

/*
 * Copyright (c) 2024 Robert Bosch Manufacturing Solutions GmbH, Germany. All rights reserved.
 */
package com.boschsemanticstack.rql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.boschsemanticstack.rql.model.v1.RqlFilter;

/**
 * Annotation to restrict {@link String} type search to an element with a given regex.
 * This check is only performed if the filter option is a {@link RqlFilter.Operator.LIKE} or {@link RqlFilter.Operator.LIKE_IGNORE_CASE}.
 * <p>
 * e.g.
 * filter=like(attribute,"some-*Attribute*")
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.METHOD } )
public @interface RqlPattern {
   /**
    * @return the regex of allowed pattern.
    */
   String regex();

   /**
    * @return a customize error message
    */
   String errorMessage() default "";
}
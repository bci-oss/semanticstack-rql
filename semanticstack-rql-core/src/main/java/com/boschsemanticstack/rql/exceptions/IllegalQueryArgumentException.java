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

package com.boschsemanticstack.rql.exceptions;

/**
 * Abstract class for illegal query argument exceptions.
 */
public abstract class IllegalQueryArgumentException extends IllegalArgumentException {

   /**
    * @param message
    */
   protected IllegalQueryArgumentException( final String message ) {
      super( message );
   }
 
   protected IllegalQueryArgumentException( final String message, final Throwable cause ) {
      super( message, cause );
   }
}

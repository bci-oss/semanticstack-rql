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

import java.util.List;

public interface RqlOrder extends RqlModelNode {

   /**
    * @return a not-null, not-empty collection
    */
   List<RqlFieldDirection> fieldDirections();

   default boolean isEmpty() {
      return fieldDirections().isEmpty();
   }
}

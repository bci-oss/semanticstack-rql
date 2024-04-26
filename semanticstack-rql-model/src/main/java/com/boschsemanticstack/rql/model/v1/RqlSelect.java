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

public interface RqlSelect extends RqlModelNode {

   List<String> attributes();

   /**
    * @param attribute the given attribute name
    * @return true if the attribute is contained in the list of attributes
    * defined by the select() part of the RQL expression
    */
   boolean contains( String attribute );

   /**
    * @return true if no attributes are provided
    */
   boolean isEmpty();

   /**
    * @param text
    * @return true if one of the attributes defined by the select() part of the RQL expression starts with text
    */
   boolean hasAttributeStartingWith( String text );

   /**
    * Returns true if the attribute is contained in the list of attributes defined by the select() part of the RQL
    * query.
    *
    * @param attributeName the given attribute name.
    * @return true if selected else otherwise.
    */
   boolean isExplicitSelected( String attributeName );

   /**
    * Returns true if the attribute defined by the select() part of the RQL query starts with the given
    * attributeNamePrefix. This is useful e.g. to select all attributes of a flattened json object for example if you
    * want select the customer attributed use this method for evaluating select(customer):
    *
    * <pre>
    *  {
    *    ...
    *    "customer":{
    *       "surname"  : ...,
    *       "firstname": ...
    *    }
    * }
    * </pre>
    *
    * @param attributeNamePrefix the given attribute name prefix
    * @return true if the attribute starts with the given attribute name prefix.
    */
   boolean isImplicitSelected( String attributeNamePrefix );
}

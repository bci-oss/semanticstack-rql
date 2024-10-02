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

package com.boschsemanticstack.rql.parser.v1;

import java.util.ArrayList;
import java.util.List;

import com.boschsemanticstack.rql.exceptions.ParseException;
import com.boschsemanticstack.rql.parser.v1.internal.InternalRqlParser;

import org.antlr.v4.runtime.ParserRuleContext;

class ParseResult {
   private final List<ParseException> errors = new ArrayList<>();
   private ParserRuleContext parseTree;
   private InternalRqlParser parser;

   public ParserRuleContext getParseTree() {
      return parseTree;
   }

   public void setParseTree( final ParserRuleContext parseTree ) {
      this.parseTree = parseTree;
   }

   public InternalRqlParser getParser() {
      return parser;
   }
 
   public void setParser( final InternalRqlParser parser ) {
      this.parser = parser;
   }

   public List<ParseException> getErrors() {
      return errors;
   }
}

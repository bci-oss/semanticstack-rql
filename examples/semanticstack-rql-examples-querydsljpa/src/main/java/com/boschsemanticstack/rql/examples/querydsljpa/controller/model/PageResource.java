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

package com.boschsemanticstack.rql.examples.querydsljpa.controller.model;

import java.util.LinkedList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Just a lazy, simple page-implementation for demonstration purposes.
 */
@Data
@JsonInclude( Include.NON_NULL )
public class PageResource<T> {
   @JsonProperty
   private long totalCount;
   @JsonProperty
   private int count;
   @JsonProperty
   private int page;
   @JsonProperty
   private int pageSize;
   @JsonProperty
   private List<T> elements = new LinkedList<>();
   @JsonProperty
   private List<Link> links = new LinkedList<>();

   public PageResource( final Page<T> page ) {
      totalCount = page.getTotalElements();
      elements.addAll( page.getContent() );
      count = page.getSize();
      this.page = page.getNumber();
      pageSize = page.getPageable().getPageSize();
   }

   public static <T> PageResource<T> of( final Page<T> page ) {
      return new PageResource<>( page );
   }

   public PageResource<T> withSelfLink( final Class<?> class1 ) {
      links.add( WebMvcLinkBuilder.linkTo( class1 ).withRel( "self" ) );
      return this;
   }

   public PageResource<T> withLink( final Link link ) {
      links.add( link );
      return this;
   }
}

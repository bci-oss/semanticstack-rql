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

package com.boschsemanticstack.rql.examples.querydsljpa.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table( name = "BURGER" )
public class Burger {
   @Id
   private long id;

   @Column( name = "NAME" )
   private String name;

   @Column( name = "DRESSING" )
   private Dressing dressing;

   public Burger() {
   }

   public Burger( final long id, final String name, final Dressing dressing ) {
      this.id = id;
      this.name = name;
      this.dressing = dressing;
   }

   public long getId() {
      return id;
   }

   public void setId( final long id ) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName( final String name ) {
      this.name = name;
   }

   public Dressing getDressing() {
      return dressing;
   }

   public void setDressing( final Dressing dressing ) {
      this.dressing = dressing;
   }

   @Override
   public boolean equals( final Object o ) {
      if ( this == o ) {
         return true;
      }
      if ( o == null || getClass() != o.getClass() ) {
         return false;
      }
      final Burger burger = (Burger) o;
      return id == burger.id && Objects.equals( name, burger.name ) && dressing == burger.dressing;
   }

   @Override
   public int hashCode() {
      return Objects.hash( id, name, dressing );
   }

   @Override
   public String toString() {
      return "Burger(id=" + id + ", name=" + name + ", dressing=" + dressing + ")";
   }

   public static BurgerBuilder builder() {
      return new BurgerBuilder();
   }

   public static class BurgerBuilder {
      private long id;
      private String name;
      private Dressing dressing;

      public BurgerBuilder id( final long id ) {
         this.id = id;
         return this;
      }

      public BurgerBuilder name( final String name ) {
         this.name = name;
         return this;
      }

      public BurgerBuilder dressing( final Dressing dressing ) {
         this.dressing = dressing;
         return this;
      }

      public Burger build() {
         return new Burger( id, name, dressing );
      }
   }
}

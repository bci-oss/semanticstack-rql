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

package com.boschsemanticstack.rql.examples.querydsljpa.demodata;

import com.boschsemanticstack.rql.examples.querydsljpa.model.Burger;
import com.boschsemanticstack.rql.examples.querydsljpa.model.Dressing;
import com.boschsemanticstack.rql.examples.querydsljpa.repository.BurgerRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataGenerator implements ApplicationListener<ApplicationReadyEvent> {

   private final BurgerRepository burgerRepo;

   public DataGenerator( final BurgerRepository burgerRepo ) {
      this.burgerRepo = burgerRepo;
   }

   @Override
   public void onApplicationEvent( final ApplicationReadyEvent event ) {
      if ( burgerRepo.count() > 0L ) {
         return;
      }

      burgerRepo.save( Burger.builder().id( 1 ).name( "Mac Rql" ).dressing( Dressing.CHILI_CHEESE_SAUCE ).build() );
      burgerRepo.save( Burger.builder().id( 2 ).name( "Big Mac Rql" ).dressing( Dressing.TOMATO_SAUCE ).build() );
      burgerRepo.save( Burger.builder().id( 3 ).name( "Big Rql" ).dressing( Dressing.CHILI_CHEESE_SAUCE ).build() );
      burgerRepo.save( Burger.builder().id( 4 ).name( "Bacon Rql Mac" ).dressing( Dressing.MUSTARD ).build() );
   }
}

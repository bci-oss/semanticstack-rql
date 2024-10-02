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

package com.boschsemanticstack.rql.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.boschsemanticstack.rql.exceptions.ParseException;
import com.boschsemanticstack.rql.model.v1.RqlFieldDirection;
import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.model.v1.impl.RqlFieldDirectionImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlSliceImpl;
import com.boschsemanticstack.rql.parser.v1.RqlParser;
import org.junit.jupiter.api.Test;

class RqlRepresentationTest {

  @Test
  void queryWithSlashAttributeShouldBeNotParseable() {
    final String expression = " option=sort(+att1,-att2)" +
        "&filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))" +
        "&select=att1,att2,att3/subAtt4"
        + "&option=limit(5,500)";

    assertThatThrownBy(() -> RqlParser.from(expression)).isInstanceOf(ParseException.class)
        .hasMessageContaining("token recognition error at: '/'");
  }

  @Test
  void queryWithRandomizedOrderShouldBeParsable() {
    final String expression = " option=sort(+att1,-att2)" +
        "&filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))" +
        "&select=att1,att2,att3.subAtt4"
        + "&option=limit(5,500)";

    final RqlQueryModel query = RqlParser.from(expression);

    assertThat(query.getOptions().getOrder().fieldDirections())
        .containsExactly(
            new RqlFieldDirectionImpl("att1", RqlFieldDirection.Direction.ASCENDING),
            new RqlFieldDirectionImpl("att2", RqlFieldDirection.Direction.DESCENDING)
        );
    assertThat(query.getOptions().getSlice().get().limit()).isEqualTo(500);
    assertThat(query.getOptions().getSlice().get().offset()).isEqualTo(5);
    assertThat(query.getSelect().attributes())
        .containsExactly("att1", "att2", "att3.subAtt4");

    assertThat(query.getFilter().get().getChildren()).extracting(RqlFilter::getOperator,
            RqlFilter::getAttribute, RqlFilter::getValue)
        .containsExactly(
            tuple(RqlFilter.Operator.EQ, "att2", "theSame"),
            tuple(RqlFilter.Operator.LT, "att1", 5),
            tuple(RqlFilter.Operator.GT, "att1", 42)

        );
  }

  @Test
  void shouldParseNewSyntaxWithSelectEqualsWithoutParens() {
    // GIVEN
    final String expression = "select=id,name&filter=eq(id,\"4711\")&option=sort(+name,-description),limit(5,10)";

    // WHEN
    final RqlQueryModel model = RqlParser.from(expression);

    // THEN
    assertThat(model.getSelect().attributes()).containsExactly("id", "name");

    assertThat(model.getFilter().get().getOperator()).isEqualTo(RqlFilter.Operator.EQ);
    assertThat(model.getFilter().get().getValues()).containsExactly("4711");

    assertThat(model.getOptions().getOrder().fieldDirections())
        .containsExactly(
            new RqlFieldDirectionImpl("name", RqlFieldDirection.Direction.ASCENDING),
            new RqlFieldDirectionImpl("description", RqlFieldDirection.Direction.DESCENDING)
        );
    assertThat(model.getOptions().getSlice()).contains(new RqlSliceImpl(5, 10));
  }
}

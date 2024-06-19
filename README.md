# Semantic Stack Resource Query Language (RQL)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=bci-oss_semanticstack-rql&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=bci-oss_semanticstack-rql)
[![Maven Central](https://img.shields.io/maven-central/v/com.boschsemanticstack/semanticstack-rql-parent)](https://central.sonatype.com/artifact/com.boschsemanticstack/semanticstack-rql-parent)

## Table of Contents

- [Semantic Stack Resource Query Language (RQL)](#semantic-stack-resource-query-language-rql)
  - [Table of Contents](#table-of-contents)
  - [Introduction](#introduction)
  - [Getting help](#getting-help)
  - [Build and contribute](#build-and-contribute)
  - [Semantic Stack RQL Components](#semantic-stack-rql-components)
    - [`semanticstack-rql-parser`](#semanticstack-rql-parser)
    - [`semanticstack-rql-model`](#semanticstack-rql-model)
    - [`semanticstack-rql-core`](#semanticstack-rql-core)
    - [`semanticstack-rql-2-querydsl`](#semanticstack-rql-2-querydsl)
    - [`examples`](#examples)
    - [`documentation`](#documentation)
  - [License](#license)

## Introduction

Fundamentally, RQL (Resource Query Language) serves as a query language tailored for use in REST, specifically within URIs featuring data structures resembling
objects. This repository, in turn, provides a Java parser implemented using antlr4 to facilitate the integration of RQL on client and server side.

The main parts of the RQL are: select, filter and option.

```text
select=id&filter=eq(name, "blue")&option=limit({start},{count}),sort(-id)
```

The select operator trims each response down to the set of attributes defined in the arguments of the query.
Filtering reduces the amount of retrieved entities by given criteria, paging returns a subset of the results.
With sorting, the retrieved results are returned in a predefined order.

This repository contains a detailed developer documentation written in AsciiDoc.
The source files (AsciiDoc) are located [here](documentation/modules/rql) and are built using
[Antora](https://antora.org/) which generates the documentation as HTML files. A site generated using Antora is self-contained and can be viewed entirely
offline via a web browser without setting up a web server.

## Getting help

Are you having trouble with Semantic Stack RQL? We want to help!

* Check the [documentation](https://docs.bosch-semantic-stack.com/rql/index.html)
* Ask a question the [GitHub discussions](https://github.com/bci-oss/semanticstack-rql/discussions).
* Having issues with the Semantic Stack RQL? Open a [GitHub issue](https://github.com/bci-oss/semanticstack-rql/issues)

## Build and contribute

The top level elements of the SDK structure are all carried out as Maven multimodule projects.
Building the project requires a Java 17-compatible JDK.

To build the project, run the following command:

```bash
mvn clean install
```

We are always looking forward to your contributions. For more details on how to contribute just take
a look at the [contribution guidelines](CONTRIBUTING.md). Please create an issue first before
opening a pull request.

## Semantic Stack RQL Components

### `semanticstack-rql-parser`

This Maven module contains the antlr4 grammar as well as the parser to create the RqlQueryModel from a string or vice versa.

*antlr4 Grammar*: This refers to the definition of the language structure using the antlr4 parser. It allows for the precise description of the syntax and
semantics of the RQL language.

*Parser*: The parser is the central tool that uses antlr4 code to analyze RQL queries and convert them into an RqlQueryModel. Therefore, this module enables the
interpretation of RQL queries in string format and transforms them into an internal model (RqlQueryModel) that can be further processed.

*RqlQueryModel*: This represents the result of the parsing operation (see `semanticstack-rql-model`). It is an internal representation of the RQL query,
presented
in a structure easily understandable for the application.

### `semanticstack-rql-model`

This module provides a model (RqlQueryModel) for the RQL language. It contains classes for the different RQL operators and parts.
The plain model consisting only of interfaces and an implementation where each part is immutable. +

### `semanticstack-rql-core`

This module provides a set of core classes that can be thrown by the used by all other projects.

### `semanticstack-rql-2-querydsl`

This module provides a converter and parser from RQL to QueryDSL to generated criteria queries with JPA.

### `examples`

This folder contains examples of how to use the RQL parser and model.

### `documentation`

This folder contains a detailed documentation written in AsciiDoc and generated with Antora.

## License

SPDX-License-Identifier: MPL-2.0

This program and the accompanying materials are made available under the terms of the
[Mozilla Public License, v. 2.0](LICENSE).

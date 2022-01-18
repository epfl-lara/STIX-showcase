# From Verified Scala to STIX File System Embedded Code using Stainless

This repository contains a small public fragment of the case
study we used to evaluate the use of Stainless to construct
high-assurance embedded software.

There are two aspects that this case study evaluates:
  * generating C code from Stainless (genc)
  * verifying such lower-level imperative code

## Prerequisites

Stainless: https://epfl-lara.github.io/stainless/installation.html

It should run with versions such as 0.9.1.

## Invoke formal verification

To run the verification task:\
[./verify](verify)

## Generate C-code

To generate C-code from Scala using genc functionality of stainless: \
[./compile](compile)

The resulting C files is also committed to the repository and 
can be found in the [gen](gen/) folder.

To get some flavor of properties proven, consider the function
[setBlockAsFree](File.scala#L42) in the Scala source code [File.scala](File.scala). 
Among the interesting properties that we prove is the invariant [blockCountInvariant](File.scala#L130), which
is defined in [BlockCountInvariant.scala](BlockCountInvariant.scala#L55) in terms of executable recursive Scala function [countStatusFrom](BlockCountInvariant.scala#L14).

The corresponding generated code for the entire fragment is in [gen/esover.c](gen/esover.c#L142) file with the C function named the same. In the context of the full case study, the generate code, along with certain bridge functions, compiles as a drop-in replacement for parts of the existing C implementation of the file system and exhibits similar performance as the original code (in some cases running faster, in some cases slower due to decisions taken by the C compiler), and with very similar code sizes.

## Other Links
* Stainless Website: https://stainless.epfl.ch
* EPFL-LARA Website: https://lara.epfl.ch/w/
* Ateleris GmbH Website: https://www.ateleris.ch/

## License
This repository is released under the Apache 2.0 license. 
See the [LICENSE](https://github.com/epfl-lara/STIX-showcase/blob/master/LICENSE) file for more information.

# From Verified Scala to STIX File System Embedded Code using Stainless
Case-study driven adaptation of Stainless from a verifier 
originally focused on functional programs to a system that can be used 
to develop high-assurance embedded software. A public benchmark that 
can be used to validate other such approaches in the future.

## Prerequisites
Stainless: https://epfl-lara.github.io/stainless/installation.html

## run verification
To run the verification:\
``./verify``

## generate C-code
To generate C-code from Scala:\
``./compile``

The resulting C files can be found in the [gen](https://github.com/epfl-lara/STIX-showcase/tree/master/gen) folder.

## Other Links
* Stainless Website: https://stainless.epfl.ch
* EPFL-LARA Website: https://lara.epfl.ch/w/
* Ateleris GmbH Website: https://www.ateleris.ch/

## License
This repository is released under the Apache 2.0 license. 
See the [LICENSE](https://github.com/epfl-lara/STIX-showcase/blob/master/LICENSE) file for more information.

SWORD 2.0 Server
================

This server library is an implementation of the SWORD 2.0 standard defined [here](https://sword.cottagelabs.com/previous-versions-of-sword/sword-v2/sword-v2-specifications/)
([SWORD 2.0 profile](http://swordapp.github.io/SWORDv2-Profile/SWORDProfile.html))

The variant hosted in this repository is being used as the library of choice to implement the compliant interface
for [Dataverse](https://dataverse.org). It also gets pushed to [Maven Central](https://mvnrepository.com/artifact/io.gdcc/sword2-server).

## Important changes

This library does no longer support [RFC2387 type uploads (multipart/related)](dx.doi.org/10.17487/RFC2387)!
It will present users an error message, telling them to use Atom instead.

Support for this type of uploads was broken for a long time and did not make it into SWORD v3.
[It was tagged for removal for a SWORD v2.1 spec](http://www.mail-archive.com/sword-app-tech@lists.sourceforge.net/msg00327.html),
which never happened.

## History of this library

1. This library has been develop by @richard-jonnes and @bmckinney at https://github.com/swordapp/JavaServer2.0 first.
2. As development has been staled, DANS-KNAW took as part of project EASY at https://github.com/DANS-KNAW/easy-sword2-lib
3. MyCoRe project forked and maintain their own version with interesting changes. https://github.com/MyCoRe-Org/easy-sword2-lib
4. IQSS needed a fix in this library, so they currently have their own fork at https://github.com/IQSS/swordv2-java-server-library
   This project aims to replace that fork and provide a version with updated dependencies. It has been replaced with
   releases from this repo in the meantime.

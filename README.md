RuTA Testing Standalone
=======================

A standalone version of the RuTA Testing framework.

Licensed under the APL-2.0.

See http://svn.apache.org/repos/asf/uima/ruta/trunk/ruta-ep-addons/src/main/java/org/apache/uima/ruta/testing/ for the original version.

After building execute org.ie4opendata.ruta_testing_standalone.Evaluate without parameters to see the list of accepted parameters.

Please note: your type system has to include the types in src/main/resources/EvalTypeSystem.xml to be able to use this code.

Command-line script
-------------------

mvn package appassembler:assemble

./target/appassembler/bin/ruta-evaluate

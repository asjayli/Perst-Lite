javac -source 1.4 -target 1.4 -g org/garret/perst/*.java org/garret/perst/fulltext/*.java org/garret/perst/impl/*.java org/garret/perst/impl/sun14/*.java
jar cvf ../lib/perst14.jar org/garret/perst/*.class org/garret/perst/fulltext/*.class org/garret/perst/impl/*.class org/garret/perst/impl/sun14/*.class

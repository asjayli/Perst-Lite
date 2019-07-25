if not defined WTK_HOME set WTK_HOME=c:\WTK2.5.2
set J2MEAPI_PATH=%WTK_HOME%\lib\cldcapi11.jar;%WTK_HOME%\lib\midpapi20.jar;%WTK_HOME%\lib\cldc_1.1.jar;%WTK_HOME%\lib\midp_2.0.jar
mkdir classes
javac -target 1.2 -source 1.2 -d classes -bootclasspath %J2MEAPI_PATH% -sourcepath .;rms;fulltext;weak com\jcraft\jzlib\*.java org\garret\perst\*.java org\garret\perst\impl\*.java rms\org\garret\perst\*.java rms\org\garret\perst\impl\*.java fulltext\org\garret\perst\fulltext\*.java fulltext\org\garret\perst\impl\*.java weak\org\garret\perst\impl\*.java
cd classes
jar cvf ../../lib/perst-rms.jar .
cd ..
rd /s/q classes
Test'N'Groove

Test'N'Groove (read as in Test and Groove) is the integration of Groovy (http://groovy.codehaus.org)
and TestNG (http://testng.org). This enables writting TestNG test directly in Groovy scripting language.

Note: starting with [revision 5068](https://code.google.com/p/testngroove/source/detail?r=5068) (2007-02-15), Groovy has full support for annotation usage.
Considering this, an integration with TestNG was imminent, so here it is.

## INSTALLATION ##
If you got your Test'N'Groove binary distribution all you need to do is
unarchieve it on your Groovy install directory.
The installation will create batch files in the bin folder: groovyng and the necessary
jars in the lib dir.

## BUILD ##
Use Ant to build the distribution (the default Ant target). The only requirement is to
create a local.build.properties containing the property groovy.jar pointing to a valid
groovy jar.

## RUNNING ##

Running TestNG enabled Groovy scripts mean only passing them to the groovyng:

groovyt myscript.groovy

This module supports all TestNG command line options, the only requirement being that any options must be
provided prior to the scripts to be run:

groovyt [OPTIONS](OPTIONS.md) scripts...

For detailed documentation please consult TestNG site: http://testng.org


We also include an Ant task. Here is an example:

> 

&lt;target name="test"&gt;


> > 

&lt;taskdef resource="testngroovetasks" classpathref="CPREF" /&gt;




> 

&lt;groovyt haltonfailure="true"&gt;


> > 

&lt;scriptfileset dir="DIR"  includes="\*.groovy" /&gt;


> > <classpath ... />

> 

&lt;/groovyt&gt;


> 

&lt;/target&gt;



where CPREF must be a path including the TestNG jar and Test'N'Groove jar.

## WARNING ##

The current Groovy build has dependencies against xerces and xml-apis jars. However, when
running on top of a JVM newer than 1.4, which is however required by Test'N'Groove,
these are already available in JDK, so please make sure you don't have them in your


<GROOVY\_INSTALL\_DIR>

/lib folder, otherwise you may see some ugly stacktraces :-).

Enjoy Test'N'Groove!
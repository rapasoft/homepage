# XJC and JDK 8
- Rapasoft
- 
- 2015/05/14
- Java XML XJC JDK8
- publish

Recently I have stumbled upon an error when trying to generate JAXB annotated classes using Jaxb2 Maven plugin and XJC goal:

            Failed to read schema document 'xjc.xsd', because 'file' access is not allowed due to restriction set by the accessExternalSchema property.
            
This is a permissions problem which can be fixed [by a simple VM argument](http://stackoverflow.com/questions/23011547/webservice-client-generation-error-with-jdk8).

I have found that best solution when using IntelliJ Idea is to setup Maven VM arguments (Settings >> Maven >> Runner - VM Arguments). Another solution when using command line is to set MAVEN_OPTS environment variable with the mentioned VM argument:

            MAVEN_OPTS=-Djavax.xml.accessExternalSchema=all
To build this project, use `mvn clean javadoc:javadoc package'. NetBeans
includes maven support natively. To use NetBeans for building, you can
refer to http://wiki.netbeans.org/MavenBestPractices. To summarize
the necessary steps:

1. Open project. NetBeans automatically detects if project is mavenized,
as indicated by a special icon.

2. Go to project properties > actions, and add custom. The 'Execute Goals'
should be `clean javadoc:javadoc package'. It will then become available
on the context menu for the project. Please refer to
http://stackoverflow.com/a/9459001 for pictures.

3. Run newly added execution to generate javadoc and resulting jar.

The rest of the documentation will be located in the file
target/site/apidocs/index.html, opened using any standard web browser.

To execute the program after building, use
`java -jar target/rounding-0.0.1-SNAPSHOT.jar'

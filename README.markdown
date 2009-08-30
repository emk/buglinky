buglinky
========

buglinky is a bot for use with Google Wave.  For now, it must be run on
on a Google App Engine instance.

For more information, see:

* <http://wave.google.com/>
* <http://code.google.com/apis/wave/extensions/robots/java-tutorial.html>

Customizing
-----------

At a minimum, you must change the following two pieces of information to
match your App Engine application name:

1. The <application>...</application> element in
   war/WEB-INF/appengine-web.xml.
2. The address in BugLinkyServlet.ME.  Failure to change this address
   will result you receiving tens of thousands of useless requests,
   because your bot will be responding to its own edits.

You will probably also want to take a look at the various information in
BugLinkyProfileServlet.

Missing Files
-------------

The following *.jar files have been omitted from buglinky.  You can get
these from any Google App Engine project:

    war/WEB-INF/lib/appengine-api-1.0-sdk-1.2.2.jar
    war/WEB-INF/lib/datanucleus-appengine-1.0.2.final.jar
    war/WEB-INF/lib/datanucleus-core-1.1.4-gae.jar
    war/WEB-INF/lib/datanucleus-jpa-1.1.4.jar
    war/WEB-INF/lib/geronimo-jpa_3.0_spec-1.1.1.jar
    war/WEB-INF/lib/geronimo-jta_1.1_spec-1.1.1.jar
    war/WEB-INF/lib/jdo2-api-2.3-ea.jar

The following *.jar files are specific to Wave, and can be downloaded from
<http://code.google.com/p/wave-robot-java-client/downloads/list>.

    war/WEB-INF/lib/json.jar
    war/WEB-INF/lib/jsonrpc.jar
    war/WEB-INF/lib/wave-robot-api-20090813.jar

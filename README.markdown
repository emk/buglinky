buglinky
========

buglinky is a bot for use with Google Wave.  For now, it must be run on on
a Google App Engine instance.

When run, buglinky turns text of the form "bug #12" and "issue #13" into
links to a bug tracker.  It runs in real time as you edit, and it tries to
generate as little network traffic as possible.

For more information, see:

* <http://wave.google.com/>
* <http://code.google.com/apis/wave/extensions/robots/java-tutorial.html>

If you want to see a much simpler (but still very useful) wave robot, see
<http://senikk.com/min-f%C3%B8rste-google-wave-robot>.  That one doesn't
run in real time as you edit, but on the other hand, it's only half a
screen of Python.

Customizing
-----------

At a minimum, you must change the following two pieces of information to
match your App Engine application name:

1. The &lt;application>...&lt;/application> element in
   war/WEB-INF/appengine-web.xml.
2. BugLinkyServlet.APP_NAME.  Failure to change this address will result
   you receiving tens of thousands of useless requests, because your bot
   will be responding to its own edits.

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

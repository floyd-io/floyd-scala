floyd-scala
===========

Floyd Scala Server.

This server is intended as a boilerplate to show a stream of events using the chunked streams supported by HTTP 1.1

Follow these steps to get started:

1. Git-clone this repository.

        $ git clone git://github.com/floyd-io/floyd-scala.git my-project

2. Change directory into your clone:

        $ cd my-project

3. Install MongoDB
 
        $ sudo apt-get install mongodb

4. Launch SBT:

        $ sbt

5. Compile everything and run all tests:

        > test

6. Start the application:

        > re-start

7. Browse to [http://localhost:8080](http://localhost:8080/)

8. Stop the application:

        > re-stop

9. Learn more at http://github.com/floyd-io/wiki , in http://www.spray.io/ and in http://akka.io/

10. Start hacking on src/main/scala/io/floyd/web/FloydServiceActor.scala

IDEA integration
================

To integrate with IntelliJ run the following command and import the project as an SBT project

       $ sbt gen-idea



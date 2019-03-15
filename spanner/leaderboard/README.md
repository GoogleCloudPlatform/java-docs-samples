# Java Cloud Spanner Sample Leaderboard Application

A leaderboard sample that uses the Cloud Spanner commit timestamp feature and demonstrates
how to call the [Google Cloud Spanner API](https://cloud.google.com/spanner/docs/)
using the [Google Cloud Client Library for Java](https://github.com/GoogleCloudPlatform/google-cloud-java).

This sample requires [Java](https://www.java.com/en/download/) and [Maven](http://maven.apache.org/) for building the application.

This sample includes extra directories `step5`, `step6`, and `step7` that contain partial versions of this sample application. These directories are intended to provide guidance as part of a separate Codelab walk-through where the application is built in the following stages
that correspond to the steps in Codelab:

* step5 - Create the sample database along with the tables Players and Scores.
* step6 - Populate the Players and Scores tables with sample data.
* step7 - Run sample queries including sorting the results by timestamp.

If you only want to run the complete sample refer to the application in the `complete` directory.


## Build and Run

1.  **Follow the set-up instructions in [the documentation](https://cloud.google.com/java/docs/setup).**

2.  Enable APIs for your project.
    [Click here](https://console.cloud.google.com/flows/enableapi?apiid=spanner.googleapis.com&showconfirmation=true)
    to visit Cloud Platform Console and enable the Google Cloud Spanner API.

3.  Create a Cloud Spanner instance via the Cloud Plaform Console's
    [Cloud Spanner section](http://console.cloud.google.com/spanner).

4.  In a terminal shell, change directory to the version of the application you want to run:
    ```
    cd complete
    ```

5.  Run the following Maven command to build the application:
    ```
    mvn install -DskipTests
    ```

6.  Change directory into the `target` directory where the application's jar file gets built to.
    ```
    cd target
    ```


7. Run the Spanner Leaderboard sample with `java -jar leaderboard.jar` to see a list of available commands:
    ```
    @shell:~/.../target$ java -jar leaderboard.jar
    
    Leaderboard 1.0.0
    Usage:
    java -jar leaderboard.jar <command> <instance_id> <database_id> [command_option]

    Examples:
    java -jar leaderboard.jar create my-instance example-db
        - Create a sample Cloud Spanner database along with sample tables in your project.

    java -jar leaderboard.jar insert my-instance example-db players
        - Insert 100 sample Player records into the database.

    java -jar leaderboard.jar insert my-instance example-db scores
        - Insert sample score data into Scores sample Cloud Spanner database table.

    java -jar leaderboard.jar query my-instance example-db
        - Query players with top ten scores of all time.

    java -jar leaderboard.jar query my-instance example-db 168
        - Query players with top ten scores within a timespan specified in hours.

    java -jar leaderboard.jar delete my-instance example-db
        - Delete sample Cloud Spanner database.
    ```

    ```
    $ java -jar leaderboard.jar create my-instance my-database
    Created database [projects/arc-nl/instances/my-instance/databases/my-database]
    ```
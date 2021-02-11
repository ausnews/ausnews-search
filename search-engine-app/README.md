# AUSNEWS search engine

This [vespa](https://vespa.ai) application is the search application and engine of the ausnews search project.

This application deploys the search engine and application to GKE and is used in combination with the crawler and feeder.

To develop locally, clone and checkout the `docker` branch as below:

**Executable example:**

<pre data-test="exec">
$ git clone https://github.com/ausnews/search-engine-app.git
$ git checkout docker
$ NEWS_APP=`pwd`/search-engine-app
$ cd $NEWS_APP &amp;&amp; mvn clean package
$ docker run --detach --name vespa --hostname vespa-container --privileged \
  --volume $NEWS_APP:/apps --publish 8080:8080 vespaengine/vespa
</pre>

**Wait for the configserver to start:**

<pre>
$ docker exec vespa bash -c 'curl -s --head http://localhost:19071/ApplicationStatus'
</pre>

**Deploy the application:**

<pre>
$ docker exec vespa bash -c '/opt/vespa/bin/vespa-deploy prepare /apps/target/application.zip && \
    /opt/vespa/bin/vespa-deploy activate'
</pre>

**Wait for the application to start:**

<pre>
$ curl -s --head http://localhost:8080/ApplicationStatus
</pre>

**Create data feed:**

To fetch some content, clone the crawler, follow setup instructions and run the code in bin/abc-sample.sh. This will create a number of ABC news articles you can feed to the engine.

<pre>
$ git clone https://github.com/ausnews/search-engine-app.git
$ ./bin/abc-sample.sh
$ cp vespa.json $NEWS_APP/
</pre>

**Feed data:**

<pre>
$ docker exec vespa bash -c 'java -jar /opt/vespa/lib/jars/vespa-http-client-jar-with-dependencies.jar \
    --file /apps/vespa.json --host localhost --port 8080'
</pre>

**Test the application:**

<pre>
$ curl -s 'http://localhost:8080/search/?query=what+is+dad+bod'
</pre>

**Browse the site:**

[http://localhost:8080/site](http://localhost:8080/site)


**Shutdown and remove the container:**

<pre data-test="after">
$ docker rm -f vespa
</pre>
![Build and deploy the search engine](https://github.com/ausnews/ausnews-search/workflows/Build%20and%20deploy%20the%20search%20engine/badge.svg)<br/>
![Build and deploy the Web API](https://github.com/ausnews/ausnews-search/workflows/Build%20and%20deploy%20the%20Web%20API/badge.svg)<br/>
![Build and deploy site on www.ausnews.org](https://github.com/ausnews/ausnews-search/workflows/Build%20and%20deploy%20site%20on%20www.ausnews.org/badge.svg)<br/>
![Build and deploy spiders](https://github.com/ausnews/ausnews-search/workflows/Build%20and%20deploy%20spiders/badge.svg) <br/>
[![Build and deploy augmenter](https://github.com/ausnews/ausnews-search/actions/workflows/augmenter.yml/badge.svg)](https://github.com/ausnews/ausnews-search/actions/workflows/augmenter.yml)

# AUSNews Search

As seen on https://www.ausnews.org

AUSNews search is a high performance, open-source search engine indexing some Australian news sites. It consists of 5 components:
- [Search engine app](./search-engine-app/). This is a [Vespa](https://vespa.ai) application. Great for scale, great for machine learned ranking. It supports live resizing of content nodes, live updates to search definitions and the application itself without any downtime.
- [Spiders](./crawler/). Like it says on the tin, these crawl news websites and feeds into the search engine for processing and storage. This component utilises [Scrapy](scrapy.org) (python), inherited RISJbot for news article scraping with custom spiders for Australian content
- Search [web-api](./web-api/). This is a (currently) simple user-facing API that interacts with and abstracts the vespa application backend. A simple micronaut/kotlin application.
- [Frontend site](./site/). A [Material Angular](https://material.angular.io) application for the browser UI.
- [Augmenter](./augmenter). Send extra signals about documents. Currently just twitter stats for each story. This is helpful for the "Top News" section.
- Infrastructure: Spiders, Search, Augmenter and the Web API are all deployed to GKE. Each folder has a subfolder deployment/ containing kubernetes yaml. Github workflows run per-directory to re-deploy each component when commits to master are made. The site can be run locally or on any static hosting service - on commit the page is pushed to the github pages repo.

## Search Engine

Search supports familiar user queries such as:
- `covid -china`: Searches for articles containing the word "covid" and not "china"
- `bylines:"Josh Taylor" google`: Searches for articles with "google" written by Josh Taylor.
- `source:abc bushfire`: Articles from abc.net.au containing "bushfire"
- `covid source:guardian firstpubtime:>1612211170` covid articles on The Guardian published after a certain time.

The web API constructs vespa queries (YQL) as used by the site, to make the above queries easier, but you can still type as above into the search field.

### Ranking & Search
Ranking is fairly simple and straightforward to begin with. It has extremely good stemming and other linguistic capabilities, and uses bm-25 algorithm combined with article "freshness" for ranking. This can be improved over time, but the aim here is to rank news articles on properties that cannot be simply gamed.

### Performance & Scale
AUSNews Search can handle 1400+ queries/second on just two vespa container and three content nodes, with a fairly limited dataset (~10,000 articles). Mean response rate from vespa <b>under 10ms</b>. This is with almost no tuning - performance can certainly be improved. Performance is unlikely to be affected with scale.

<img width="1024" alt="performance" src="https://user-images.githubusercontent.com/1582274/107164681-35cd6a00-6a04-11eb-8136-cd10d7c9684f.png">

Vespa supports live scaling and automatic rebalancing of content. This means that container or content nodes can simply be scaled up/down as required, without downtime or manual sharding changes. To scale vespa nodes, simply alter the numbers in bootstrap.sh, run it and run deploy.sh again. Here's a screenshot of document node rebalancing when adding two extra nodes:

<img width="1024" alt="Screen Shot 2021-02-04 at 12 53 12 pm" src="https://user-images.githubusercontent.com/1582274/106835149-eaa51580-66ea-11eb-804b-7dd9ac1cf5fd.png">

The Web API also runs independently and is designed to run on smaller nodes.

Sample `ab` output:
```
Concurrency Level:      50
Time taken for tests:   70.147 seconds
Complete requests:      100000
Failed requests:        0
Keep-Alive requests:    100000
Total transferred:      284740792 bytes
HTML transferred:       271440792 bytes
Requests per second:    1425.57 [#/sec] (mean)
Time per request:       35.074 [ms] (mean)
Time per request:       0.701 [ms] (mean, across all concurrent requests)
Transfer rate:          3964.05 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    0   0.1      0       6
Processing:     7   35  17.8     31     361
Waiting:        7   35  17.8     31     361
Total:          7   35  17.8     31     361

Percentage of the requests served within a certain time (ms)
  50%     31
  66%     38
  75%     43
  80%     46
  90%     56
  95%     67
  98%     81
  99%     92
 100%    361 (longest request)
 ```

## Architecture
The entire application is deployed to GKE and the set of applications and deployment can be reproduced.

```
                                +---------+
                                |         |
                                |         |
                                | Browser +--------------+
                                |         |              |
                                |         |              |
                                +-------+-+        +-----v--------+
                                        |          | Static site  |
                                      +-v------+   +--------------+
                                      |        |
                                      |Ingress |
                                      |        |
+----+GKE Cluster+--------------------+--+-----+-------------------------------------------+
|                                        |                                                 |
|                                        v                                                 |
|        ++Scalable Web APIs+--------------+                                               |
|        | +-------+  +-------+  +-------+ |                                               |
|        | |  Pod  |  |  Pod  |  |  Pod  | +-------+                                       |
|        | +-------+  +-------+  +-------+ |       |                                       |
|        |          Stateless              |       |                       +---------+     |
|        +---------------------------------+       |       +---------------+ Spiders |     |
|                                                  |       |               +---------+     |
|                                                  |       |Vespa Feed API                 |
|                                                  |       |                               |
|                                           +------v-------v----+                          |
|                                      +----+    k8s service    +-----+                    |
|                                      |    +---------+---------+     |                    |
|                                      |              |               |                    |
|                                      v              v               |                    |
|                             +-+Vespa Search Engine+-+---------------v-------+            |
|                             |                                               |            |
|                             |    ++Scalable search & document processing    |            |
|    +-------------+          |    | +---------+ +---------+ +---------+ +    |            |
|    |             |          |    | |   Pod   | |   Pod   | |   Pod   | |    |            |
|    |Monitoring & <---------------+ +---------+ +---------+ +---------+ |    |            |
|    |Alerting     |          |    +-------------------------------------+    |            |
|    |             |          |           |           |           |           |            |
|    +-------------+          |           v           v           v           |            |
|                             |    ++Scalable content with redundancy+---+    |            |
|                             |    | +---------+ +---------+ +---------+ |    |            |
|                             |    | |   Pod   | |   Pod   | |   Pod   | |    |            |
|                             |    | +---------+ +---------+ +---------+ |    |            |
|                             |    +-------------------------------------+    |            |
|                             |                                               |            |
|                             +-----------------------------------------------+            |
|                                                                                          |
+------------------------------------------------------------------------------------------+
```


## Crawler
A [Scrapy](scrapy.org) crawler. Currently searches
- guardian.com (Australian content, presumably because it's crawling from an Australian IP)
- abc.net.au
- smh.com.au
- news.com.au
- canberratimes.com.au
- theage.com.au
- thewest.com.au
- Straightforward to add more.

The crawler runs periodically and feeds content straight into the search engine for processing. When crawling, it currently feeds about 16 articles per second.

## Web API
A simple micronaut app written in Kotlin. An interface between clients and the search engine, as vespa applications are not designed to be exposed to the public internet. It abstracts the query language exposed and simplifies the frontend. Also allows this component to be scaled on its own.

## Local dev

Run vespa in local docker container and deploy the application to it (requires maven):
```
cd search-engine-app
docker run --detach --name vespa --hostname vespa-container --privileged --volume `pwd`:/apps --publish 8080:8080 vespaengine/vespa
mvn clean package
docker exec vespa bash -c '/opt/vespa/bin/vespa-deploy prepare /apps/target/application.zip && \
    /opt/vespa/bin/vespa-deploy activate'
```

Run the spiders (requires python3, pip) and feed to vespa
```
cd crawler
python3 -m venv aunews-spider
source aunews-spider/bin/activate
pip install -r requirements.txt
python spiders.py
```

Run the web api:
```
cd web-api
MICRONAUT_ENVIRONMENTS=dev ./gradlew clean run
```

Run the website (requires angular-cli installed):
```
cd site
ng serve
```

Note that you can do local dev of individual components against live k8s deployments, even the internal vespa nodes, by port-forwarding with kubectl.

## GKE Deployment

Make sure you have `gcloud` setup and are authenticated for your chosen account. Create your cluster:
```
cd search-engine-app
./scripts/create_cluster.sh
```

Build & Deploy the search engine:
```
./scripts/bootstrap.sh && ./scripts/deploy.sh
```

Build & Deploy the spiders:
```
cd ../crawler
docker build -t aunews-scrapy .
docker push aunews-scrapy
kubectl apply -f deployment/scrapy.yml
```

Build & Deploy the web API:
```
cd ../web-api
./gradlew jibDockerBuild
docker push ausnews-web-api
kubectl apply -f deployment/web-api.yml -f deployment/service.yml
```

Run the site locally, setting the external endpoint to the web api's service (either port-forward, or annotate the service to make it an external IP)
```
cd ../site
ng serve
```
Done! Visit localhost:4200

For prometheus/grafana monitoring (this includes vespa stats; query rates, document statistics etc), make sure you have `helm` installed and:
```
cd monitoring
helm install -f prometheus-stack.yml prometheus-grafana prometheus-community/kube-prometheus-stack
kubectl apply -f master-podmonitor.yml
```

import logging
import tweepy
import time
from vespa.application import Vespa
import hashlib
from urllib.request import urlopen
import requests
import re
from kubernetes import client, config
import base64

logger = logging.getLogger(__name__)

class TwitterInserter:
    
    def run(self):
        config.load_kube_config()
        v1 = client.CoreV1Api()
        twitter_secrets = v1.read_namespaced_secret(name='twitter-secrets', namespace='default').data
        api_key = base64.b64decode(twitter_secrets["api-key"])
        api_secret = base64.b64decode(twitter_secrets["api-secret"])
        self.vespa = Vespa(url = "http://localhost", port = 8080)
        auth = tweepy.AppAuthHandler(api_key, api_secret)
        self.api = tweepy.API(auth)
        updated = 0
        for userid in ['abcnews', 'GuardianAus', 'smh', 'iTnews_au', 'theage', 'canberratimes', 'zdnetaustralia', 'newscomauHQ', 'westaustralian', 'SBSNews', 'australian', 'crikey_news', '9NewsAUS']:
            try:
                for status in tweepy.Cursor(self.api.user_timeline, id=userid, include_entities=True, tweet_mode="extended").items(60):
                    if len(status.entities['urls']) == 0:
                        continue
                    url = status.entities['urls'][0]['expanded_url']
                    if (re.match(r'https?://zd.net', url) or url.startswith("https://bit.ly")):
                        url = urlopen(url).geturl()
                    url = url.split('?')[0]
                    if (url.startswith("https://twitter.com") or url.startswith("https://www.reddit.com")):
                        continue
                    article = self.get_article(url)
                    if article:
                        self.update_document(article, status)
                        updated += 1
#                    else:
#                        self.insert_document(url)
            except Exception as e:
                print("fuck {}".format(e))
                continue
        print("Completed run, updated {} tweets".format(updated))


#    def insert_document(self, url):
#        payload = {'url': url }
#        requests.get("http://localhost:8000/", params=payload)
#        print("Hit spider url for {}".format(url))

    def update_document(self, article, status):
        vespa_fields = { }
        vespa_fields['twitter_favourite_count'] = status.favorite_count
        vespa_fields['twitter_retweet_count'] = status.retweet_count
        vespa_fields['twitter_link'] = 'https://twitter.com/{}/status/{}'.format(status.user.screen_name, status.id)
        response = self.vespa.update_data(
            schema = "newsarticle",
            data_id = hashlib.sha256(article['fields']['url'].encode()).hexdigest(),
            fields = vespa_fields
        )
        #print("Updated {} with {} {}: {}".format(article['fields']['url'], status.favorite_count, status.retweet_count, response))

    def get_article(self, url):
        article_time = time.time() - 24 * 60 * 60
        body = {
            'yql': 'select url from sources newsarticle where userQuery();',
            'query': "url:{}".format(url),
            'hits': 1,
        }
        results = self.vespa.query(body=body)
        if len(results.hits) > 0:
            return results.hits[0]

while True:
    a = TwitterInserter()
    a.run()
    time.sleep(300)

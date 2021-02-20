import logging
import tweepy
import time
from vespa.application import Vespa
import hashlib
from urllib.request import urlopen

logger = logging.getLogger(__name__)

class TwitterInserter:
    api_key = "TWITTER_API_KEY"
    api_secret = "TWITTER_API_SECRET"
    
    def run(self):
        self.vespa = Vespa(url = "http://vespa-search", port = 8080)
        auth = tweepy.AppAuthHandler(self.api_key, self.api_secret)
        self.api = tweepy.API(auth)
        updated = 0
        for userid in ['abcnews', 'GuardianAus', 'smh', 'iTnews_au', 'theage', 'canberratimes', 'zdnetaustralia', 'newscomauHQ', 'westaustralian']:
            try:
                for status in tweepy.Cursor(self.api.user_timeline, id=userid, include_entities=True).items(60):
                    if len(status.entities['urls']) == 0:
                        continue
                    url = status.entities['urls'][0]['expanded_url']
                    url = url.split('?')[0]
                    if (url.startswith("https://twitter.com")):
                        continue
                    if (url.startswith("https://zd.net") or url.startswith("https://bit.ly")):
                        url = urlopen(url).geturl()
                    article = self.get_article(url)
                    if article:
                        self.update_document(article, status)
                        updated += 1
            except Exception as e:
                logger.error(e)
        print("Completed run, updated {} tweets".format(updated))


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

    def get_twitter_user(self, url):
        if url.startswith("https://www.abc.net.au"):
            return "abcnews"
        if url.startswith("https://www.theguardian.com/"):
            return "GuardianAus"
        if url.startswith("https://www.smh.com.au"):
            return "smh"
        if url.startswith("https://www.itnews.com.au"):
            return "iTnews_au"
        if url.startswith("https://www.theage.com.au"):
            return "theage"
        if url.startswith("https://www.canberratimes.com.au"):
            return "canberratimes"
        if url.startswith("https://www.zdnet.com"):
            return "zdnetaustralia"
        if url.startswith("https://www.news.com.au"):
            return "newscomauHQ"
        if url.startswith("https://thewest.com.au"):
            return "westaustralian"

while True:
    a = TwitterInserter()
    a.run()
    time.sleep(300)

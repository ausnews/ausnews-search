from itemadapter import ItemAdapter
import hashlib
import logging
import tweepy
from vespa.application import Vespa

logger = logging.getLogger(__name__)

class TwitterRelevance:
    api_key = "TWITTER_API_KEY"
    api_secret = "TWITTER_API_SECRET"
    bearer_token = "TWITTER_BEARER_TOKEN"
    access_token = "TWITTER_ACCESS_TOKEN"
    access_token_secret = "TWITTER_ACCESS_TOKEN_SECRET"

    def open_spider(self, spider):
        auth = tweepy.OAuthHandler(self.api_key, self.api_secret)
        auth.set_access_token(self.access_token, self.access_token_secret)
        self.api = tweepy.API(auth)
        self.vespa = Vespa(url = "http://vespa-search", port = 8080)

    def process_item(self, item, spider):
        try:
            url = item['url']
            user = self.get_twitter_user(url)
            results = self.api.search(url + " from:" + user)

            if len(results) > 0:
                vespa_fields = { }
                vespa_fields['twitter_favourite_count'] = results[0].favorite_count
                vespa_fields['twitter_retweet_count'] = results[0].retweet_count
                response = self.vespa.update_data(
                    schema = "newsarticle",
                    data_id = hashlib.sha256(item['url'].encode()).hexdigest(),
                    fields = vespa_fields
                )
            return item
        except:
            logger.critical("Failed twitter")
            return item

    def get_twitter_user(self, url):
        if url.startswith("https://www.abc.net.au"):
            return "abcnews"
        if url.startswith("https://www.theguardian.com"):
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
        if url.startswith("https://www.thewest.com.au"):
            return "westaustralian"
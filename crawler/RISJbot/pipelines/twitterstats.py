from itemadapter import ItemAdapter
import hashlib
import logging
import tweepy

logger = logging.getLogger(__name__)

class TwitterRelevance:
    api_key = "RGoakRHnlnx5Oo6Ogi0KR1Z98"
    api_secret = "KvM3oIs3zVK9dXcKiOrRv6YCbWlRuo9YBrL73rhrFP6PRCBXu4"
    bearer_token = "AAAAAAAAAAAAAAAAAAAAAIUyMwEAAAAAFbTFJhRL0rm6NbZt25aAPjjrLow%3DYKfU4Cziy7OMQAUihiGuBeqr37aDCoSySYYEVxriEeaJLpNe5O"
    access_token = "1356828778257739778-o9FNZ6UrhjsDIBCOD0LFsmRw1WftPt"
    access_token_secret = "CTLnGh5hzAg1hua7gKx9Huyl2YF2Gh4ixDV8yUoOBSghJ"

    def open_spider(self, spider):
        auth = tweepy.OAuthHandler(self.api_key, self.api_secret)
        auth.set_access_token(self.access_token, self.access_token_secret)
        self.api = tweepy.API(auth)

    def process_item(self, item, spider):
        try:
            url = item['url']
            user = self.get_twitter_user(url)
            results = self.api.search(url + " from:" + user)
            if len(results) > 0:
                item['twitter_favourite_count'] = results[0].favorite_count
                item['twitter_retweet_count'] = results[0].retweet_count
        except:
            logger.error("Failed twitter")
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
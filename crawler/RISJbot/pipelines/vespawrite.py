from itemadapter import ItemAdapter
import hashlib
from vespa.application import Vespa
import logging

logger = logging.getLogger(__name__)

class VespaWrite:
    def open_spider(self, spider):
        self.vespa = Vespa(url = "http://vespa-search", port = 8080)
    
    def process_item(self, item, spider):
        try:
            vespa_fields = { }
            vespa_fields['url'] = item['url']
            vespa_fields['bodytext'] = item['bodytext']
            vespa_fields['firstpubtime'] = item['firstpubtime']
            if ('modtime' in item):
                vespa_fields['modtime'] = item['modtime']
            vespa_fields['wordcount'] = item['wordcount']
            vespa_fields['headline'] = item['headline']
            vespa_fields['sentiment'] = item['sentiment']
            if ('summary' in item):
                vespa_fields['abstract'] = item['summary']
            if ('keywords' in item):
                vespa_fields['keywords'] = item['keywords']
            if ('bylines' in item):
                vespa_fields['bylines'] = item['bylines']
            if ('section' in item):
                vespa_fields['section'] = item['section']
            vespa_fields['source'] = item['source']
            if ('twitter_retweet_count' in item):
                vespa_fields['twitter_retweet_count'] = item['twitter_retweet_count']
            if ('twitter_favourite_count' in item):
                vespa_fields['twitter_favourite_count'] = item['twitter_favourite_count']

            response = self.vespa.feed_data_point(
                schema = "newsarticle",
                data_id = hashlib.sha256(item['url'].encode()).hexdigest(),
                fields = vespa_fields
            )
            return item
        except (KeyError, TypeError):
            logger.debug("error: " + item)
            pass
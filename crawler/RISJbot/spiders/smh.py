# -*- coding: utf-8 -*-
from RISJbot.spiders.newsrssfeedspider import NewsRSSFeedSpider
from RISJbot.loaders import NewsLoader
# Note: mutate_selector_del_xpath is somewhat naughty. Read its docstring.
from RISJbot.utils import mutate_selector_del_xpath
from RISJbot.utils import split_multiple_byline_string
from itemloaders.processors import Identity, TakeFirst
from itemloaders.processors import Join, Compose, MapCompose
import re

class SMHSpider(NewsRSSFeedSpider):
    name = 'smh'
    allowed_domains = ['www.smh.com.au']
    start_urls = ['https://www.smh.com.au/rss/feed.xml', 
        'https://www.smh.com.au/rss/politics/federal.xml', 
        'https://www.smh.com.au/rss/national/nsw.xml',
        'https://www.smh.com.au/rss/world.xml',
        'https://www.smh.com.au/rss/national.xml',
        'https://www.smh.com.au/rss/business.xml',
        'https://www.smh.com.au/rss/culture.xml',
        'https://www.smh.com.au/rss/technology.xml',
        'https://www.smh.com.au/rss/environment.xml',
        'https://www.smh.com.au/rss/lifestyle.xml',
        'https://www.smh.com.au/rss/sport.xml',

        ]

    def parse_node(self, response, selector):
        """Override NewsRSSFeedSpider to normalise URLs and remove tracking
           junk."""
        for rq in super().parse_node(response, selector):
            yield rq.replace(url = rq.url.split('?')[0])

    # RSSFeedSpider parses the RSS feed and calls parse_page(response) as a
    # callback for each page it finds in the feed.
    def parse_page(self, response):
        """@url http://www.dailymail.co.uk/news/article-4242322/Milo-Yiannopoulos-BANNED-CPAC-conference.html?ITO=1490
        @returns items 1
        @scrapes bodytext bylines fetchtime firstpubtime modtime headline
        @scrapes keywords section source summary url
        """

        s = response.selector
        # Remove some content from the tree before passing it to the loader.
        # There aren't native scrapy loader/selector methods for this.        
        mutate_selector_del_xpath(s, '//script')
        mutate_selector_del_xpath(s, '//*[@style="display:none"]')
        mutate_selector_del_xpath(s,
                                  '//div[contains(@class, "related-carousel")]'
                                 )

        l = NewsLoader(selector=s)

        # Get alternative to RSS-source URL fluff
        l.add_xpath('url', 'head/link[@rel="canonical"]/@href')

        l.add_xpath('bodytext', '//*[@id="content"]/div/article/section//text()')
        l.add_xpath('keywords',
                       'head/meta[@property="keywords"]/@content')
        # Add a number of items of data that should be standardised across
        # providers. Can override these (for TakeFirst() fields) by making
        # l.add_* calls above this line, or supplement gaps by making them
        # below.
        l.add_scrapymeta(response)
        l.add_fromresponse(response)
        l.add_htmlmeta()
        l.add_schemaorg(response)
        l.add_opengraph()
        l.add_readability(response)

        # TODO: JS dross in body; might need a standard solution to keep this
        #       out.
        # TODO: Related article dross in body. <div class=related-carousel>

        item = l.load_item()

#        self.logger.debug('bodytext', item['bodytext'])

        return item


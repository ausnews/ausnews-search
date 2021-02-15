import scrapy
from scrapy.crawler import CrawlerProcess
from RISJbot.spiders.abcau import ABCAUSpider
from RISJbot.spiders.newscomau import NEWSCOMAUSpider
from RISJbot.spiders.smh import SMHSpider
from RISJbot.spiders.zdnet import ZDNetSpider
from RISJbot.spiders.uk.guardian import GuardianSpider
from RISJbot.spiders.canberratimes import CanberraTimesSpider
from RISJbot.spiders.theage import TheAgeSpider
from RISJbot.spiders.thewest import TheWestSpider
from RISJbot.spiders.itnews import ITNewsSpider
from scrapy.utils.project import get_project_settings
from scrapy.utils.log import configure_logging
import logging


process = CrawlerProcess(get_project_settings())
configure_logging({'LOG_LEVEL': 'CRITICAL', 'LOG_STDOUT': False})
logger = logging.getLogger(__name__)
logger.disabled = True
logger.propagate = False
configure_logging(install_root_handler=True)
logging.disable(50)  # CRITICAL = 50
process.crawl(ZDNetSpider)
process.crawl(ABCAUSpider)
process.crawl(GuardianSpider)
process.crawl(NEWSCOMAUSpider)
process.crawl(SMHSpider)
process.crawl(CanberraTimesSpider)
process.crawl(TheAgeSpider)
process.crawl(TheWestSpider)
process.crawl(ITNewsSpider)
process.start()

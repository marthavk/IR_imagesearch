package crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class WikiCrawlController {

	private static final String CRAWL_STORAGE_FOLDER = "crawler4jStorage";
	private static final int NUMBER_OF_CRAWLERS = 8;

	private static CrawlController controller;

	public WikiCrawlController() {
		CrawlConfig config = new CrawlConfig();

		config.setCrawlStorageFolder(CRAWL_STORAGE_FOLDER);
		config.setPolitenessDelay(200);
		config.setMaxDepthOfCrawling(-1);
		config.setMaxPagesToFetch(100000);
		config.setResumableCrawling(false);

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		try {
			controller = new CrawlController(config, pageFetcher, robotstxtServer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		controller.addSeed("http://en.wikipedia.org/wiki/Main_Page");
		controller.start(WikiCrawler.class, NUMBER_OF_CRAWLERS);
	}

	public void start() {
		controller.start(WikiCrawler.class, NUMBER_OF_CRAWLERS);
	}

	public static void main(String[] args) throws Exception {
		org.apache.log4j.PropertyConfigurator.configure("log4j.properties");
		WikiCrawlController crawlController = new WikiCrawlController();
		crawlController.start();
	}

}
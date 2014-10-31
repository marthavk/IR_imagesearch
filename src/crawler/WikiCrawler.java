package crawler;

import java.util.List;

import org.apache.http.Header;

import parser.BasicImageParser;
import parser.Parser;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class WikiCrawler extends WebCrawler {

	private Parser parser;

	public WikiCrawler() {
		this.parser = new BasicImageParser();
	}

	/**
	 * The crawler will the given url if this method returns true
	 */
	@Override
	public boolean shouldVisit(WebURL url) {
		String href = url.getURL().toLowerCase();
		return href.startsWith("http://en.wikipedia.org/wiki/");
	}

	/**
	 * Called when a page is crawled
	 */
	@Override
	public void visit(Page page) {
		// System.out.println("Crawler - Fetched page: " +
		// page.getWebURL().getURL());
		parser.parse(((HtmlParseData) page.getParseData()).getHtml());
	}

	/**
	 * Only for testing
	 */
	private void printPage(Page page) {
		int docid = page.getWebURL().getDocid();
		String url = page.getWebURL().getURL();
		String domain = page.getWebURL().getDomain();
		String path = page.getWebURL().getPath();
		String subDomain = page.getWebURL().getSubDomain();
		String parentUrl = page.getWebURL().getParentUrl();
		String anchor = page.getWebURL().getAnchor();

		System.out.println("Docid: " + docid);
		System.out.println("URL: " + url);
		System.out.println("Domain: '" + domain + "'");
		System.out.println("Sub-domain: '" + subDomain + "'");
		System.out.println("Path: '" + path + "'");
		System.out.println("Parent page: " + parentUrl);
		System.out.println("Anchor text: " + anchor);

		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();
			String html = htmlParseData.getHtml();
			List<WebURL> links = htmlParseData.getOutgoingUrls();

			System.out.println("Text length: " + text.length());
			System.out.println("Html length: " + html.length());
			System.out.println("Number of outgoing links: " + links.size());
		}

		Header[] responseHeaders = page.getFetchResponseHeaders();
		if (responseHeaders != null) {
			System.out.println("Response headers:");
			for (Header header : responseHeaders) {
				System.out.println("\t" + header.getName() + ": " + header.getValue());
			}
		}

		System.out.println(((HtmlParseData) page.getParseData()).getHtml());

		System.out.println("=============");
	}
}
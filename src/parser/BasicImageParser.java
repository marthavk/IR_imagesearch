package parser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import solr.SolrConnector;

public class BasicImageParser implements Parser {
	
	private static final String url = "http://en.wikipedia.org/wiki/United_States"; 

    private SolrConnector connector;

	public static void main(String[] args) throws IOException {
		String htmlString = Jsoup.connect(url).get().outerHtml();
		new BasicImageParser().parse(htmlString);
	}

    public BasicImageParser() {
        connector = new SolrConnector();
    }
	
	public void parse(String htmlString) {
		Document dom = Jsoup.parse(htmlString);
		
		/* The infobox in the top right corner of a wikipedia article */
		Element infoBox = dom.select("table.infobox").first();
		if (infoBox != null) {
			parseInfoBox(infoBox, dom);
		}
		
		
		Elements thumbs = dom.select(".thumb");
		Iterator<Element> thumbsIter = thumbs.iterator();
		while (thumbsIter.hasNext()) {
			parseThumb(thumbsIter.next(), dom);
			
		}
	}
	
	private String getPageTitle(Document dom) {
		return dom.select("h1#firstHeading").first().text();
	}
	
	private void parseInfoBox(Element infoBox, Document dom) {
		Elements images = infoBox.select("img");
		Iterator<Element> it = images.iterator();
		while (it.hasNext()) {
			Map<String, Object> imageKeys = new HashMap<String, Object>();
			Element image = it.next();
			
			imageKeys.put("title", getPageTitle(dom));
			
			parseImageAttributes(image, imageKeys);
			
			Element tableCell = image.parent().parent();
			String description = tableCell.text();
			if (!description.isEmpty()) 
				imageKeys.put("description", description);
			
			//Let the first paragraph after the infobox be the context
			Element context = infoBox;
			while ( ( context = context.nextElementSibling() ) != null ) {
				if ( "p".equals( context.tagName() ) )
					break;
			}
			if  (context != null)
				imageKeys.put("context", context.text());
			
			
			connector.push(imageKeys);
		}
	}
	
	private void parseThumb(Element thumb, Document dom) {
		Map<String, Object> imageKeys = new HashMap<String, Object>();
		Element image = thumb.select("img").first();
		if (image == null)
			return;
		
		imageKeys.put("title", getPageTitle(dom));
		
		parseImageAttributes(image, imageKeys);
		
		Element description = thumb.select(".thumbcaption").first();
		if (description != null) 
			imageKeys.put("description", description.text());
		
		Element context = thumb.nextElementSibling();
		if (context != null)
			imageKeys.put("context", context.text());
		
		//closest subtitle
		Element sibling = thumb;
		String[] headings = {"h1","h2","h3","h4","h5","h6"};
		while ( (sibling = sibling.previousElementSibling()) != null ) {
			if ( Arrays.asList(headings).contains( sibling.tagName() ) ) {
				String subtitle = sibling.text().replace("[edit]", "");
				imageKeys.put("subtitle", subtitle);
				break;
			}
		}
		
		connector.push(imageKeys);
	}
	
	private void parseImageAttributes(Element image, Map<String, Object> imageKeys) {
		String src = image.attr("src");
		imageKeys.put("url", src);
		String fileName = tokenizeFileName(src);
		imageKeys.put("filename", fileName);
		if (image.hasAttr("alt") && !image.attr("alt").isEmpty())
			imageKeys.put("alt", image.attr("alt"));
	}
	
	private String tokenizeFileName(String url) {
		String[] path = url.split("/");
		String fileName = path[path.length - 1];//get the part of the url after the last slash (the file name)
		fileName = fileName.replaceAll("(jpe?g|png|gif|svg)", "");//Remove file extension
		try { //convert url encoded
			fileName = URLDecoder.decode(fileName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		fileName = fileName.replaceAll("\\.|,",""); //Remove dots and commas
		fileName = fileName.replaceAll("_|-"," "); //change underscore and hyphen to space
		fileName = fileName.replaceAll("[0-9]+px",""); //Remove image size from filename
		return fileName.trim();
	}

}
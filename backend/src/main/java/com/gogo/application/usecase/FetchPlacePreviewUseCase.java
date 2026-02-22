package com.gogo.application.usecase;

import com.gogo.application.dto.PlacePreviewResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class FetchPlacePreviewUseCase {

    private static final String USER_AGENT =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) " +
            "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";

    public PlacePreviewResponse execute(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(5000)
                    .followRedirects(true)
                    .get();

            String title = ogContent(doc, "og:title");
            if (title == null) title = doc.title();

            String imageUrl = ogContent(doc, "og:image");
            String address = ogContent(doc, "og:street-address");
            String description = ogContent(doc, "og:description");

            return new PlacePreviewResponse(title, imageUrl, address, description);
        } catch (Exception e) {
            return new PlacePreviewResponse(null, null, null, null);
        }
    }

    private String ogContent(Document doc, String property) {
        Element el = doc.selectFirst("meta[property=" + property + "]");
        if (el == null) el = doc.selectFirst("meta[name=" + property + "]");
        return el != null ? el.attr("content") : null;
    }
}

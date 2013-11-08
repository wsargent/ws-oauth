package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.utils.URIBuilder;
import play.libs.F.*;
import play.libs.ws.*;
import play.mvc.*;

public class Application extends Controller {

    static final String HOST_AUTHENTICATE = "foursquare.com/oauth2/authenticate";
    static final String HOST_ACCESS_TOKEN = "https://www.foursquare.com/oauth2/access_token";
    static final String CLIENT_ID = "***";
    static final String REDIRECT_URI = "http://localhost:9000/code";
    static final String CLIENT_SECRET = "***";
    static final String GRANT_TYPE = "authorization_code";
    static final String RESPONSE_TYPE = "code";
    static final String FOURSQUARE_API_FRIENDS = "https://api.foursquare.com/v2/users/self/friends";

    public static Result index() {
        String uri = new URIBuilder().setScheme("https").setHost(HOST_AUTHENTICATE)
                .setParameter("client_id", CLIENT_ID).setParameter("response_type", RESPONSE_TYPE)
                .setParameter("redirect_uri", REDIRECT_URI).toString();

        return redirect(uri);
    }

    public static Promise<Result> code() {

        WSRequestHolder authenticate = WS.url(HOST_ACCESS_TOKEN).setQueryParameter("client_id", CLIENT_ID)
                .setQueryParameter("client_secret", CLIENT_SECRET).setQueryParameter("grant_type", GRANT_TYPE)
                .setQueryParameter("redirect_uri", REDIRECT_URI)
                .setQueryParameter("code", request().getQueryString("code").toString());

        final Promise<Result> resultPromise = authenticate.get().flatMap(new Function<WSResponse, Promise<Result>>() {
            public Promise<Result> apply(WSResponse response) {
                JsonNode json = response.asJson();
                return WS.url(FOURSQUARE_API_FRIENDS).setQueryParameter("oauth_token", json.findPath("access_token").asText())
                        .setQueryParameter("v", "20131108").get().map(new Function<WSResponse, Result>() {
                            public Result apply(WSResponse response) {
                                return ok(response.asJson());
                            }
                        });
            }
        });

        return resultPromise;
    }

}
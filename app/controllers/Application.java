package controllers;

import models.SharedTranscript;
import play.mvc.*;

public class Application extends Controller {

    public static Result index() {
        return ok(views.html.index.render());
    }

    public static Result addUtterance() {
        Http.RequestBody body = request().body();
        String textBody = body.asText();
        if (null == textBody) {
            textBody = "";
        }
        SharedTranscript ourText = SharedTranscript.find.byId((long)1);
        if (ourText == null) {
            SharedTranscript.create();
        }
        ourText = SharedTranscript.find.byId((long)1);
        ourText.addToSharedText(textBody);

        return ok();
    }

    public static Result getUtterances() {
        response().setContentType("text/event-stream");
        SharedTranscript ourText = SharedTranscript.find.byId((long)1);
        if (ourText == null) {
            SharedTranscript.create();
        }
        ourText = SharedTranscript.find.byId((long)1);

        return ok(ourText.getSSESharedText());
    }

    public static Result modifyOption() {
        return ok();
    }

    public static Result upvoteOption() {
        return ok();
    }

}

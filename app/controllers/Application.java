package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.SharedTranscript;
import models.UpdateMessenger;
import play.mvc.*;
import playextension.EventSource;

public class Application extends Controller {

    public static Result index() {
        return ok(views.html.index.render());
    }

    public static Result blockOption() {
        Http.RequestBody body = request().body();
        JsonNode jsonNode = body.asJson();

        int utteranceIndex = jsonNode.get(0).asInt();
        int optionIndex = jsonNode.get(1).asInt();

        SharedTranscript ourText = SharedTranscript.getOnlySharedTranscript();

        ourText.blockOption(utteranceIndex, optionIndex);

        return ok();
    }

    public static Result unblockOption() {
        Http.RequestBody body = request().body();
        JsonNode jsonNode = body.asJson();

        int utteranceIndex = jsonNode.get(0).asInt();
        int optionIndex = jsonNode.get(1).asInt();

        SharedTranscript ourText = SharedTranscript.getOnlySharedTranscript();

        ourText.unblockOption(utteranceIndex, optionIndex);

        return ok();
    }

    public static Result addUtterance() {
        Http.RequestBody body = request().body();
        String textBody = body.asText();
        if (null == textBody) {
            textBody = "";
        }
        SharedTranscript ourText = SharedTranscript.getOnlySharedTranscript();
        ourText.addToSharedTranscript(textBody);

        return ok();
    }

    public static Result getUtterances() {
        response().setContentType("text/event-stream");
        SharedTranscript ourText = SharedTranscript.getOnlySharedTranscript();

        return ok(new EventSource() {
            @Override
            public void onConnected() {
                UpdateMessenger.singleton.tell(this, null);
            }
        });
    }

    public static Result modifyOption() {
        Http.RequestBody body = request().body();
        JsonNode jsonNode = body.asJson();

        int utteranceIndex = jsonNode.get(0).asInt();
        int optionIndex = jsonNode.get(1).asInt();
        String newValue = jsonNode.get(2).asText();

        SharedTranscript ourText = SharedTranscript.getOnlySharedTranscript();

        ourText.modifySharedTranscript(utteranceIndex, optionIndex, newValue);

        return ok();
    }

    public static Result upvoteOption() {
        return ok();
    }

}

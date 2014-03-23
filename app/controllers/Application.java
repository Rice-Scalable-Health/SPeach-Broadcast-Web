package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.RawUtterance;
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

        // pull out the text and confidence from the sent string.
        String[] textAndConfidence = textBody.split("&&&");
        String text = textAndConfidence[0];
        Double confidence = Double.parseDouble(textAndConfidence[1]);

        // create the raw utterance in the database.
        RawUtterance.create(text, confidence);

        SharedTranscript ourText = SharedTranscript.getOnlySharedTranscript();
        // set the confidence levels
        if (confidence > .9) {
            ourText.addToSharedTranscript(text);
        }
        else if (confidence > .8) {
            ourText.addToSharedTranscript("*"+text);
        }
        else {
            ourText.addToSharedTranscript("**"+text);
        }

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

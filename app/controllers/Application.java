package controllers;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import models.Option;
import models.SharedTranscript;
import models.UpdateMessenger;
import models.Utterance;
import models.Vote;
import play.mvc.*;
import playextension.EventSource;

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

    /**
     * create a vote ballot for an Option by requesting the client IP
     * Ensure there's one option vote per utterance per IP
     * @param optionId ID for an Option 
     * @return
     */
    public static Result upvoteOption(long optionId, long utterId) {
    		String ip = request().remoteAddress();
    		
    		// find out all the votes the client has casted
    		List<Vote> votesForIp = Vote.findByIP(ip);
    		Option optionToUpvote = Option.find.byId(optionId);
    		Long utterOfOption = optionToUpvote.getUtter().getId();
    		
    		/*
    		 * check all valid votes casted by this client, 
  		   *   if a vote is not pointed to this utterance,
  		   *	 do nothing
  		   *   if the vote's option is pointed to this utterance, 
  		   *     if the option id matches,
  		   * 		   do nothing
  		   *     if the option id does not match, (well, this means that there's a conflict of votes)
  		   *       marking this vote invalid
  		   * create a new voted 
  		   * increment the option vote count
    		 */
    		for (Vote vote : votesForIp) {
    			if (vote.getValid() == true) {
    				if (vote.getOption().getUtter().getId() == utterOfOption) {
    					if (vote.getOption().getId() != optionId) {
    						vote.setValid(false);
    					}
    				}
    			}
    		}
    		// if the person has not voted yet.
    		optionToUpvote.addVote(ip);
    		
        return ok();
    }

}

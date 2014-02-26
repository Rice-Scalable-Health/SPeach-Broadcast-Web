package controllers;

import java.util.ArrayList;
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
    public static Result upvoteOption(long optionId) {
    		String ip = request().remoteAddress();
    		
    		// find out all the votes the client has casted
    		List<Vote> votesForIp = Vote.findByIP(ip);
    		Option optionToUpvote = Option.find.byId(optionId);
    		Long utterOfOption = optionToUpvote.getUtter().getId();
    		
    		// gather all votes by this client for this utterance
    		// if zero votes, create a new vote, increment the option 
    		// else go through all votes, 
    		//   verify the optionId, 
    		//    if success, make valid, done, return
    		//		if fails, mark it invalid, decrement the associated option
    		// verification complete, no valid votes for this utterance, create a new vote
    		
    		List<Vote> votesOfUtter = new ArrayList<Vote>();
    		for (Vote vote : votesForIp) {
    			if (vote.getOption().getUtter().getId() == utterOfOption) {
    				votesOfUtter.add(vote);
    			}
    		}
    		// saved for debugging purpose
    		/*if (votesOfUtter.size() == 0) {
    			Vote.create(ip, optionToUpvote);
    			optionToUpvote.increment();
    			return ok();
    		}else{*/
    		if (votesOfUtter.size() != 0) {
	    		for (Vote vote : votesOfUtter) {
	    			if (vote.getOption().getId() == optionToUpvote.getId()) {
	    				if (vote.getValid() == true) {
	    					return ok(); // method ends here if verification complete
	    				}else if (vote.getValid() == false) {
	    					vote.setValid(true);
	    					optionToUpvote.increment();
	    				}
	    			}else if (vote.getOption().getId() != optionToUpvote.getId()) {
	    				vote.setValid(false);
	    				vote.getOption().decrement();
	    			}
	    		}
    		}
    		// only reaches here when all votes are flagged invalid for this utterance
    		// create a new vote
    		Vote.create(ip, optionToUpvote);
    		optionToUpvote.increment();
    		
        return ok();
    }

}

package controllers;

import models.*;
import models.forms.CreateSessionForm;
import models.forms.JoinSessionForm;
import play.data.Form;
import play.mvc.*;
import playextension.EventSource;
import java.io.File;
import java.math.BigDecimal;

import static play.data.Form.form;

public class Application extends Controller {

    public static Result index() {
        return ok(views.html.index.render(Form.form(CreateSessionForm.class), Form.form(JoinSessionForm.class)));
    }

    public static Result instructions() {
        return ok(views.html.instructions.render(Form.form(CreateSessionForm.class), Form.form(JoinSessionForm.class)));
    }

    public static Result downloadApp() {
        response().setContentType("application/x-download");
        response().setHeader("Content-disposition","attachment; filename=SPeachAPP.md");
        return ok(new File("public/android-app/SPeachAPP.md"));
    }

    public static Result newSession() {
        final Form<CreateSessionForm> filledForm = form(CreateSessionForm.class).bindFromRequest();

        if (filledForm.hasErrors()) {
            return badRequest(views.html.index.render(filledForm, Form.form(JoinSessionForm.class)));
        } else {
            Session session = Session.create(filledForm.get().getName());
            return redirect(routes.Application.viewTranscript(session.getId()));
        }
    }

    public static Result joinSession() {
        final Form<JoinSessionForm> filledForm = form(JoinSessionForm.class).bindFromRequest();

        if (filledForm.hasErrors()) {
            return badRequest(views.html.index.render(Form.form(CreateSessionForm.class), filledForm));
        } else {
            String sessionId = filledForm.get().getSessionId();
            Session session = Session.findById(sessionId);

            if (session == null) {
                return badRequest(views.html.index.render(Form.form(CreateSessionForm.class), filledForm));
            }
            else {
                return redirect(routes.Application.viewTranscript(sessionId));
            }
        }
    }

    public static Result editTranscript(String id) {
        if (Session.findById(id) == null) {
            return redirect(routes.Application.index());
        }
        Session session = Session.findById(id);
        String sessionName = session.getName();

        return ok(views.html.editor.render(id, sessionName));
    }

    public static Result viewTranscript(String id) {
        if (Session.findById(id) == null) {
            return redirect(routes.Application.index());
        }
        Session session = Session.findById(id);
        String sessionName = session.getName();

        return ok(views.html.viewTranscript.render(id, sessionName));
    }

    public static Result requestHelp(String id) {
        Http.RequestBody body = request().body();
        String textBody = body.asText();
        if (null == textBody) {
            textBody = "";
        }

        if (textBody.equals("")) {
            textBody = "0";
        }

        int indexToHelpWith = Integer.parseInt(textBody);
        SharedTranscript ourText = SharedTranscript.findBySessionId(id);

        ourText.requestHelp(indexToHelpWith, id);

        return ok();
    }

    public static Result addUtterance(String id) {
        Http.RequestBody body = request().body();
        String textBody = body.asText();
        if (null == textBody) {
            textBody = "";
        }

        // pull out the text and confidence from the sent string.
        String[] textAndConfidence = textBody.split("&&&");
        String text = textAndConfidence[0];
        BigDecimal confidence = new BigDecimal(textAndConfidence[1]);

        // create the raw utterance in the database.
        RawUtterance.create(text, confidence);

//        // write out to a file. the filename should be unique to each session
//        RawUtterance.WriteToFile("Utterances");

        SharedTranscript ourText = SharedTranscript.findBySessionId(id);

//        // set the confidence levels
//        if (confidence > .9) {
//            ourText.addToSharedTranscript(text+"\t");
//        }
//        else if (confidence > .8) {
//            ourText.addToSharedTranscript("*"+text+"\t");
//        }
//        else {
//            ourText.addToSharedTranscript("**"+text+"\t");
//        }
        String toAdd = "";
        if (!ourText.getTranscript().equals("")) {
            toAdd = "\t";
        }

        ourText.addToSharedTranscript(toAdd+text, id);

        return ok();
    }

    public static Result getUtterances(final String id) {
        response().setContentType("text/event-stream");
        SharedTranscript ourText = SharedTranscript.findBySessionId(id);

        return ok(new EventSource() {
            @Override
            public void onConnected() {
                UpdateMessenger.singleton.tell(new EventSourceRegisterRequest(this, id), null);
            }
        });
    }

    public static Result getTranscriptData(final String id) {
        return ok(new EventSource() {
            @Override
            public void onConnected() {
                ViewerUpdateMessenger.singleton.tell(new EventSourceRegisterRequest(this, id), null);
            }
        });
    }

    public static Result modifyOption(String id) {
        Http.RequestBody body = request().body();
        String textBody = body.asText();
        if (null == textBody) {
            textBody = "";
        }
        SharedTranscript ourText = SharedTranscript.findBySessionId(id);

        ourText.modifySharedTranscript(textBody, id);

        return ok();
    }

    public static Result upvoteOption(String id) {
        return ok();
    }

    public static Result speaker(String id) {
        return ok(views.html.speaker.render());
    }

}

import play.Application;
import play.GlobalSettings;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.notFound;

public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {

        //Credits by ASCII art - BladeRunner
        System.out.println(" _____________________\n" +
                "/ ,-----------------. \\\n" +
                "| |    1.05459 e -34| |\n" +
                "| `-----------------' |\n" +
                "| [@ ] On/Off  ###### |\n" +
                "|              ###### |\n" +
                "| [7] [8] [9] [C] [AC]|\n" +
                "|                     |\n" +
                "| [4] [5] [6] [X] [%] |\n" +
                "|                     |\n" +
                "| [1] [2] [3] [+] [-] |\n" +
                "|                     |\n" +
                "| [0] [.]  [EXP]  [=] |\n" +
                "\\____________________/\n" +
                "PROTOTYPE");

    }

    /**
     * When an exception occurs in your application, the onError operation will be called. The default is to use the internal framework error page. You can override this
     */
    @Override
    public F.Promise<Result> onHandlerNotFound(Http.RequestHeader request) {
        return F.Promise.<Result> pure(notFound(views.html.mensagens.erro.naoEncontrada.render(request.uri())));
    }

    /**
     * When an exception occurs in your application, the onError operation will be called. The default is to use the internal framework error page. You can override this
     */
    @Override
    public F.Promise<Result> onError(Http.RequestHeader request, Throwable t) {
        return super.onError(request, t);
    }

    /**
     * The onBadRequest operation will be called if a route was found, but it was not possible to bind the request parameters
     */
    @Override
    public F.Promise<Result> onBadRequest(Http.RequestHeader request, String error) {
        return F.Promise.<Result> pure(badRequest(views.html.error.render(error)));
    }

}
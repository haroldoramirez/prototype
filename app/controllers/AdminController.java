package controllers;

import models.Log;
import models.Usuario;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import secured.SecuredAdmin;

@Security.Authenticated(SecuredAdmin.class)
public class AdminController extends Controller {

    /**
     * mostra a pagina inicial
     *
     * rota /admin
     *
     * @return mostra a pagina inicial caso o usuario seja admin
     */
    public Result painel(int page, String sortBy, String order, String filter, String autor) {
        return ok(views.html.admin.inicio.render(
                Usuario.page(page, 10, sortBy, order, filter),
                Usuario.last(),
                Log.last()));
    }
}

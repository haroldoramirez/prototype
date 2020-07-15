package controllers;

import daos.UsuarioDAO;
import models.Usuario;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Optional;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    @Inject
    private UsuarioDAO usuarioDAO;

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result main() {

        String email = session().get("email");

        Optional<Usuario> possivelUsuario = usuarioDAO.comEmail(email);

        if (possivelUsuario.isPresent()) {
            Usuario usuario = possivelUsuario.get();
            return ok(views.html.main.render(usuario));
        } else {
            return ok(views.html.main.render(null));
        }

    }

}

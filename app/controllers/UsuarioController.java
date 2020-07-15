package controllers;

import com.avaje.ebean.Ebean;
import daos.UsuarioDAO;
import models.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import secured.SecuredAdmin;
import secured.SecuredUser;
import validators.UsuarioAdminFormData;
import views.html.admin.usuarios.list;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Optional;

import static play.data.Form.form;

public class UsuarioController extends Controller {

    @Inject
    private UsuarioDAO usuarioDAO;

    private final Form<Usuario> usuarioForm = Form.form(Usuario.class);

    static private final LogController logController = new LogController();

    private String mensagem;
    private String tipoMensagem;

    private Optional<Usuario> usuarioAtual() {
        String email = session().get("email");
        return usuarioDAO.comEmail(email);
    }

    /**
     * @return a object user authenticated
     */
    @Nullable
    private Usuario atual() {

        String username = session().get("email");

        try {
            //retorna o usuário atual que esteja logado no sistema
            return Ebean.createQuery(Usuario.class, "find usuario where email = :email")
                    .setParameter("email", username)
                    .findUnique();
        } catch (Exception e) {
            Logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * Retrieve a autenticated user
     *
     * @return a user json
     */
    @Security.Authenticated(SecuredUser.class)
    public Result autenticado() {
        Usuario usuarioAtual = atual();

        if (usuarioAtual == null) {
            return notFound("Usuario não autenticado");
        }

        return ok(Json.toJson(usuarioAtual));
    }

    /**
     * Retrieve a list of all usuarios
     *
     * @return a list of all usuarios in a render template
     */
    @Security.Authenticated(SecuredAdmin.class)
    public Result telaLista(int page, String sortBy, String order, String filter) {

        //Necessario para verificar se o usuario e gerente
        if (usuarioAtual().isPresent()) {
            Usuario usuario = usuarioAtual().get();
            if (usuario.isGerente()) {
                return forbidden(views.html.mensagens.erro.naoAutorizado.render());
            }
        }

        try {
            return ok(
                    list.render(
                            Usuario.page(page, 18, sortBy, order, filter),
                            sortBy, order, filter
                    )
            );
        } catch (Exception e) {
            Logger.error(e.getMessage());
            return badRequest(views.html.error.render(e.getMessage()));
        }

    }

    /**
     * @return cadastro form for register a new user
     */
    @Security.Authenticated(SecuredAdmin.class)
    public Result telaNovo() {

        //Necessario para verificar se o usuario e gerente
        if (usuarioAtual().isPresent()) {
            Usuario usuario = usuarioAtual().get();
            if (usuario.isGerente()) {
                return forbidden(views.html.mensagens.erro.naoAutorizado.render());
            }
        }

        Form<UsuarioAdminFormData> usuarioForm = form(UsuarioAdminFormData.class);
        return ok(views.html.admin.usuarios.create.render(usuarioForm, Usuario.getListaPapeis()));
    }

    /**
     * @return detail form with a user
     */
    @Security.Authenticated(SecuredAdmin.class)
    public Result telaDetalhe(Long id) {

        //Necessario para verificar se o usuario e gerente
        if (usuarioAtual().isPresent()) {
            Usuario usuario = usuarioAtual().get();
            if (usuario.isGerente()) {
                return forbidden(views.html.mensagens.erro.naoAutorizado.render());
            }
        }

        try {
            Usuario usuario = Ebean.find(Usuario.class, id);

            if (usuario == null) {
                return notFound(views.html.mensagens.erro.naoEncontrado.render("Usuário não encontrado"));
            }

            return ok(views.html.admin.usuarios.detail.render(usuario));
        } catch (Exception e) {
            Logger.error(e.getMessage());
            return badRequest(views.html.error.render(e.getMessage()));
        }

    }

    /**
     * @return edit form with a user
     */
    @Security.Authenticated(SecuredAdmin.class)
    public Result telaEditar(Long id) {

        //Necessario para verificar se o usuario e gerente
        if (usuarioAtual().isPresent()) {
            Usuario usuario = usuarioAtual().get();
            if (usuario.isGerente()) {
                return forbidden(views.html.mensagens.erro.naoAutorizado.render());
            }
        }

        try {

            //logica onde instanciamos um objeto que esteja cadastrado na base de dados
            UsuarioAdminFormData usuarioAdminFormData = (id == 0) ? new UsuarioAdminFormData() : models.Usuario.makeUsuarioAdminFormData(id);

            //apos o objeto ser instanciado passamos os dados para o eventoformdata e os dados serao carregados no form edit
            Form<UsuarioAdminFormData> formData = Form.form(UsuarioAdminFormData.class).fill(usuarioAdminFormData);

            if (formData == null) {
                return notFound(views.html.mensagens.erro.naoEncontrado.render("Usuário não encontrado"));
            }

            return ok(views.html.admin.usuarios.edit.render(id, formData, Usuario.getListaPapeis()));
        } catch (Exception e) {
            Logger.error(e.getMessage());
            return badRequest(views.html.error.render(e.getMessage()));
        }

    }

    /**
     * Save a user
     *
     * @return ok user json
     */
    @Security.Authenticated(SecuredAdmin.class)
    public Result inserirAdmin() {

        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);

        //Necessario para verificar se o usuario e gerente
        if (usuarioAtual().isPresent()) {
            Usuario usuario = usuarioAtual().get();
            if (usuario.isGerente()) {
                return forbidden(views.html.mensagens.erro.naoAutorizado.render());
            }
        }

        //Resgata os dados do formulario atraves de uma requisicao e realiza a validacao dos campos
        Form<UsuarioAdminFormData> formData = Form.form(UsuarioAdminFormData.class).bindFromRequest();

        //se existir erros nos campos do formulario retorne o CursoFormData com os erros
        if (formData.hasErrors()) {
            return badRequest(views.html.admin.usuarios.create.render(formData, Usuario.getListaPapeis()));
        } else {
            try {
                //Converte os dados do formularios para uma instancia
                Usuario usuario = Usuario.makeInstance(formData.get());

                String senha_original = formData.field("senha").value();

                //faz uma busca na base de dados do usuario
                Usuario usuarioBusca = Ebean.find(Usuario.class).where().eq("email", formData.data().get("email")).findUnique();

                if (usuarioBusca != null) {
                    formData.reject(Messages.get("register.error.already.registered") + " '" + usuarioBusca.getEmail() + "' ");
                    return badRequest(views.html.admin.usuarios.create.render(formData, Usuario.getListaPapeis()));
                }

                usuario.setSenha(BCrypt.hashpw(formData.field("senha").value(), BCrypt.gensalt()));

                usuario.setStatus(true);
                usuario.setDataCadastro(new Date());
                usuario.setUltimoAcesso(Calendar.getInstance());

                Ebean.save(usuario);

                if (usuarioAtual().isPresent()) {
                    formatter.format("Usuário administrador: '%1s' cadastrou um novo Usuário: '%2s' com privilégios de '%3s'", usuarioAtual().get().getEmail(), usuario.getEmail(), usuario.getPapel());
                    logController.inserir(sb.toString());
                }

                tipoMensagem = "success";
                mensagem = "Usuário '" + usuario.getNome() + "' cadastrado com sucesso.";
                return created(views.html.mensagens.usuario.mensagens.render(mensagem, tipoMensagem));
            } catch (Exception e) {
                Logger.error(e.getMessage());
                formData.reject("Erro interno de Sistema. Descrição: " + e);
                return badRequest(views.html.admin.usuarios.create.render(formData, Usuario.getListaPapeis()));
            }
        }
    }

    /**
     * Update a user from id
     *
     * @param id identificador
     * @return a user updated with a form
     */
    @Security.Authenticated(SecuredAdmin.class)
    public Result editar(Long id) {

        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);

        //Necessario para verificar se o usuario e gerente
        if (usuarioAtual().isPresent()) {
            Usuario usuario = usuarioAtual().get();
            if (usuario.isGerente()) {
                return forbidden(views.html.mensagens.erro.naoAutorizado.render());
            }
        }
        //Resgata os dados do formulario atraves de uma requisicao e realiza a validacao dos campos
        Form<UsuarioAdminFormData> formData = Form.form(UsuarioAdminFormData.class).bindFromRequest();

        //se existir erros nos campos do formulario retorne o CursoFormData com os erros
        if (formData.hasErrors()) {
            return badRequest(views.html.admin.usuarios.edit.render(id, formData, Usuario.getListaPapeis()));
        } else {
            try {

                Usuario usuarioBusca = Ebean.find(Usuario.class, id);

                if (usuarioBusca == null) {
                    return notFound(views.html.mensagens.erro.naoEncontrado.render("Usuário não encontrado"));
                }

                //verifica caso tente alterar o administrador do sistema
                if (usuarioBusca.getNome().equals("Administrador")) {
                    mensagem = "Não alterar o administrador do sistema.";
                    tipoMensagem = "danger";
                    return badRequest(views.html.mensagens.usuario.mensagens.render(mensagem, tipoMensagem));
                }

                Form<Usuario> form = usuarioForm.fill(Usuario.find.byId(id)).bindFromRequest();

                Usuario usuario = form.get();

                String senha = BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt());

                usuario.setId(id);
                usuario.setSenha(senha);
                usuario.setDataAlteracao(new Date());

                usuario.update();

                if (usuarioAtual().isPresent()) {
                    formatter.format("Usuário Administrador: '%1s' atualizou o Usuário: '%2s'.", usuarioAtual().get().getEmail(), usuario.getEmail());
                    logController.inserir(sb.toString());
                }

                tipoMensagem = "info";
                mensagem = "Usuário '" + usuario.getNome() + "' atualizado com sucesso.";
                return ok(views.html.mensagens.usuario.mensagens.render(mensagem, tipoMensagem));
            } catch (Exception e) {
                tipoMensagem = "danger";
                mensagem = "Erro interno de Sistema. Descrição: " + e;
                Logger.error(e.getMessage());
                return badRequest(views.html.mensagens.usuario.mensagens.render(mensagem, tipoMensagem));
            }
        }

    }

    /**
     * block a user from id
     *
     * @param id identificador
     * @return a message with a form
     */
    @Security.Authenticated(SecuredAdmin.class)
    public Result bloquear(Long id) {

        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);

        //Necessario para verificar se o usuario e gerente
        if (usuarioAtual().isPresent()) {
            Usuario usuario = usuarioAtual().get();
            if (usuario.isGerente()) {
                return forbidden(views.html.mensagens.erro.naoAutorizado.render());
            }
        }

        try {

            Usuario usuario = Ebean.find(Usuario.class, id);

            if (usuario == null) {
                return notFound(views.html.mensagens.erro.naoEncontrado.render("Usuário não encontrado"));
            }

            //verifica caso tente alterar o administrador do sistema
            if (usuario.getNome().equals("Administrador")) {
                mensagem = "Não bloquear o administrador do sistema.";
                tipoMensagem = "danger";
                return badRequest(views.html.mensagens.usuario.mensagens.render(mensagem, tipoMensagem));
            }

            //busca o usuario atual que esteja logado no sistema
            Usuario usuarioAtual = atual();

            //caso o usuario administrador queira excluir a si proprio enquanto estiver autenticado
            if (usuarioAtual.getEmail().equals(usuario.getEmail())) {
                mensagem = "Não bloquear seu próprio usuário enquanto ele estiver autenticado.";
                tipoMensagem = "danger";
                return badRequest(views.html.mensagens.usuario.mensagens.render(mensagem, tipoMensagem));
            }

            usuario.setStatus(false);
            usuario.update();

            if (usuarioAtual().isPresent()) {
                formatter.format("Usuário Administrador: '%1s' bloqueou o Usuário: '%2s'.", usuarioAtual().get().getEmail(), usuario.getEmail());
                logController.inserir(sb.toString());
            }

            tipoMensagem = "success";
            mensagem = "Usuário '" + usuario.getNome() + "' foi bloqueado.";
            return ok(views.html.mensagens.usuario.mensagens.render(mensagem, tipoMensagem));
        } catch (Exception e) {
            tipoMensagem = "danger";
            mensagem = "Erro interno de Sistema. Descrição: " + e;
            Logger.error(e.getMessage());
            return badRequest(views.html.mensagens.usuario.mensagens.render(mensagem, tipoMensagem));
        }

    }

    /**
     * block a user from id
     *
     * @param id identificador
     * @return a message with a form
     */
    @Security.Authenticated(SecuredAdmin.class)
    public Result desbloquear(Long id) {

        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);

        //Necessario para verificar se o usuario e gerente
        if (usuarioAtual().isPresent()) {
            Usuario usuario = usuarioAtual().get();
            if (usuario.isGerente()) {
                return forbidden(views.html.mensagens.erro.naoAutorizado.render());
            }
        }

        try {

            Usuario usuario = Ebean.find(Usuario.class, id);

            if (usuario == null) {
                return notFound(views.html.mensagens.erro.naoEncontrado.render("Usuário não encontrado"));
            }

            usuario.setStatus(true);
            usuario.update();

            if (usuarioAtual().isPresent()) {
                formatter.format("Usuário Administrador: '%1s' desbloqueou o Usuário: '%2s'.", usuarioAtual().get().getEmail(), usuario.getEmail());
                logController.inserir(sb.toString());
            }

            tipoMensagem = "success";
            mensagem = "Usuário '" + usuario.getNome() + "' foi desbloqueado.";
            return ok(views.html.mensagens.usuario.mensagens.render(mensagem, tipoMensagem));
        } catch (Exception e) {
            tipoMensagem = "danger";
            mensagem = "Erro interno de Sistema. Descrição: " + e;
            Logger.error(e.getMessage());
            return badRequest(views.html.mensagens.usuario.mensagens.render(mensagem, tipoMensagem));
        }

    }

    /**
     * Remove a user from a id
     *
     * @param id identificador
     * @return ok user on json
     */
    @Security.Authenticated(SecuredAdmin.class)
    public Result remover(Long id) {

        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);

        //Necessario para verificar se o usuario e gerente
        if (usuarioAtual().isPresent()) {
            Usuario usuario = usuarioAtual().get();
            if (usuario.isGerente()) {
                return forbidden(views.html.mensagens.erro.naoAutorizado.render());
            }
        }

        //busca o usuario atual que esteja logado no sistema
        Usuario usuarioAtual = atual();

        //verifica se o usuario atual for nulo
        if (usuarioAtual == null) {
            tipoMensagem = "danger";
            mensagem = "Usuário não encontrado.";
            return badRequest(views.html.mensagens.usuario.mensagens.render(mensagem, tipoMensagem));
        }

        //verificar se o usuario atual encontrado e administrador
        if (!usuarioAtual.isAdmin()) {
            mensagem = "Você não tem privilégios de Administrador";
            tipoMensagem = "danger";
            return badRequest(views.html.mensagens.usuario.mensagens.render(mensagem, tipoMensagem));
        }

        try {
            //busca o usuario para ser excluido
            Usuario usuario = Ebean.find(Usuario.class, id);

            if (usuario == null) {
                return notFound(views.html.mensagens.erro.naoEncontrado.render("Usuário não encontrado"));
            }

            //verifica caso tente excluir o administrador do sistema
            if (usuario.getNome().equals("Administrador")) {
                mensagem = "Não excluir o administrador do sistema.";
                tipoMensagem = "danger";
                return badRequest(views.html.mensagens.usuario.mensagens.render(mensagem, tipoMensagem));
            }

            //caso o usuario administrador queira excluir a si proprio enquanto estiver autenticado
            if (usuarioAtual.getEmail().equals(usuario.getEmail())) {
                mensagem = "Não excluir seu próprio usuário enquanto ele estiver autenticado.";
                tipoMensagem = "danger";
                return badRequest(views.html.mensagens.usuario.mensagens.render(mensagem, tipoMensagem));
            }

            Ebean.delete(usuario);

            if (usuarioAtual().isPresent()) {
                formatter.format("Usuário Administrador: '%1s' excluiu o Usuário: '%2s'.", usuarioAtual().get().getEmail(), usuario.getEmail());
                logController.inserir(sb.toString());
            }

            tipoMensagem = "danger";
            mensagem = "Usuário '" + usuario.getNome() + "' excluído com sucesso.";
            return ok(views.html.mensagens.usuario.mensagens.render(mensagem, tipoMensagem));
        } catch (Exception e) {
            tipoMensagem = "danger";
            mensagem = "Erro interno de Sistema. Descrição: " + e;
            Logger.error(e.toString());
            return badRequest(views.html.mensagens.usuario.mensagens.render(mensagem, tipoMensagem));
        }

    }
}

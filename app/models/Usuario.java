package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.PagedList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.format.Formats;
import play.libs.Json;
import validators.UsuarioAdminFormData;

import javax.persistence.*;
import java.util.*;

@Entity
public class Usuario extends Model {

    /*-------------------------------------------------------------------
     *				 		     ATTRIBUTES
     *-------------------------------------------------------------------*/

    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false, unique = true, length = 60)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String senha;

    //Declarar o papel padrão do usuário que é o usuario
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private Papel papel;

    @Column(nullable = false)
    @JsonIgnore
    private Boolean status;

    @Formats.DateTime(pattern="YYYY-MM-DD")
    @Temporal(TemporalType.DATE)
    @JsonIgnore
    private Date dataCadastro;

    @Formats.DateTime(pattern="YYYY-MM-DD")
    @Temporal(TemporalType.DATE)
    @JsonIgnore
    private Date dataAlteracao;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar ultimoAcesso;

    /*-------------------------------------------------------------------
     *				 		     CONSTRUCTORS
     *-------------------------------------------------------------------*/

    public Usuario() {}

    public Usuario(Long id, String nome, String email, String senha, Boolean status) {
        this.setId(id);
        this.setNome(nome);
        this.setEmail(email);
        this.setSenha(senha);
        this.setPapel(Papel.USUARIO);
        this.setStatus(status);
    }

    //Instancia um Formulario de cadastro de usuario via administrador
    public static Usuario makeInstance(UsuarioAdminFormData formData) {
        Usuario usuario = new Usuario();
        usuario.setNome(formData.nome);
        usuario.setEmail(formData.email);
        usuario.setSenha(formData.senha);
        usuario.setPapel(Papel.valueOf(formData.papel));
        return usuario;
    }

    public static UsuarioAdminFormData makeUsuarioAdminFormData(Long id) {

        Usuario usuario = Ebean.find(Usuario.class, id);

        if (usuario == null) {
            throw new RuntimeException("Objeto não encontrado");
        }

        return new UsuarioAdminFormData(usuario.nome, usuario.email, usuario.senha, usuario.papel);
    }


    /*-------------------------------------------------------------------
     *				 		   GETTERS AND SETTERS
     *-------------------------------------------------------------------*/

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Papel getPapel() {
        return papel;
    }

    public void setPapel(Papel papel) {
        this.papel = papel;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Date getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Date dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public Date getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(Date dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

    public Calendar getUltimoAcesso() {
        return ultimoAcesso;
    }

    public void setUltimoAcesso(Calendar ultimoAcesso) {
        this.ultimoAcesso = ultimoAcesso;
    }

    /*-------------------------------------------------------------------
     *				 		   UTILS
     *-------------------------------------------------------------------*/

    @JsonIgnore
    public boolean isAdmin() {
        return papel == Papel.ADMINISTRADOR;
    }

    @JsonIgnore
    public boolean isSupervisor() {
        return papel == Papel.SUPERVISOR;
    }

    @JsonIgnore
    public boolean isGerente() {
        return papel == Papel.GERENTE;
    }

    @JsonIgnore
    public boolean isUsuario() {
        return papel == Papel.USUARIO;
    }

    @JsonIgnore
    public boolean isAtivo() {
        return status;
    }

    /**
     * Provide a list of names for use in form display.
     * @return A list of level names in sorted order.
     */
    public static List<String> getListaPapeis() {
        String[] nameArray = {"USUARIO", "SUPERVISOR", "GERENTE", "ADMINISTRADOR"};
        return Arrays.asList(nameArray);
    }

    public static Map<String,String> options() {
        LinkedHashMap<String,String> options = new LinkedHashMap<>();
        for (Usuario c : Usuario.find.orderBy("nome").findList()) {
            options.put(c.id.toString(),c.nome);
        }
        return options;
    }

    @Override
    public String toString() {
        return Json.toJson(this).toString();
    }

    /*-------------------------------------------------------------------
     *				 		   FINDERS
     *-------------------------------------------------------------------*/

    public static Finder<Long, Usuario> find = new Finder<>(Usuario.class);

    /**
     * Return a page of usuario
     *
     * @param page Page to display
     * @param pageSize Number of usuario per page
     * @param sortBy usuario nome property used for sorting
     * @param order Sort order (either or asc or desc)
     * @param filter Filter applied on the name column
     */
    public static PagedList<Usuario> page(int page, int pageSize, String sortBy, String order, String filter) {
        return
                find.where()
                        .ilike("nome", "%" + filter + "%")
                        .orderBy(sortBy + " " + order)
                        .findPagedList(page, pageSize);
    }

    /**
     * Return a list of last usuarios created
     *
     */
    public static List<Usuario> last() {
        return find.where().orderBy("dataCadastro desc").setMaxRows(5).findList();
    }

}

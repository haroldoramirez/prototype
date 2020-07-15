package models;

public enum Papel {

    USUARIO(1), SUPERVISOR(2), GERENTE(3), ADMINISTRADOR(4);

    public int valorPapel;

    Papel(int valor) {
        valorPapel = valor;
    }
}

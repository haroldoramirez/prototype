/*-INICIO - currency - Funcao para formatar valores monetarios---------------------------------*/
function formatCurrencyBR (n, c, d, t) {
    let s, j, i;
    c = isNaN(c = Math.abs(c)) ? 2 : c, d = d == undefined ? "," : d, t = t == undefined ? "." : t, s = n < 0 ? "-" : "", i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + "", j = (j = i.length) > 3 ? j % 3 : 0;
    return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : "");
}
function currencyFormatDE (num) {
    return num
       .toFixed(0) // always two decimal digits
       .replace(".", ",") // replace decimal point character with ,
       .replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1.")// use . as a separator
}
/*-FIM - currency - Funcao para formatar valores monetarios------------------------------------*/

/*-INICIO - Funcao para formatar valores-------------------------------------------------------*/
function formatarCampoCpfCnpj(campoTexto) {

    if (campoTexto.length <= 11) {

        return campoTexto.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/g,"\$1.\$2.\$3\-\$4");

    } else {

        return campoTexto.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/g,"\$1.\$2.\$3\/\$4\-\$5");

    }

}
function formatarCep(texto) {
    return texto.substring(0, 5) + "-" + texto.substring(5, 9);
}
/*-FIM - Funcao para formatar valores----------------------------------------------------------*/
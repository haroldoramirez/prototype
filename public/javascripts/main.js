//Botao de concluir o calculo
demo = {

    //Mensagem que aparece ao clickar no botao finalizar
    showSwal: function(type) {

        if (type === 'success-message') {
            swal({
                title: "Obrigado",
                text: "Engenhasol Calculator",
                buttonsStyling: false,
                confirmButtonClass: "btn btn-lg btn-success btn-fill",
                type: "success"
            });

        }
    }

}

//Instancia da data de nascimento
$('.datepicker').datetimepicker({
    locale: 'pt-br',
    format: 'YYYY-MM-DD',    //use this format if you want the 12hours timpiecker with AM/PM toggle
    icons: {
        time: "fa fa-clock-o",
        date: "fa fa-calendar",
        up: "fa fa-chevron-up",
        down: "fa fa-chevron-down",
        previous: 'fa fa-chevron-left',
        next: 'fa fa-chevron-right',
        today: 'fa fa-screenshot',
        clear: 'fa fa-trash',
        close: 'fa fa-remove'
    }
});
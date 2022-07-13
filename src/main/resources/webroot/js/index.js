
$(function () {
    $("form input").on('keypress', ev => {
        if ((ev.which && ev.which === 13) || (ev.keyCode && ev.keyCode === 13)) {
            ev.preventDefault();
        }
    });
    $('form').on('submit', event => {
        event.preventDefault()
        event.stopPropagation()
    });

    $("#ncccs-form input").prop("selected", false);
    $("#input-date").val(getCurrentDate());
});



$(function () {
    $("#btn-answers-submit").on('click', () => {
        for (let i = 0; i < $('#ncccs-form select').length; i++) {
            if (!$('#ncccs-form select')[i].checkValidity()) {
                $('#ncccs-form .submit-for-validation').trigger("click");
                return;
            }
        }

        sendAnswers($("#input-user-id").val(), $("#input-date").val());

        function sendAnswers(userId, inputDate) {
            const answers = $(".ncccs-select").map((i, elem) => {
                const ret =
                {
                    userId: userId,
                    inputDate: inputDate,
                    questionNumber: i + 1,
                    questionValue: $(elem).val()
                };
                return ret;
            }).get();

            new JsonRpcClient(new JsonRpcRequest(getJsonNcccsRpcUrl(), "sendAnswers",
                [userId, answers], () => {
                    swalAlert("回答を登録しました", "", "success", e => $("#pills-result-tab").trigger('click'));
                }, (data, textStatus, errorThrown) => swalAlert("エラー", "データの送信に失敗しました", "error", e => { }))).rpc()
        }

    });

});



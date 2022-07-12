let myChart;
$(function () {
    myChart = new Chart(document.getElementById('ncccs-total-chart'), {
        type: 'line',
        data: {},
        options: {
            y: {
                min: 0,
                max: 10,
            },
        },
    });
    $("#pills-result-tab").on('shown.bs.tab', e => {
        drawChart("user1");
    });

    function drawChart(userId) {
        new JsonRpcClient(new JsonRpcRequest(getJsonRpcUrl(), "getChartData",
            [userId], data => {
                const json = data.result;
                console.log(json);
                myChart.data = json;
                myChart.update();
            }, (data, textStatus, errorThrown) => swalAlert("エラー", "データの送信に失敗しました", "error", e => { }))).rpc()

    }
});



var connected = false;
var xhr;

$("#btnPost").click(function() {

    $.post( "update", $("#textToPost").val(), function(data) {
        console.log("post sent");

        $("#postStatus").html(messageSentTemplate(data))
        $("#textToPost")
            .val('')
            .focus()
    });
});

function messageSentTemplate(responseData) {
    return '<span class="glyphicon glyphicon-circle-arrow-up">&nbsp;'
             + $("#textToPost").val()
             + '</span>&nbsp;&nbsp;'
             + '<span class="glyphicon glyphicon-circle-arrow-down">&nbsp;'
             + responseData
             + '</span>'
}

$("#btnConnect").click(function(){
    if (connected) {
        xhr.abort();
        connectionChanged(false)
        console.log("Client Disconnected");
    }
    else {
        reconnect();
        connectionChanged(true)
    }
});

function connectionChanged(status) {
    connected = status;
    $("#btnConnect").text(status ? 'Disconnect' : 'Connect');
    $("#btnConnect")
        .removeClass('btn-' + (status ? 'success' : 'danger'))
        .addClass('btn-' + (status ? 'danger' : 'success'));
}

function reconnect() {
    console.log("Connecting to Server...");
    $('#status').text("Connecting...");

    xhr = new XMLHttpRequest();
    xhr.open('GET', 'part2.html', true);

    var nextLine = 0;
    console.log("opened");

    xhr.onreadystatechange = function () {
        console.log("onreadystatechange");

        //readyState: headers received 2, body received 3, done 4
        if (xhr.readyState != 2 && xhr.readyState != 3 && xhr.readyState != 4)
            return;
        if (xhr.readyState == 3 && xhr.status != 200)
            return;
        if (xhr.status == 200) {
            if (xhr.readyState == 2) {
                console.log("Connected...");
                $('#status').text("Connected");
            }
            if (xhr.readyState == 3) {
                $("#data").prepend(pushedMessageTemplate(xhr.response.slice(nextLine)));
                nextLine = xhr.response.length;
            }
        }
    }

    function pushedMessageTemplate(message) {
        return '<li class="list-group-item"><b>' + currentTimeStamp() + '</b><pre>' + message + '</pre></li>'
    }

    function currentTimeStamp() { return new Date()t.toLocaleTimeString() }

    xhr.onload = function () {
        // server completed the request.
        console.log("onload");
        //reconnect();

        connectionChanged(false)

        console.log("Server disconnected");
        $('#status').text("Disconnected");
    }

    xhr.onerror = function () {
        //console.log("reconnecting after error...");
        //reconnect();

        //server closed. Handle reconnects...
        console.log("Server disconnected in a graceless manner");
        $('#status').text("Disconnected");
        connected = false;
        $("#btnConnect").prop('value', 'Connect');
    }

    xhr.onabort = function () {
        //console.log("reconnecting after abort...");
        //reconnect();

        //client aborted the operation.
        console.log("aborted");
        $('#status').text("Disconnected");
    }

    console.log("sending");
    xhr.send();
    console.log("sent");
};
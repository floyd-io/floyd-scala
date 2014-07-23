var connected = false;
var xhr;

$("#btnPost").click(function() {
    $.post("update", $("#textToPost").val(), function(data) {
        console.log("post sent");

        $("#postStatus").html(messageSentTemplate(data))
        $("#textToPost")
            .val('')
            .focus()
    });
});

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

function connectionStatus(newStatus) {
    $('#status').text(newStatus)
}

function messageSentTemplate(responseData) {
    return '<span class="glyphicon glyphicon-circle-arrow-up">&nbsp;'
             + $("#textToPost").val()
             + '</span><br/><br/>'
             + '<span class="glyphicon glyphicon-circle-arrow-down">&nbsp;'
             + responseData
             + '</span>'
}

function connectionChanged(isConnected) {
    connected = isConnected;
    connectionStatus(isConnected ? "Connected" : "Disconnected")
    $("#btnConnect").text(isConnected ? 'Disconnect' : 'Connect');
    $("#btnConnect")
        .removeClass('btn-' + (isConnected ? 'success' : 'danger'))
        .addClass('btn-' + (isConnected ? 'danger' : 'success'));

    $("#textToPost").prop('disabled', !isConnected);
    $("#btnPost").prop('disabled', !isConnected);
}

function reconnect() {
    console.log("Connecting to Server...");
    connectionStatus("Connecting...")

    xhr = new XMLHttpRequest();
    var nextLine = 0;

    xhr.onreadystatechange = function () {
        console.log("onreadystatechange");

        //readyState: headers received 2, body received 3, done 4
        if (xhr.readyState != 2 && xhr.readyState != 3 && xhr.readyState != 4)
            return;
        if (xhr.readyState == 3 && xhr.status != 200) {
            connectionStatus("Error: " + xhr.status)
            return;
        }
        if (xhr.status == 200) {
            if (xhr.readyState == 2) {
                console.log("Connected...");
                connectionStatus("Connected");
            }
            if (xhr.readyState == 3) {
                pushMessageArrived(xhr.response.slice(nextLine))
                nextLine = xhr.response.length;
            }
        }
    }

    function pushMessageArrived(pushMessage) {
       $("#data").prepend(pushedMessageTemplate(pushMessage));
    }

    xhr.onload = function () {
        // server completed the request.
        console.log("onload");
        //reconnect();

        connectionChanged(false)
        console.log("Server disconnected");
    }

    xhr.onerror = function () {
        //console.log("reconnecting after error...");
        //reconnect();

        //server closed. Handle reconnects...
        console.log("Server disconnected in a graceless manner");
        connectionChanged(false)
    }

    xhr.onabort = function () {
        //console.log("reconnecting after abort...");
        //reconnect();

        //client aborted the operation.
        console.log("aborted");
        connectionStatus("Disconnected");
    }

    xhr.open('GET', 'part2.html', true);
    console.log("opened");

    console.log("sending");
    xhr.send();
    console.log("sent");
};

function pushedMessageTemplate(message) {
   return '<li class="list-group-item li-animated"><b>' + currentTimeStamp() + '</b><pre>' + message + '</pre></li>';
}

function currentTimeStamp() {
    return new Date().toLocaleTimeString();
}
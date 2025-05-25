// Основной JS для Secure Messaging

if (document.getElementById('messageForm')) {
    let stompClient = null;
    let messageContainer = document.getElementById('messageContainer');
    let messageInput = document.getElementById('messageInput');
    let sendButton = document.getElementById('sendBtn');

    function connect() {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function(frame) {
            if (currentChatId && currentChatId !== 'null') {
                stompClient.subscribe('/topic/chat/' + currentChatId, function(response) {
                    const msg = JSON.parse(response.body);
                    displayMessage(msg);
                });
            }
        });
    }

    function sendMessage() {
        const content = messageInput.value.trim();
        if (content && stompClient) {
            stompClient.send('/app/chat/' + currentChatId, {}, content);
            messageInput.value = '';
        }
    }

    function displayMessage(msg) {
        const msgDiv = document.createElement('div');
        msgDiv.classList.add('message');
        if (msg.senderId === currentUserId) {
            msgDiv.classList.add('my-message');
        } else {
            msgDiv.classList.add('other-message');
        }
        if (msg.senderId !== currentUserId) {
            const nameSpan = document.createElement('span');
            nameSpan.classList.add('sender-name');
            nameSpan.textContent = msg.senderName + ': ';
            msgDiv.appendChild(nameSpan);
        }
        const textSpan = document.createElement('span');
        textSpan.textContent = msg.content;
        msgDiv.appendChild(textSpan);
        const timeSpan = document.createElement('span');
        timeSpan.classList.add('text-muted', 'small', 'ms-2');
        const date = new Date(msg.timestamp);
        timeSpan.textContent = date.toLocaleTimeString([], {hour: '2-digit', minute: '2-digit', hour12: false});
        msgDiv.appendChild(timeSpan);
        messageContainer.appendChild(msgDiv);
        messageContainer.scrollTop = messageContainer.scrollHeight;
    }

    // Подключаемся при загрузке
    connect();
    // Отправка при нажатии кнопки
    sendButton.addEventListener('click', function() {
        sendMessage();
    });
    // Отправка при нажатии Enter
    messageInput.addEventListener('keydown', function(event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            sendMessage();
        }
    });
    stompClient.connect({}, function(frame) {
        console.log("✅ Connected:", frame);
        if (currentChatId && currentChatId !== 'null') {
            stompClient.subscribe('/topic/chat/' + currentChatId, function(response) {
                const msg = JSON.parse(response.body);
                displayMessage(msg);
            });
        }
    }, function(error) {
        console.error("❌ STOMP connection error:", error);
    });
}


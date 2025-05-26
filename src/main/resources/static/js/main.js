document.addEventListener("DOMContentLoaded", () => {
    const messageForm = document.getElementById("messageForm");
    const messageInput = document.getElementById("messageInput");
    const sendButton = document.getElementById("sendBtn");
    const messageContainer = document.getElementById("messageContainer");

    if (!messageForm || !messageInput || !sendButton || !messageContainer) return;

    let stompClient = null;

    function connect() {
        const socket = new SockJS("/ws");
        stompClient = Stomp.over(socket);

        stompClient.connect({}, (frame) => {
            console.log("✅ Connected:", frame);
            if (currentChatId && currentChatId !== "null") {
                stompClient.subscribe("/topic/chat/" + currentChatId, (response) => {
                    const msg = JSON.parse(response.body);
                    displayMessage(msg);
                });
            }
        }, (error) => {
            console.error("❌ Connection error:", error);
        });
    }

    function sendMessage() {
        const content = messageInput.value.trim();
        if (!content || !stompClient || currentChatId === "null") return;

        stompClient.send("/app/chat/" + currentChatId, {}, content);
        messageInput.value = "";
    }

    function displayMessage(msg) {
        const msgDiv = document.createElement("div");
        msgDiv.classList.add("message");

        if (msg.senderId === currentUserId) {
            msgDiv.classList.add("my-message");
        } else {
            msgDiv.classList.add("other-message");
            const nameSpan = document.createElement("span");
            nameSpan.classList.add("sender-name");
            nameSpan.textContent = msg.senderName + ": ";
            msgDiv.appendChild(nameSpan);
        }

        const textSpan = document.createElement("span");
        textSpan.textContent = msg.content;
        msgDiv.appendChild(textSpan);

        const timeSpan = document.createElement("span");
        timeSpan.classList.add("text-muted", "small", "ms-2");
        timeSpan.textContent = new Date(msg.timestamp).toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit",
            hour12: false,
        });
        msgDiv.appendChild(timeSpan);

        messageContainer.appendChild(msgDiv);
        messageContainer.scrollTop = messageContainer.scrollHeight;
    }

    connect();

    sendButton.addEventListener("click", (e) => {
        e.preventDefault();
        sendMessage();
    });

    messageInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            sendMessage();
        }
    });
});

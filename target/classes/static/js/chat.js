// Establish WebSocket connection using SockJS and STOMP
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

// Get references to data passed from Thymeleaf
const chatInfoDiv = document.getElementById('chatInfo');
const currentChatId = chatInfoDiv.dataset.chatId;
const currentChatType = chatInfoDiv.dataset.chatType;
const currentUserIIN = chatInfoDiv.dataset.currentUser;

// Connect and set up subscriptions
stompClient.connect({}, function (frame) {
    console.log("Connected: " + frame);
    // Subscribe to personal queue for incoming private messages
    stompClient.subscribe('/user/queue/messages', function (message) {
        const msg = JSON.parse(message.body);
        // If message is for a different chat than the one currently open, we could handle notifications.
        if (!currentChatId || msg.chatId !== currentChatId) {
            console.log("New message for another chat:", msg);
            // TODO: highlight chat in sidebar or show notification (not implemented here)
            return;
        }
        displayMessage(msg);
    });
    // If current chat is a group, subscribe to its topic for group messages
    if (currentChatType === 'GROUP' && currentChatId) {
        stompClient.subscribe('/topic/chatroom/' + currentChatId, function (message) {
            const msg = JSON.parse(message.body);
            displayMessage(msg);
        });
    }
});

// Display a message in the chat window
function displayMessage(msg) {
    const messageArea = document.getElementById('messageArea');
    if (!messageArea) return;
    const messageDiv = document.createElement('div');
    let text;
    if (msg.sender === currentUserIIN) {
        // Message from current user
        text = "You: " + msg.content;
        messageDiv.className = "message my-message";
    } else {
        // Message from another user
        // Use senderName if available (else fallback to sender IIN)
        const name = msg.senderName || msg.sender;
        text = name + ": " + msg.content;
        messageDiv.className = "message their-message";
    }
    messageDiv.textContent = text;
    messageArea.appendChild(messageDiv);
    // Auto-scroll to bottom
    messageArea.scrollTop = messageArea.scrollHeight;
}

// Send a message (called when message form is submitted)
function sendMessage() {
    const input = document.getElementById('messageInput');
    if (!input || !input.value.trim()) {
        return;
    }
    const content = input.value.trim();
    // Send the message to the server endpoint
    stompClient.send("/app/chat", {}, JSON.stringify({chatId: currentChatId, content: content}));
    input.value = "";
}

// Mobile: sidebar toggle logic
const sidebar = document.getElementById('sidebar');
const toggleBtn = document.getElementById('toggleSidebar');
const closeBtn = document.getElementById('closeSidebar');
if (toggleBtn) {
    toggleBtn.addEventListener('click', () => {
        sidebar.classList.add('open');
    });
}
if (closeBtn) {
    closeBtn.addEventListener('click', () => {
        sidebar.classList.remove('open');
    });
}

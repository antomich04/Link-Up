const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendFriendRequestNotification = onDocumentCreated(
  "FriendRequests/{requestId}",
  async (event) => {
    const snapshot = event.data;
    const friendRequest = snapshot.data();
    const receiverRef = friendRequest.receiver;
    const senderRef = friendRequest.sender;

    if(!receiverRef || !senderRef){
        return;
    }

    const receiverDoc = await admin.firestore().collection("Users").doc(receiverRef.id).get();
    const senderDoc = await admin.firestore().collection("Users").doc(senderRef.id).get();
    const receiverTokens = receiverDoc.data()?.tokens || [];
    const senderTokens = senderDoc.data()?.tokens || [];

    //Filters out any tokens that belong to the sender
    const tokensToNotify = receiverTokens.filter(token => !senderTokens.includes(token));

    if(tokensToNotify.length === 0){
        return;
    }

    const payload = {
      data: {
        type: "friend_request",
        sender: senderRef.id,
        title: "New Friend Request",
        body: `${senderRef.id} has sent you a friend request!`,
        target: "FriendRequests"
      }
    };

    const messages = tokensToNotify.map(token => ({
      ...payload,
      token
    }));
    await Promise.all(messages.map(msg => admin.messaging().send(msg)));
  }
);

exports.sendFriendAcceptedNotification = onDocumentUpdated(
  "FriendRequests/{requestId}",
  async (event) => {
    const beforeData = event.data.before.data();
    const afterData = event.data.after.data();

    if(beforeData.status !== "accepted" && afterData.status === "accepted"){
      const senderRef = afterData.sender;
      const receiverRef = afterData.receiver;

      if(!senderRef || !receiverRef){
        return;
      }

      const senderDoc = await admin.firestore().collection("Users").doc(senderRef.id).get();
      const receiverDoc = await admin.firestore().collection("Users").doc(receiverRef.id).get();

      const senderTokens = senderDoc.data()?.tokens || [];
      const receiverTokens = receiverDoc.data()?.tokens || [];

      //Filters out tokens belonging to the receiver
      const tokensToNotify = senderTokens.filter(token => !receiverTokens.includes(token));

      if(tokensToNotify.length === 0){
        return;
      }

      const payload = {
        data: {
          type: "friend_accepted",
          receiver: receiverRef.id,
          title: "Friend Request Accepted",
          body: `${receiverRef.id} has accepted your friend request!`,
          target: "Friends"
        }
      };

      const messages = tokensToNotify.map(token => ({
        ...payload,
        token
      }));

      await Promise.all(messages.map(msg => admin.messaging().send(msg)));
    }
  }
);

exports.sendMessageNotification = onDocumentCreated(
  "Chats/{chatId}/messages/{messageId}",
  async (event) => {
    const snapshot = event.data;

    if(!snapshot){
        return;
    }

    const message = snapshot.data();
    const { sender, receiver, text, timestamp, migrated } = message;

    if(!sender || !receiver || !text){
        return;
    }

    if(migrated === true){
        return;
    }


    if(timestamp){
        const messageTime = timestamp.toDate ? timestamp.toDate() : new Date(timestamp._seconds * 1000);
        const now = new Date();
        const messageAgeMs = now - messageTime;

        if(messageAgeMs > 30000){  //30 seconds
            return;
        }
    }

    try{
        //Query for users by username field instead of document ID
        const senderQuery = await admin.firestore().collection("Users")
            .where("username", "==", sender)
            .limit(1)
            .get();

        const receiverQuery = await admin.firestore().collection("Users")
            .where("username", "==", receiver)
            .limit(1)
            .get();

        //Checks if users were found
        if(senderQuery.empty || receiverQuery.empty){
            return;
        }

        const senderData = senderQuery.docs[0].data();
        const receiverData = receiverQuery.docs[0].data();

        const senderTokens = senderData?.tokens || [];
        const receiverTokens = receiverData?.tokens || [];

        const tokensToNotify = receiverTokens.filter(token => !senderTokens.includes(token));

        if(tokensToNotify.length === 0){
          return;
        }

        const payload = {
          data: {
            type: "new_message",
            sender: String(sender),
            receiver: String(receiver),
            title: `New message from ${sender}`,
            body: text,
            target: "Chat"
          }
        };

        const messages = tokensToNotify.map(token => ({
          ...payload,
          token
        }));

        const sendResults = await Promise.all(
            messages.map(msg => admin.messaging().send(msg).catch(error => {
                return null;
            }))
        );

    }catch(error) {
        console.error('Error in sendMessageNotification function:', error);
    }
  }
);
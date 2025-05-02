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

    // Filter out any tokens that belong to the sender
    const tokensToNotify = receiverTokens.filter(token => !senderTokens.includes(token));

    if(tokensToNotify.length === 0){
        return;
    }

    const payload = {
      data: {
        type: "friend_request",
        sender: senderRef.id,
        title: "New Friend Request",
        body: `${senderRef.id} has sent you a friend request!`
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

      // Filter out tokens belonging to the receiver
      const tokensToNotify = senderTokens.filter(token => !receiverTokens.includes(token));

      if(tokensToNotify.length === 0){
        return;
      }

      const payload = {
        data: {
          type: "friend_accepted",
          receiver: receiverRef.id,
          title: "Friend Request Accepted",
          body: `${receiverRef.id} has accepted your friend request!`
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
const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { logger } = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendFriendRequestNotification = onDocumentCreated(
    "FriendRequests/{requestId}",
    async (event) => {
        const snapshot = event.data;
        const friendRequest = snapshot.data();
        const receiverRef = friendRequest.receiver;

        if(!receiverRef){
            return;
        }
        const receiverUsername = receiverRef.id;
        const userSnapshot = await admin.firestore()
            .collection("Users")
            .doc(receiverUsername)
            .get();

        const token = userSnapshot.data()?.token;

        if(token){
            const payload = {
                data: {
                    type: "friend_request",
                    sender: friendRequest.sender.id,
                    title: "New Friend Request",
                    body: `${friendRequest.sender.id} has sent you a friend request!`
                },
                token: token,
            };
            await admin.messaging().send(payload);
        }else{
            return;
        }
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

            const senderUsername = senderRef.id;
            const receiverUsername = receiverRef.id;

            const userSnapshot = await admin.firestore()
                .collection("Users")
                .doc(senderUsername)
                .get();

            const token = userSnapshot.data()?.token;

            if(token){
                const payload = {
                    data: {
                        type: "friend_accepted",
                        receiver: receiverUsername,
                        title: "Friend Request Accepted",
                        body: `${receiverUsername} has accepted your friend request!`
                    },
                    token: token,
                };
                await admin.messaging().send(payload);
            }else{
                return;
            }
        }
    }
);
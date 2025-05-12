package com.example.linkup.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.linkup.R
import com.example.linkup.activities.adapters.BottomSheetFriendsAdapter
import com.example.linkup.activities.adapters.ChatsAdapter
import com.example.linkup.activities.firestoreDB.ChatViewModel
import com.example.linkup.activities.firestoreDB.Client
import com.example.linkup.activities.firestoreDB.Friendship
import com.example.linkup.activities.notifications.NotificationsHandler
import com.example.linkup.activities.roomDB.LocalDatabase
import com.example.linkup.activities.roomDB.UserViewModel
import com.example.linkup.activities.roomDB.UserViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class HomePage : Fragment() {
    private lateinit var rvChats : RecyclerView
    private lateinit var messageFriendBtn : FloatingActionButton
    private lateinit var bottomSheetFriends : BottomSheetDialog
    private lateinit var friendsContainer : RecyclerView
    private lateinit var client: Client
    private lateinit var friendsList: List<Friendship>
    private lateinit var userViewModel: UserViewModel
    private lateinit var loggedinUsername: String
    private lateinit var chatsAdapter: ChatsAdapter
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var notificationsHandler: NotificationsHandler

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.home_page, container, false)
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setTitle("CHATS")

        val database = LocalDatabase.getDB(requireContext())
        val userDao = database.userDao()
        val factory = UserViewModelFactory(userDao)
        userViewModel = factory.create(UserViewModel::class.java)
        client = Client()

        notificationsHandler = NotificationsHandler(requireContext())

        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        rvChats = view.findViewById(R.id.rvChats)
        rvChats.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            val loggedinUser = userViewModel.getLoggedInUser()
            loggedinUsername = loggedinUser!!.username

            chatsAdapter = ChatsAdapter(emptyList(), loggedinUsername, parentFragmentManager)
            rvChats.adapter = chatsAdapter

            //Observes chats from ViewModel and updates adapter
            chatViewModel.chats.observe(viewLifecycleOwner) { chatList ->
                chatsAdapter.updateData(chatList)
            }

            client.getFriendsList(loggedinUsername).observe(viewLifecycleOwner) { fetchedFriends ->
                friendsList = fetchedFriends

                if(::friendsContainer.isInitialized && friendsContainer.adapter != null){
                    (friendsContainer.adapter as BottomSheetFriendsAdapter).updateData(friendsList)
                }

                //Clears previous chats
                chatViewModel.clearChats()

                //Setups listener for each friend's chat
                fetchedFriends.forEach { friendship ->
                    val friendId = if(friendship.friendUsername!!.id == loggedinUsername) {
                        friendship.userUsername!!.id
                    } else {
                        friendship.friendUsername!!.id
                    }

                    client.getChats(loggedinUsername, friendId).observe(viewLifecycleOwner) { chatList ->
                        if (chatList.isNotEmpty()) {
                            chatViewModel.addOrUpdateChat(chatList[0])
                        }
                    }
                }
            }
        }

        messageFriendBtn = view.findViewById(R.id.messageFriendBtn)
        messageFriendBtn.setOnClickListener{
            val orientation = requireContext().resources.configuration.orientation

            bottomSheetFriends = BottomSheetDialog(requireContext())
            bottomSheetFriends.behavior.state = if(orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE){
                BottomSheetBehavior.STATE_HALF_EXPANDED
            }else{
                BottomSheetBehavior.STATE_EXPANDED
            }

            bottomSheetFriends.behavior.maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
            bottomSheetFriends.setContentView(R.layout.bottom_sheet_friends)

            friendsContainer = bottomSheetFriends.findViewById(R.id.rvContainer)!!
            friendsContainer.layoutManager = LinearLayoutManager(requireContext())
            if(::friendsList.isInitialized){
                friendsContainer.adapter = BottomSheetFriendsAdapter(friendsList, loggedinUsername, bottomSheetFriends, parentFragmentManager, client)
            }

            bottomSheetFriends.show()
        }

        return view
    }
}
package com.example.goalhabitapp.navigation

object Routes {
    const val Splash = "splash"
    const val Login = "login"
    const val Register = "register"
    const val Home = "home"
    const val Templates = "templates"
    const val Habits = "habits"
    const val CreateHabit = "create_habit"
    const val Goals = "goals"
    const val CreateGoal = "create_goal"

    const val Profile = "profile"

    const val Friends = "friends"
    const val FriendProfile = "friend_profile/{id}"
    fun friendProfile(id: Int) = "friend_profile/$id"


}

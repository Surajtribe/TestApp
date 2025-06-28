package com.test.emp.presentation.navigation


sealed class Screens(val route:String){

    object SplashScreen : Screens("SplashScreen")
    object RandomTextScreen : Screens("randomTextScreen")

}



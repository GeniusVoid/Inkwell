package com.example.data.backup

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleDriveAuthHelper(private val context: Context) {

  private val driveFileScope = Scope("https://www.googleapis.com/auth/drive.file")
  private val driveAppDataScope = Scope("https://www.googleapis.com/auth/drive.appdata")

  private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
    .requestEmail()
    .requestProfile()
    .requestScopes(driveFileScope, driveAppDataScope)
    .build()

  val signInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

  fun getSignedInAccount(): GoogleSignInAccount? {
    val account = GoogleSignIn.getLastSignedInAccount(context)
    return if (account != null && GoogleSignIn.hasPermissions(account, driveFileScope, driveAppDataScope)) {
      account
    } else {
      null
    }
  }

  fun getSignInIntent(): Intent {
    return signInClient.signInIntent
  }

  suspend fun fetchAccessToken(account: GoogleSignInAccount): String? = withContext(Dispatchers.IO) {
    try {
      val androidAccount = account.account ?: return@withContext null
      val scopeString = "oauth2:https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/drive.appdata"
      GoogleAuthUtil.getToken(context, androidAccount, scopeString)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun signOut(onComplete: () -> Unit) {
    signInClient.signOut().addOnCompleteListener {
      onComplete()
    }
  }
}

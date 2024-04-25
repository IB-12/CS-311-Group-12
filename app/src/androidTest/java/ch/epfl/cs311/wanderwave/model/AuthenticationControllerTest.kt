package ch.epfl.cs311.wanderwave.model

import android.net.Uri
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationUserData
import ch.epfl.cs311.wanderwave.model.repository.AuthTokenRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import io.mockk.called
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AuthenticationControllerTest {

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockFirebaseAuth: FirebaseAuth

  @RelaxedMockK private lateinit var mockHttpClient: OkHttpClient

  @RelaxedMockK private lateinit var mockTokenRepository: AuthTokenRepository

  private lateinit var authenticationController: AuthenticationController

  private val dummyUser =
      AuthenticationUserData(
          "testid", "testemail", "testDisplayName", "https://example.com/testphoto.jpg")

  @Before
  fun setup() {
    authenticationController =
        AuthenticationController(mockFirebaseAuth, mockHttpClient, mockTokenRepository)
  }

  fun setupDummyUserSignedIn() {
    every { mockFirebaseAuth.currentUser } returns
        mockk {
          every { uid } returns dummyUser.id
          every { email } returns dummyUser.email
          every { displayName } returns dummyUser.displayName
          every { photoUrl } returns Uri.parse(dummyUser.photoUrl)
        }
    val call = mockk<Call>()
    every { mockHttpClient.newCall(any()) } returns call
    every { call.execute() } returns
        mockk {
          every { body } returns
              mockk {
                every { string() } returns
                    """
            {
              "firebase_token": "testtoken-firebase",
              "access_token": "testtoken-spotify-access",
              "refresh_token": "testtoken-spotify-refresh"
            }
            """
                        .trimIndent()
              }
        }
    every { mockTokenRepository.setAuthToken(any(), any(), any()) } returns Unit
    every {
      mockTokenRepository.getAuthToken(AuthTokenRepository.AuthTokenType.FIREBASE_TOKEN)
    } returns "testtoken-firebase"
    every {
      mockTokenRepository.getAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN)
    } returns "testtoken-spotify-access"
    every {
      mockTokenRepository.getAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_REFRESH_TOKEN)
    } returns "testtoken-spotify-refresh"
  }

  @Test
  fun canGetData() = runBlocking {
    setupDummyUserSignedIn()

    val userData = authenticationController.getUserData()
    assert(userData == dummyUser)

    assert(authenticationController.isSignedIn())
  }

  @Test
  fun canSignOut() = runBlocking {
    every { mockFirebaseAuth.signOut() } returns Unit
    authenticationController.deauthenticate()
    verify { mockFirebaseAuth.signOut() }
  }

  @Test
  fun alreadySignedIn() = runBlocking {
    setupDummyUserSignedIn()
    val result = authenticationController.authenticate("testcode").first()
    assert(result)

    verify { mockFirebaseAuth.signInWithCustomToken(any()) wasNot called }
  }

  @Test
  fun signInWithToken() = runBlocking {
    setupDummyUserSignedIn()
    val mockFirebaseUser =
        mockk<com.google.firebase.auth.FirebaseUser> {
          every { uid } returns "testid"
          every { email } returns null
          every { displayName } returns null
          every { photoUrl } returns null
        }

    val task =
        mockk<Task<AuthResult>> {
          every { result } returns mockk { every { user } returns mockFirebaseUser }
          every { isComplete } returns true
          every { isSuccessful } returns true
          every { isCanceled } returns false
          every { exception } returns null
        }

    every { mockFirebaseAuth.currentUser } returns null
    every { mockFirebaseAuth.signInWithCustomToken("testtoken-firebase") } returns task

    val result = authenticationController.authenticate("testcode").first()
    verify { mockFirebaseAuth.signInWithCustomToken("testtoken-firebase") }
    assert(result)
  }

  @Test
  fun useRefreshToken() = runBlocking {
    setupDummyUserSignedIn()
    val mockFirebaseUser =
        mockk<com.google.firebase.auth.FirebaseUser> {
          every { uid } returns "testid"
          every { email } returns null
          every { displayName } returns null
          every { photoUrl } returns null
        }

    val task =
        mockk<Task<AuthResult>> {
          every { result } returns mockk { every { user } returns mockFirebaseUser }
          every { isComplete } returns true
          every { isSuccessful } returns true
          every { isCanceled } returns false
          every { exception } returns null
        }

    every { mockFirebaseAuth.currentUser } returns null
    every { mockFirebaseAuth.signInWithCustomToken("testtoken-firebase") } returns task

    val result = authenticationController.refreshTokenIfNecessary()
    verify { mockFirebaseAuth.signInWithCustomToken("testtoken-firebase") }
    assert(result)
  }
}
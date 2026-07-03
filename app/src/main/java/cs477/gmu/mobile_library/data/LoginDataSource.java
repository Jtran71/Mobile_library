package cs477.gmu.mobile_library.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import cs477.gmu.mobile_library.data.model.LoggedInUser;

import java.io.IOException;

public class LoginDataSource {

    private FirebaseAuth mAuth;

    public LoginDataSource() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void login(String email, String password, LoginCallback callback) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            callback.onResult(new Result.Error(new IOException("Email and password cannot be empty")));
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            LoggedInUser loggedInUser = new LoggedInUser(
                                    user.getUid(),
                                    user.getEmail() != null ? user.getEmail() : "User"
                            );
                            callback.onResult(new Result.Success<>(loggedInUser));
                        } else {
                            callback.onResult(new Result.Error(new IOException("User not found")));
                        }
                    } else {
                        String errorMessage = task.getException() != null 
                                ? task.getException().getMessage() 
                                : "Login failed";
                        callback.onResult(new Result.Error(new IOException(errorMessage)));
                    }
                });
    }

    public void signup(String email, String password, LoginCallback callback) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            callback.onResult(new Result.Error(new IOException("Email and password cannot be empty")));
            return;
        }

        if (password.length() < 6) {
            callback.onResult(new Result.Error(new IOException("Password must be at least 6 characters")));
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            LoggedInUser loggedInUser = new LoggedInUser(
                                    user.getUid(),
                                    user.getEmail() != null ? user.getEmail() : "User"
                            );
                            callback.onResult(new Result.Success<>(loggedInUser));
                        } else {
                            callback.onResult(new Result.Error(new IOException("User creation failed")));
                        }
                    } else {
                        String errorMessage = task.getException() != null 
                                ? task.getException().getMessage() 
                                : "Signup failed";
                        callback.onResult(new Result.Error(new IOException(errorMessage)));
                    }
                });
    }

    public void logout() {
        mAuth.signOut();
    }

    public boolean isLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public LoggedInUser getCurrentUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            return new LoggedInUser(user.getUid(), user.getEmail() != null ? user.getEmail() : "User");
        }
        return null;
    }

    public interface LoginCallback {
        void onResult(Result<LoggedInUser> result);
    }
}
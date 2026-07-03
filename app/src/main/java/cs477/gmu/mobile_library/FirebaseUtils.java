package cs477.gmu.mobile_library;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import cs477.gmu.mobile_library.data.LoginDataSource;
import cs477.gmu.mobile_library.data.LoginRepository;
import cs477.gmu.mobile_library.data.model.LoggedInUser;

public class FirebaseUtils {

    public static DatabaseReference getBooksReference() {
        LoginRepository loginRepository = LoginRepository.getInstance(new LoginDataSource());
        LoggedInUser user = loginRepository.getLoggedInUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("books");
        return user != null ? ref.child(user.getUserId()) : ref;
    }
}


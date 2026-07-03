package cs477.gmu.mobile_library.data;

import cs477.gmu.mobile_library.data.model.LoggedInUser;

public class LoginRepository {

    private static volatile LoginRepository instance;

    private LoginDataSource dataSource;

    private LoggedInUser user = null;

    private LoginRepository(LoginDataSource dataSource) {
        this.dataSource = dataSource;
        this.user = dataSource.getCurrentUser();
    }

    public static LoginRepository getInstance(LoginDataSource dataSource) {
        if (instance == null) {
            instance = new LoginRepository(dataSource);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return dataSource.isLoggedIn();
    }

    public LoggedInUser getLoggedInUser() {
        if (user == null) {
            user = dataSource.getCurrentUser();
        }
        return user;
    }

    public void logout() {
        user = null;
        dataSource.logout();
    }

    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
    }

    public void login(String email, String password, LoginDataSource.LoginCallback callback) {
        dataSource.login(email, password, result -> {
            if (result instanceof Result.Success) {
                setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
            }
            callback.onResult(result);
        });
    }

    public void signup(String email, String password, LoginDataSource.LoginCallback callback) {
        dataSource.signup(email, password, result -> {
            if (result instanceof Result.Success) {
                setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
            }
            callback.onResult(result);
        });
    }
}
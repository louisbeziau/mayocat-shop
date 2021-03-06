package org.mayocat.authorization.cookies;

import javax.inject.Inject;
import javax.inject.Provider;

import org.mayocat.accounts.store.UserStore;
import org.mayocat.authorization.Authenticator;
import org.mayocat.authorization.PasswordManager;
import org.mayocat.accounts.model.Tenant;
import org.mayocat.accounts.model.User;
import org.mayocat.accounts.store.UserStore;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

@Component("cookie")
public class CookieAuthenticator implements Authenticator
{
    @Inject
    private Provider<UserStore> userStore;

    @Inject
    private PasswordManager passwordManager;

    @Inject
    private CookieCrypter crypter;

    @Inject
    private Logger logger;

    public boolean respondTo(String headerName, String headerValue)
    {
        if (headerName.equals("Cookie")) {
            String[] cookies = headerValue.split(";");
            for (String cookie : cookies) {
                if (cookie.indexOf("=") > 0) {
                    String name = cookie.split("=")[0];
                    if (name.trim().equals("username")) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public Optional<User> verify(String value, Tenant tenant)
    {
        try {
            String username = null;
            String password = null;
            String[] cookies = value.split(";");
            for (String cookie : cookies) {
                if (cookie.indexOf("=") > 0) {
                    String name = cookie.split("=")[0];
                    String val = cookie.split("=")[1];
                    if (name.trim().equals("username")) {
                        username = this.crypter.decrypt(val);
                    } else if (name.trim().equals("password")) {
                        password = this.crypter.decrypt(val);
                    }
                }
            }
            if (!Strings.isNullOrEmpty(password) && !Strings.isNullOrEmpty(username)) {
                User user = userStore.get().findUserByEmailOrUserName(username);
                if (user != null) {
                    if (this.passwordManager.verifyPassword(password, user.getPassword())) {
                        return Optional.of(user);
                    }
                }
            }
            return Optional.absent();
        } catch (EncryptionException e) {
            this.logger.error("Failed to decrypt cookies", e);
            return Optional.absent();
        }
    }

}

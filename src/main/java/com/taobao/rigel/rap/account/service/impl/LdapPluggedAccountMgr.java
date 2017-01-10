package com.taobao.rigel.rap.account.service.impl;

import com.taobao.rigel.rap.account.bo.Notification;
import com.taobao.rigel.rap.account.bo.User;
import com.taobao.rigel.rap.account.service.AccountMgr;
import com.taobao.rigel.rap.organization.bo.Corporation;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by ian on 2017/1/10.
 *
 * @author ian
 * @since 2017/01/10 15:44
 */
public class LdapPluggedAccountMgr implements AccountMgr {
    private AccountMgr delegate;
    private String serverUrl;
    private String baseDn;
    private String userSearchFilter = "(&(uid={0})(gidNumber=10000))";

    public void setServerUrl(String url) {
        serverUrl = url;
    }

    public void setUserSearchFilter(String userSearchFilter) {
        this.userSearchFilter = userSearchFilter;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public AccountMgr getDelegate() {
        return delegate;
    }

    public void setDelegate(AccountMgr mgr) {
        delegate = mgr;
    }


    public boolean validate(String account, String password) {

        boolean ret = getDelegate().validate(account, password);
        if (ret) {
            return true;
        }

        User user = searchFromLdap(account, password);
        if (user == null) {
            return false;
        }
        // 插入新用户
        User exists = getUser(account);
        if (exists != null) {
            exists.setEmail(user.getEmail());
            exists.setName(user.getName());
            exists.setEmpId(user.getEmpId());
            exists.setLastLoginDate(new Date());
            updateUser(user);
            return true;
        }

        addUser(user);
        user.setPassword("RESERVED");
        // 更新密码
        updateUser(user);
        return true;
    }

    public boolean addUser(User user) {
        return getDelegate().addUser(user);
    }

    public User getUser(int userId) {
        return getDelegate().getUser(userId);
    }

    public List<User> getUserList() {
        return getDelegate().getUserList();
    }

    public List<User> getUserList(int teamId) {
        return getDelegate().getUserList(teamId);
    }

    public User getUser(String account) {
        return getDelegate().getUser(account);
    }

    public boolean changePassword(String account, String oldPassword, String newPassword) {
        return getDelegate().changePassword(account, oldPassword, newPassword);
    }

    public int getUserId(String account) {
        return getDelegate().getUserId(account);
    }

    public void changeProfile(int userId, String profileProperty, String profileValue) {
        getDelegate().changeProfile(userId, profileProperty, profileValue);
    }

    public boolean updateProfile(int userId, String name, String email, String password, String newPassword) {
        return getDelegate().updateProfile(userId, name, email, password, newPassword);
    }

    public void _updatePassword(String account, String password) {
        getDelegate()._updatePassword(account, password);
    }

    public List<Corporation> getCorporationList() {
        return getDelegate().getCorporationList();
    }

    public List<Corporation> getCorporationListWithPager(int userId, int pageNum, int pageSize) {
        return getDelegate().getCorporationListWithPager(userId, pageNum, pageSize);
    }

    public User getUserByName(String name) {
        return getDelegate().getUserByName(name);
    }

    public Map<String, String> getUserSettings(int userId) {
        return getDelegate().getUserSettings(userId);
    }

    public String getUserSetting(int userId, String key) {
        return getDelegate().getUserSetting(userId, key);
    }

    public void updateUserSetting(int userId, String key, String value) {
        getDelegate().updateUserSetting(userId, key, value);
    }

    public List<Notification> getNotificationList(int userId) {
        return getDelegate().getNotificationList(userId);
    }

    public void clearNotificationList(int userId) {
        getDelegate().clearNotificationList(userId);
    }

    public void addNotification(Notification notification) {
        getDelegate().addNotification(notification);
    }

    public void readNotification(int id) {
        getDelegate().readNotification(id);
    }

    public void readNotificationList(int userId) {
        getDelegate().readNotificationList(userId);
    }

    public List<Notification> getUnreadNotificationList(int curUserId) {
        return getDelegate().getUnreadNotificationList(curUserId);
    }

    public int getUserNum() {
        return getDelegate().getUserNum();
    }

    public void updateUser(User user) {
        getDelegate().updateUser(user);
    }

    public String validatePasswordFormat(String password) {
        return getDelegate().validatePasswordFormat(password);
    }

    protected User searchFromLdap(String name, String password) {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.SECURITY_PRINCIPAL, "cn=" + name + "," + baseDn);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, serverUrl + baseDn);

        SearchControls controls = new SearchControls();
        controls.setCountLimit(1);
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        InitialLdapContext ldapContext = null;
        try {
            ldapContext = new InitialLdapContext(env, null);

            controls.setReturningAttributes(new String[]{"givenName", "mail", "uid", "uidNumber"});
            NamingEnumeration<SearchResult> result = ldapContext.search(
                "", userSearchFilter,
                new String[]{"jixu.hu"},
                controls
            );
            if (result.hasMore()) {
                SearchResult next = result.next();
                User user = new User();
                user.setAccount(name);
                user.setPassword("RESERVED");

                Attributes attributes = next.getAttributes();
                user.setEmail(String.valueOf(attributes.get("mail").get()));
                user.setName(String.valueOf(attributes.get("givenName").get()));
                user.setEmpId(String.valueOf(attributes.get("uidNumber").get()));
                return user;
            }
        } catch (NamingException ignored) {
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            if (ldapContext != null) {
                try {
                    ldapContext.close();
                } catch (NamingException ignored) {

                }
                ldapContext = null;
            }
        }

        return null;
    }

}

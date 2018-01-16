package com.example.jacob.blankbookandroidclient.managers;

import com.example.jacob.blankbookandroidclient.api.models.Group;
import com.example.jacob.blankbookandroidclient.utils.Encryption;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.HashMap;

public class GroupPasswordManager {
    private final String TEST_STRING = "pe6YZAgNv2fCZefMVzX98to1ZHzPpXJurU5A1ggxCcR6gku8f6CsTUIfPhHMUA1988k5BRJqjcjn2WEgtLyFRq3l453oYpsbSpM1";

    private static GroupPasswordManager instance;

    private HashMap<String, String> passwordsByGroup = new HashMap<>();

    public static GroupPasswordManager getInstance() {
        if (instance == null) {
            instance = new GroupPasswordManager();
        }
        return instance;
    }

    public void putPassword(String group, String password) {
        passwordsByGroup.put(group, password);
    }

    public String decryptString(Group group, String data) throws GeneralSecurityException {
        return decryptString(group.Name, group.Salt, data);
    }

    public String decryptString(String groupName, String salt, String data) throws GeneralSecurityException {
        return decryptStringFromPassword(passwordsByGroup.get(groupName), salt, data);
    }

    public String decryptStringFromPassword(String password, String salt, String data) throws GeneralSecurityException {
        try {
            Encryption.CipherTextIvMac cipherTextIvMac = new Encryption.CipherTextIvMac(data);
            return Encryption.decryptString(cipherTextIvMac, Encryption.generateKeyFromPassword(password, salt));
        } catch (UnsupportedEncodingException e) {
            throw new GeneralSecurityException();
        }
    }

    public boolean passwordCorrect(String password, Group group) {
        try {
            System.out.println("decrypted test: " + decryptStringFromPassword(password, group.Salt, group.PasswordTestHash));
            return TEST_STRING.equals(decryptStringFromPassword(password, group.Salt, group.PasswordTestHash));
        } catch (Exception e) {
            return false;
        }
    }

    public String encryptString(String groupName, String salt, String data) throws GeneralSecurityException {
        return encryptStringFromPassword(passwordsByGroup.get(groupName), salt, data);
    }

    public String getTestHash(String password, String salt) throws GeneralSecurityException {
        return encryptStringFromPassword(password, salt, TEST_STRING);
    }

    public String encryptStringFromPassword(String password, String salt, String data) throws GeneralSecurityException {
        try {
            Encryption.SecretKeys keys = Encryption.generateKeyFromPassword(password, salt);
            return Encryption.encrypt(data, keys).toString();
        } catch (UnsupportedEncodingException e) {
            throw new GeneralSecurityException();
        }
    }

    public boolean hasPassword(String groupName) {
        return passwordsByGroup.containsKey(groupName);
    }
}

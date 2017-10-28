package com.example.jacob.blankbookandroidclient.api.models;

public class Group {
    public Group(String Name, boolean Protected, String PasswordTestHash, String Salt) {
        this.Name = Name;
        this.Protected = Protected;
        this.PasswordTestHash = PasswordTestHash;
        this.Salt = Salt;
    }

    public String Name;
    public boolean Protected;
    public String PasswordTestHash;
    public String Salt;
}

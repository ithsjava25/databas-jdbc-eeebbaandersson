package com.example;

public interface AccountRepository {

    // Definierar metoder för affärslogik (finns just nu kvar i main)
    boolean validateLogin(String username, String password);
    int createAccount(String firstName, String lastName, String ssn , String password);
    int updateAccount(String newPassword, int userId);
    int deleteAccount(int userId);


}

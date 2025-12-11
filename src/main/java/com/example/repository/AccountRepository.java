package com.example.repository;

public interface AccountRepository {

    // Definierar metoder för affärslogik (finns just nu kvar i main)
    boolean validateLogin(String username, String password);
    int createAccount(String firstName, String lastName, String ssn , String password);
    boolean updateAccountPassword(String newPassword, int userId);
    boolean deleteAccount(int userId);


}

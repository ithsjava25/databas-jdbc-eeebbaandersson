package com.example;

public interface AccountRepository {

    // Definierar metoder för affärslogik (finns just nu kvar i main)
    boolean validateLogin(String username, String password);
    void createAccount(String firstName, String lastName, String ssn , String password);
    void updateAccount(String newPassword, int userId);
    void deleteAccount(int userId);


}

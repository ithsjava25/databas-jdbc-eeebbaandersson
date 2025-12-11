package com.example.repository;

import java.util.Optional;

public interface AccountRepository {

    // Definierar metoder för affärslogik (finns just nu kvar i main)
    Optional<Integer> validateLogin(String username, String password);
    int createAccount(String firstName, String lastName, String ssn , String password);
    boolean updateAccountPassword(String newPassword, int userId);
    boolean deleteAccount(int userId);


}

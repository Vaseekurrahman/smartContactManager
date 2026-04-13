package com.smart.controller;

import java.security.Principal;
import java.util.List;

import com.smart.Repository.ContactRepository;
import com.smart.Repository.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SearchController {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/search/{query}")
    public List<Contact> search(@PathVariable("query") String query, Principal principal) {

        // current logged-in user
        String username = principal.getName();
        User user = userRepository.getUserByEmail(username);

        // search contacts of that user
        return contactRepository.findByNameContainingAndUser(query, user);
    }
}
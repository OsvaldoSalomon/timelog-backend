package com.timelog.timelog.controller;

import com.timelog.timelog.exceptions.UserNotFoundException;
import com.timelog.timelog.models.User;
import com.timelog.timelog.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.timelog.timelog.constants.TimeLogConstants.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping(TIME_LOG_V1_PATH)
public class UserController {

    private UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }

        return Sort.Direction.ASC;
    }

    @GetMapping(USERS_PATH)
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false/*, defaultValue = "0"*/) Integer page,
            @RequestParam(required = false/*, defaultValue = "3"*/) Integer size,
            @RequestParam(required = false, defaultValue = "id,asc") String[] sort) {

        try {
            List<Sort.Order> orders = new ArrayList<Sort.Order>();

            if (sort[0].contains(",")) {
                for (String sortOrder : sort) {
                    String[] _sort = sortOrder.split(",");
                    orders.add(new Sort.Order(getSortDirection(_sort[1]), _sort[0]));
                }
            } else {
                orders.add(new Sort.Order(getSortDirection(sort[1]), sort[0]));
            }

            Map<String, Object> responseAll = new HashMap<>();

            List<User> users;
            if (page == null) {
                users = userRepository.findAll(Sort.by(orders));
                responseAll.put("users", users);
                return new ResponseEntity<>(responseAll, HttpStatus.OK);
            }

            Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));

            Page<User> pageUsers;
            if (searchText == null)
                pageUsers = userRepository.findAll(pagingSort);
            else {
                TextCriteria search = TextCriteria.forDefaultLanguage().matching(searchText);
                pageUsers = userRepository.findAllBy(search, pagingSort);
            }

            users = pageUsers.getContent();

            if (users.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("currentPage", pageUsers.getNumber());
            response.put("totalUsers", pageUsers.getTotalElements());
            response.put("totalPages", pageUsers.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(USERS_PATH + "/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") String id) {

        Optional<User> optionalResponse = userRepository.findById(id);
        if (!optionalResponse.isPresent()) {

            throw new UserNotFoundException(id);
        }
        return new ResponseEntity<>(optionalResponse.get(), HttpStatus.OK);
    }

    @PostMapping(USERS_PATH)
    public @ResponseBody
    ResponseEntity<User> addUser(@Validated @RequestBody User user) {
        userRepository.save(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping(USERS_PATH + "/{id}")
    public void deleteUser(@PathVariable("id") String id) {

        Optional<User> optionalResponse = userRepository.findById(id);
        if (!optionalResponse.isPresent()) {

            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    @PutMapping(USERS_PATH + "/{id}")
    public ResponseEntity<User> updateUserById(@RequestBody User user, @PathVariable("id") String id) {

        Optional<User> optionalResponse = userRepository.findById(id);
        if (!optionalResponse.isPresent()) {

            throw new UserNotFoundException(id);
        }
        user.id = id;
        userRepository.save(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

}

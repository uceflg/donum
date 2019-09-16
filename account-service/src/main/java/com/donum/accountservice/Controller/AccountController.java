package com.donum.accountservice.Controller;

import com.donum.accountservice.Service.UsersInRange;
import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import com.donum.accountservice.Model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AccountController extends BaseController{

    @Autowired
    private UsersInRange _usersInRange;

    final static Logger logger = Logger.getLogger(AccountController.class);

    @ResponseBody
    @CrossOrigin()
    @GetMapping(value = "/create/{bloodGroup}/{firstName}/{lastName}/{email}/{password}/{addressline}/{postcode}")
    public ResponseEntity<String> Register(@PathVariable String bloodGroup , @PathVariable  String firstName, @PathVariable  String lastName, @PathVariable  String email, @PathVariable  String password, @PathVariable  String addressline, @PathVariable  String postcode){

        switch (APIKeyController._singleDynamoRepo.createUser(bloodGroup, firstName, lastName, email, password, addressline, postcode)){
            case 1:
                return new ResponseEntity<>("Email in use. Try another email.", HttpStatus.CONFLICT);
            default:
                return new ResponseEntity<>("User added to Database", HttpStatus.CREATED);
        }
    }

    @GetMapping("/verify-account/{accesscode}/{firstname}/{email}")
    public ResponseEntity<String> Verify(@PathVariable String accesscode, @PathVariable String firstname, @PathVariable String email){

        try {
            User user = APIKeyController._singleDynamoRepo.getSingleUser(email);
            if(user.isVerified()) {
                return new ResponseEntity<>("Already Verified", HttpStatus.CONFLICT);
            }
            if(user.getAccesscode().equals(accesscode)){
                APIKeyController._singleDynamoRepo.updateUserDetail(email, 4, "");
                return new ResponseEntity<>(accesscode, HttpStatus.OK);
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>("User does not exist!", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>("Invalid!", HttpStatus.UNAUTHORIZED);
    }

    @CrossOrigin()
    @GetMapping(value = "/get-all")
    public ResponseEntity<String> getUsers() {
        List<User> Users = APIKeyController._singleDynamoRepo.getAllUsers();
        if(Users.isEmpty()){
            return new ResponseEntity<>("No users in database.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new Gson().toJson(Users), HttpStatus.OK);
    }

    @CrossOrigin()
    @GetMapping(value = "/getuser/{email}")
    public ResponseEntity<String> getUserDetails(@PathVariable String email) { // Working
        User user = APIKeyController._singleDynamoRepo.getSingleUser(email);
        if(user != null){
            return new ResponseEntity<>(user.toString(), HttpStatus.OK);
        }
        return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
    }

    @PutMapping(value = "/updateuser")
    public void updateUserDetails(@RequestBody User user) {
        APIKeyController._singleDynamoRepo.updateUserDetails(user);
    }

    @GetMapping(value = "/reset-password-email/{firstname}/{email}")
    public ResponseEntity<String> resetPasswordEmail(@PathVariable String firstname, @PathVariable String email){

        HttpStatus httpStatus = HttpStatus.OK;
        String message = "";

        switch (APIKeyController._singleDynamoRepo.passwordResetEmail(email)){
            case 1:
                message = "Email Sent.";
                break;
            case -1:
                httpStatus = HttpStatus.BAD_REQUEST;
                message = "Error Encountered, Email Not Sent!";
        }
        return new ResponseEntity<>(message, httpStatus);
    }

    @CrossOrigin()
    @GetMapping(value = "/updatepassword/{email}/{update}")
    public ResponseEntity<String> updateUserPassword(@PathVariable String email, @PathVariable String update ){

        HttpStatus httpStatus = HttpStatus.OK;
        String message = "";

        switch(APIKeyController._singleDynamoRepo.updateUserDetail(email, 1, update)){
            case 1:
                message = "Password update: success";
                break;
            case -1:
                httpStatus = HttpStatus.BAD_REQUEST;
                message = "Failed to update";
                break;
        }
        return new ResponseEntity<>(message, httpStatus);
    }

    @CrossOrigin()
    @GetMapping(value = "/updateaddress/{email}/{update}")
    public ResponseEntity<String> updateUserAddress(@PathVariable String email, @PathVariable String update ){

        HttpStatus httpStatus = HttpStatus.OK;
        String message = "";

        switch(APIKeyController._singleDynamoRepo.updateUserDetail(email, 2, update)){
            case 1:
                message = "Address update: success";
                break;
            case -1:
                httpStatus = HttpStatus.BAD_REQUEST;
                message = "Failed to update";
                break;
        }
        return new ResponseEntity<>(message, httpStatus);
    }

    @CrossOrigin()
    @GetMapping(value = "/updateemail/{email}/{update}")
    public ResponseEntity<String> updateUserEmail(@PathVariable String firstName, @PathVariable String email, @PathVariable String update ){

        ResponseEntity message = null;
        switch(APIKeyController._singleDynamoRepo.updateUserDetail(email, 3, update)){
            case 1:
                message = new ResponseEntity<>("Email update: success", HttpStatus.OK);
                break;
            case -1:
                message = new ResponseEntity<>("Failed to update.", HttpStatus.NOT_FOUND);
                break;
        }
        return message;
    }

    @CrossOrigin()
    @DeleteMapping(value = "/delete/{email}")
    public ResponseEntity<String> deleteUserDetails(@PathVariable String email) { // Working
        User user = new User();
        user.setEmail(email);
        if(APIKeyController._singleDynamoRepo.getSingleUser(email) != null){
            APIKeyController._singleDynamoRepo.deleteUserDetails(user);
            return new ResponseEntity<>(user.toString() + " Deleted", HttpStatus.OK);
        }
        return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
    }

    @CrossOrigin()
    @GetMapping(value = "/login/{email}/{password}")
    public ResponseEntity<String> Login(@PathVariable String email, @PathVariable String password) {
        if(APIKeyController._singleDynamoRepo.checkCredentials(email, password)){
            try {
                JSONObject User = new JSONObject(new Gson().toJson(APIKeyController._singleDynamoRepo.getSingleUser(email)));
                User.remove("password");
                return new ResponseEntity<>(new Gson().toJson(User), HttpStatus.OK);
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }
        return new ResponseEntity<>("Login Failed.", HttpStatus.UNAUTHORIZED);
    }

    @CrossOrigin()
    @GetMapping(value = "/usersinrange/{longitude}/{latitude}/{radius}")
    public ResponseEntity<String> UsersInRangeOfRadius(@PathVariable double longitude, @PathVariable double latitude, @PathVariable int radius){
        if(_usersInRange.getRadiusPostcodes(longitude, latitude, radius) != ""){
            return new ResponseEntity<>(_usersInRange.getRadiusPostcodes(longitude, latitude, radius), HttpStatus.OK);
        }
        return new ResponseEntity<>("No users in given radius", HttpStatus.NOT_FOUND);
    }

    @Override
    public void loadController() {
        _controllerName = "AccountController ";
    }
}
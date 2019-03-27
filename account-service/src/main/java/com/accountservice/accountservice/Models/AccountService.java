package com.accountservice.accountservice.Models;

import com.accountservice.accountservice.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    @Autowired
    private UserRepository UserRepo;

    public User createNew(String firstname){ // This works
        return UserRepo.save(new User(firstname));
    }

    public User create( String bloodGroup, String firstname, String _surname, String _email, String _password, String _addressline, String _postcode ){ // This Works
        return UserRepo.save( new User(bloodGroup, firstname,  _surname,  _email,  _password,  _addressline,  _postcode) );
    }

    public User getByfirstName(String firstName){
        return UserRepo.findByFirstName(firstName);
    }

    public List<User> getAll(){
        return UserRepo.findAll();
    }

    public User Update(String bloodGroup, String firstname, String _surname, String _email, String _password, String _addressline, String _postcode) {
        User SpecificUser = UserRepo.findByFirstName(firstname);

        SpecificUser.setBloodGroup(bloodGroup);
        SpecificUser.setfirstName(firstname);
        SpecificUser.set_surname(_surname);
        SpecificUser.set_email(_email);
        SpecificUser.set_password(_password);
        SpecificUser.set_addressline(_addressline);
        SpecificUser.set_postcode(_postcode);

        return UserRepo.save(SpecificUser);
    }

    public void deleteAll(){
        UserRepo.deleteAll();
    } // This Works

    public void deleteByfirstName(String firstname){ // This Doesnt Work
        User user = UserRepo.findByFirstName(firstname);
        System.out.println(user);
        UserRepo.delete(user);
    }

}

package system.rest;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import system.bo.UsersEntity;
import system.bo.UsersEntity;
import system.rest.utils.View;
import system.service.UserService;

import javax.xml.bind.DatatypeConverter;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.security.MessageDigest;
import java.util.Map;
import java.util.NoSuchElementException;


@RestController
@RequestMapping("/user")
public class UserController{
    
    @Autowired
    private UserService userService;

    @JsonView(View.Brief.class)
    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsersEntity> find(@PathVariable("id") int id){
        try {
            UsersEntity user = userService.find(id);
            return new ResponseEntity<UsersEntity>(user, HttpStatus.OK);
        }
        catch (NoSuchElementException e){
            return new ResponseEntity<UsersEntity>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/detail/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> findUserDetails(@PathVariable("id") int id){
        try {
            Map<String, String> details = userService.findUserDetails(id);
            return new ResponseEntity<Map<String, String>>(details, HttpStatus.OK);
        }
        catch (NoSuchElementException e){
            return new ResponseEntity<Map<String, String>>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/AddFriend", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsersEntity> addFriend(@RequestBody addFriend friendCredentials){
        try {
            UsersEntity u1 = userService.find(friendCredentials.userId);
            UsersEntity u2 = userService.findByEmail(friendCredentials.friendEmail);
            if(u1.getUsersFriendsById().contains(u2))
            {
                return new ResponseEntity(u2, HttpStatus.OK);
            } else if(u1.getId() == u2.getId())
            {
                return new ResponseEntity(u1, HttpStatus.CONFLICT);
            }
            u1.addToFriends(u2);
            userService.update(u1);
            return new ResponseEntity(u2, HttpStatus.OK);
        }
        catch (NoSuchElementException e){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/changePass", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsersEntity> changeUserPass(@RequestBody changePass credentials){
        try {
            String oldPassword = credentials.oldPass;
            UsersEntity user = userService.find(credentials.id);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(oldPassword.getBytes());
            byte[] digestOld = md.digest();
            md.reset();
            String myHash = DatatypeConverter.printHexBinary(digestOld).toUpperCase();
            if(myHash.equals(user.getPassword()))
            {
                md.update(credentials.newPass.getBytes());
                byte[] digestNew = md.digest();
                String newHash = DatatypeConverter.printHexBinary(digestNew).toUpperCase();
                user.setPassword(newHash);
                userService.update(user);
                return new ResponseEntity(user, HttpStatus.OK);
            } else
            {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        }
        catch (NoSuchElementException e){
            return new ResponseEntity<UsersEntity>(HttpStatus.NOT_FOUND);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return new ResponseEntity<UsersEntity>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/Login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsersEntity> findByLogin(@RequestBody searchFriend login){
        try{
            UsersEntity user = userService.findByEmail(login.email);
            return new ResponseEntity<UsersEntity>(user, HttpStatus.OK);
        }
            catch (NoSuchElementException e){
            return new ResponseEntity<UsersEntity>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/LoginApi", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsersEntity> LoginUser(@RequestBody UsersEntity credentials){
        try{
            String login = credentials.getEmail();
            String password = credentials.getPassword();
            UsersEntity user = userService.findByEmail(login);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] digest = md.digest();
            String myHash = DatatypeConverter.printHexBinary(digest).toUpperCase();
            if(myHash.equals(user.getPassword()))
            {
                return new ResponseEntity(user, HttpStatus.OK);
            } else
            {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        }
        catch (NoSuchElementException e){
            return new ResponseEntity<UsersEntity>(HttpStatus.NOT_FOUND);
        }
        catch (NoSuchAlgorithmException e)
        {
            return new ResponseEntity<UsersEntity>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createUser(@RequestBody UsersEntity user){
        try{
            UsersEntity checkUser = userService.findByEmail(user.getEmail());
            return new ResponseEntity<UsersEntity>(user, HttpStatus.CONFLICT);
        }
        catch (NoSuchElementException e){
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                md.update(user.getPassword().getBytes());
                byte[] digest = md.digest();
                String hash = DatatypeConverter.printHexBinary(digest).toUpperCase();
                user.setPassword(hash);
                userService.persist(user);
            } catch(NoSuchAlgorithmException ex)
            {
                return new ResponseEntity<UsersEntity>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<UsersEntity>(user, HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateUser(@RequestBody UsersEntity user){
        userService.update(user);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity removeUser(@RequestBody UsersEntity user){
        userService.remove(user);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @JsonView(View.Brief.class)
    @RequestMapping(value = "/getAll", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UsersEntity>> getAll() {
        try {
            List<UsersEntity> users = userService.findAll();
            return new ResponseEntity<List<UsersEntity>>(users, HttpStatus.OK);
        }
        catch (NoSuchElementException e){
            return new ResponseEntity<List<UsersEntity>>(HttpStatus.NOT_FOUND);
        }
    }

}

class changePass implements Serializable{
    int id;
    String oldPass;
    String newPass;

    public changePass() {}

    public void setid(String s) {this.id = Integer.parseInt(s);}
    public void setoldPass(String s) {this.oldPass = s;}
    public void setnewPass(String s) {this.newPass = s;}
}

class addFriend implements Serializable {
    int userId;
    String friendEmail;

    public addFriend() {}

    public void setuserId(String s) {this.userId = Integer.parseInt(s);}
    public void setFriendEmail(String s) {this.friendEmail = s;}
}

class searchFriend implements Serializable {
    String email;

    public searchFriend() {}

    public void setemail(String s) {this.email = s;}
}
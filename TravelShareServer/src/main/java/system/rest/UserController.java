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
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.security.MessageDigest;
import java.util.NoSuchElementException;

/**
 *
 * @author Jan Richter
 */
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

    @JsonView(View.Brief.class)
    @RequestMapping(method = RequestMethod.GET, value = "/Login/{login}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsersEntity> findByLogin(@PathVariable("login") String login){
        try{
            UsersEntity user = userService.findByEmail(login);
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

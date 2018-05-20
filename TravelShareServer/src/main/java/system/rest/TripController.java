package system.rest;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import system.bo.PhotosEntity;
import system.bo.TripEntity;
import system.bo.UsersEntity;
import system.rest.utils.View;
import system.service.PhotoService;
import system.service.TripService;
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
@RequestMapping("/trip")
public class TripController{

    @Autowired
    private TripService TripService;

    @Autowired
    private UserService userService;

    @Autowired
    private PhotoService photoService;

    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripEntity> findCurrentUserTrip(@PathVariable("id") int id){
        try {
            TripEntity Trip = TripService.findCurrentByUserId(id);
            return new ResponseEntity<TripEntity>(Trip, HttpStatus.OK);
        }
        catch (NoSuchElementException e){
            return new ResponseEntity<TripEntity>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/finish/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripEntity> finishCurrentUserTrip(@PathVariable("id") int id){
        try {
            TripEntity Trip = TripService.findCurrentByUserId(id);
            Trip.setIsActive(false);
            TripService.update(Trip);
            return new ResponseEntity<TripEntity>(Trip, HttpStatus.OK);
        }
        catch (NoSuchElementException e){
            return new ResponseEntity<TripEntity>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/photos/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PhotosEntity>> findPhotosByTrip(@PathVariable("id") int id){
        try {
            TripEntity t = TripService.find(id);
            List<PhotosEntity> Photos = photoService.findPhotosByTrip(t);
            return new ResponseEntity<List<PhotosEntity>>(Photos, HttpStatus.OK);
        }
        catch (NoSuchElementException e){
            return new ResponseEntity<List<PhotosEntity>>(HttpStatus.NOT_FOUND);
        }
    }

    /*@JsonView(View.Brief.class)
    @RequestMapping(method = RequestMethod.GET, value = "/Login/{login}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripEntity> findByLogin(@PathVariable("login") String login){
        try{
            TripEntity user = TripService.findByEmail(login);
            return new ResponseEntity<TripEntity>(user, HttpStatus.OK);
        }
        catch (NoSuchElementException e){
            return new ResponseEntity<UsersEntity>(HttpStatus.NOT_FOUND);
        }
    }*/

    /*@RequestMapping(method = RequestMethod.POST, value = "/LoginApi", produces = MediaType.APPLICATION_JSON_VALUE)
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
    }*/

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createTrip(@RequestBody TripEntity Trip){
        if(Trip.getUsersByUserId().getId() == 0)
        {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        TripService.persist(Trip);
        return new ResponseEntity<TripEntity>(Trip, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateTrip(@RequestBody TripEntity user){
        TripService.update(user);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity removeTrip(@RequestBody TripEntity Trip){
        TripService.remove(Trip);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @JsonView(View.Brief.class)
    @RequestMapping(value = "/getAll", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TripEntity>> getAll() {
        try {
            List<TripEntity> users = TripService.findAll();
            return new ResponseEntity<List<TripEntity>>(users, HttpStatus.OK);
        }
        catch (NoSuchElementException e){
            return new ResponseEntity<List<TripEntity>>(HttpStatus.NOT_FOUND);
        }
    }
}

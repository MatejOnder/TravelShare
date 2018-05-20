package system.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
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
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.security.MessageDigest;

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

    @RequestMapping(method = RequestMethod.GET, value = "/findAll/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TripEntity>> findAllFinishedUserTrip(@PathVariable("id") int id){
        try {
            List<TripEntity> Trips = TripService.findAllByUserId(id);
            return new ResponseEntity<List<TripEntity>>(Trips, HttpStatus.OK);
        }
        catch (NoSuchElementException e){
            return new ResponseEntity<List<TripEntity>>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/findAllByFriends/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TripUserMap>> findAllFinishedUserFriendsTrip(@PathVariable("id") int id){
        try {
            List<TripEntity> Trips = TripService.findAllByUserFriendsId(id);
            ArrayList<TripUserMap> tripsUsers = new ArrayList<>();
            for(TripEntity t: Trips)
            {
                TripUserMap tm = new TripUserMap();
                tm.t = t;
                tm.u = t.getUsersByUserId();
                tripsUsers.add(tm);
            }
            return new ResponseEntity<List<TripUserMap>>(tripsUsers, HttpStatus.OK);
        }
        catch (NoSuchElementException e){
            return new ResponseEntity<List<TripUserMap>>(HttpStatus.NOT_FOUND);
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

class TripUserMap implements Serializable {
    public TripEntity t;
    public UsersEntity u;

    public TripUserMap() {}
}
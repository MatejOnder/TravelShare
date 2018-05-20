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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/photo")
public class PhotoController{

    @Autowired
    private PhotoService PhotoService;

    @Autowired
    private TripService tripService;

    @Autowired
    private UserService userService;

    @JsonView(View.Brief.class)
    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> find(@PathVariable("id") int id){
        try {
            PhotosEntity Photo = PhotoService.find(id);
            BufferedImage img = null;
            ImageIO.setUseCache(false);
            try {
                img = ImageIO.read(new File(Photo.getLocation()));
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(img, "jpeg", os);
                String b64 = Base64.getEncoder().encodeToString(os.toByteArray());
                return new ResponseEntity<String>(b64, HttpStatus.OK);
            } catch (java.io.IOException e)
            {
                e.printStackTrace();
            }
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }
        catch (NoSuchElementException e){
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
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
    public ResponseEntity createPhoto(@RequestBody PhotosEntity Photo){
        PhotoService.persist(Photo);
        TripEntity t = tripService.find(Photo.getTripByTripId().getId());
        t.addPhotosById(Photo);
        tripService.update(t);
        return new ResponseEntity<PhotosEntity>(Photo, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity uploadPhoto(@RequestBody FileUpload Photo){
        String base64Image = Photo.fileContent.split(",")[1];
        byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);
        ImageIO.setUseCache(false);
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            Path pathName = Paths.get("C:/photos/"+Photo.userId+"/");
            String fileName = pathName+"\\"+UUID.randomUUID().toString()+".jpg";
            File outputFile = new File(fileName);
            if(!outputFile.canWrite())
            {
                Files.createDirectories(pathName);
            }
            outputFile.createNewFile();
            ImageIO.write(img, "jpeg", outputFile);
            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (java.io.IOException e)
        {
            e.printStackTrace();
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateTrip(@RequestBody PhotosEntity Photo){
        PhotoService.update(Photo);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity removeTrip(@RequestBody PhotosEntity Photo){
        PhotoService.remove(Photo);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @JsonView(View.Brief.class)
    @RequestMapping(value = "/getAll", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PhotosEntity>> getAll() {
        try {
            List<PhotosEntity> Photos = PhotoService.findAll();
            return new ResponseEntity<List<PhotosEntity>>(Photos, HttpStatus.OK);
        }
        catch (NoSuchElementException e){
            return new ResponseEntity<List<PhotosEntity>>(HttpStatus.NOT_FOUND);
        }
    }
}


class FileUpload implements Serializable{

    String fileContent;
    int userId;

    public FileUpload() {}

    public void setfileContent(String s)
    {
        fileContent = s;
    }

    public void setuserId(String s)
    {
        userId = Integer.parseInt(s);
    }
}

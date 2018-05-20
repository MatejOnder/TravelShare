package system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import system.bo.PhotosEntity;
import system.bo.TripEntity;
import system.bo.UsersEntity;
import system.dao.PhotoDao;
import system.dao.TripDao;
import system.dao.UserDao;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PhotoService {

    @Autowired
    private PhotoDao PhotoDao;

    @Transactional(readOnly = true)
    public List<PhotosEntity> findAll() throws NoSuchElementException {
        List<PhotosEntity> PhotoList = PhotoDao.findAll();
        if(PhotoList.size() == 0){
            throw new NoSuchElementException();
        }
        return PhotoList;
    }

    @Transactional(readOnly = true)
    public PhotosEntity find(int id) throws NoSuchElementException {
        PhotosEntity Photo = PhotoDao.find(id);
        if(Photo == null){
            throw  new NoSuchElementException();
        }
        return  Photo;
    }

    @Transactional(readOnly = true)
    public List<PhotosEntity> findPhotosByTrip(TripEntity id) throws NoSuchElementException {
        List<PhotosEntity> Photo = PhotoDao.findPhotosByTrip(id);
        if(Photo == null){
            throw  new NoSuchElementException();
        }
        return  Photo;
    }

    /*@Transactional(readOnly = true)
    public TripEntity findByEmail(String email) throws NoSuchElementException {
        TripEntity Trip = tripDao.findByEmail(email);
        if(Trip == null){
            throw  new NoSuchElementException();
        }
        return  Trip;
    }*/

    /*@Transactional(readOnly = true)
    public boolean exists(String login) {
        return tripDao.findByEmail(login) != null;
    }*/

    @Transactional
    public void remove(PhotosEntity user){
        PhotoDao.remove(PhotoDao.find(user.getId()));
    }

    @Transactional
    public void persist(PhotosEntity Photo) {
        PhotoDao.persist(Photo);
    }

    @Transactional
    public void update(PhotosEntity Photo){
        PhotoDao.update(Photo);
    }
}

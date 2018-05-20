package system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import system.bo.TripEntity;
import system.bo.UsersEntity;
import system.dao.TripDao;
import system.dao.UserDao;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TripService {

    @Autowired
    private TripDao tripDao;

    @Transactional(readOnly = true)
    public List<TripEntity> findAll() throws NoSuchElementException {
        List<TripEntity> TripList = tripDao.findAll();
        if(TripList.size() == 0){
            throw new NoSuchElementException();
        }
        return TripList;
    }

    @Transactional(readOnly = true)
    public TripEntity find(int id) throws NoSuchElementException {
        TripEntity Trip = tripDao.find(id);
        if(Trip == null){
            throw  new NoSuchElementException();
        }
        return  Trip;
    }

    @Transactional(readOnly = true)
    public TripEntity findCurrentByUserId(int id) throws NoSuchElementException {
        TripEntity Trip = tripDao.findCurrentByUserId(id);
        if(Trip == null){
            throw  new NoSuchElementException();
        }
        return  Trip;
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
    public void remove(TripEntity user){
        tripDao.remove(tripDao.find(user.getId()));
    }

    @Transactional
    public void persist(TripEntity Trip) {
        tripDao.persist(Trip);
    }

    @Transactional
    public void update(TripEntity Trip){
        tripDao.update(Trip);
    }
}

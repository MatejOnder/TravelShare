package system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import system.bo.UsersEntity;
import system.dao.UserDao;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {
    
    @Autowired
    private UserDao userDao;

    @Transactional(readOnly = true)
    public List<UsersEntity> findAll() throws NoSuchElementException {
        List<UsersEntity> userList = userDao.findAll();
        if(userList.size() == 0){
            throw new NoSuchElementException();
        }
        return userList;
    }

    @Transactional(readOnly = true)
    public UsersEntity find(int id) throws NoSuchElementException {
        UsersEntity user = userDao.find(id);
        if(user == null){
            throw  new NoSuchElementException();
        }
        return  user;
    }

    @Transactional(readOnly = true)
    public UsersEntity findByEmail(String email) throws NoSuchElementException {
        UsersEntity user = userDao.findByEmail(email);
        if(user == null){
            throw  new NoSuchElementException();
        }
        return  user;
    }

    @Transactional(readOnly = true)
    public boolean exists(String login) {
        return userDao.findByEmail(login) != null;
    }

    @Transactional
    public void remove(UsersEntity user){
        userDao.remove(userDao.find(user.getId()));
    }

    @Transactional
    public void persist(UsersEntity user) {
        userDao.persist(user);
    }

    @Transactional
    public void update(UsersEntity user){
       userDao.update(user);
    }
}

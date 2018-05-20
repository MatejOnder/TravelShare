package system.dao;

import java.util.List;
import java.util.Objects;
import javax.persistence.NoResultException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import system.bo.TripEntity;
import system.bo.UsersEntity;


@Repository
public class TripDao extends BaseDao<TripEntity>{

    @Autowired
    private UserDao userDao;

    public TripDao() {
        super(TripEntity.class);
    }

    @Override
    public void remove(TripEntity user) {

    }

    public TripEntity findCurrentByUserId(int id)
    {
        UsersEntity u = userDao.find(id);
        return em.createNamedQuery("TripEntity.findCurrentByUserId", TripEntity.class).setParameter("user_id", u).getSingleResult();
    }
}

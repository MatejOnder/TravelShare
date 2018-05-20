package system.dao;

import java.util.ArrayList;
import java.util.Comparator;
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

    public List<TripEntity> findAllByUserId(int id)
    {
        UsersEntity u = userDao.find(id);
        return em.createNamedQuery("TripEntity.findAllByUserId", TripEntity.class).setParameter("user_id", u).getResultList();
    }

    public List<TripEntity> findAllByUserFriendsId(int id)
    {
        UsersEntity u = userDao.find(id);
        ArrayList<TripEntity> trips = new ArrayList<>();
        for(UsersEntity ue: u.getUsersFriendsById())
        {
            trips.addAll(findAllByUserId(ue.getId()));
        }
        trips.sort(new Comparator<TripEntity>() {
            @Override
            public int compare(TripEntity o1, TripEntity o2) {
                return o2.getId()-o1.getId();
            }
        });
        return trips;
    }
}

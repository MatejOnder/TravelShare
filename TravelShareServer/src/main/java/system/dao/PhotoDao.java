package system.dao;

import java.util.List;
import java.util.Objects;
import javax.persistence.NoResultException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import system.bo.PhotosEntity;
import system.bo.TripEntity;
import system.bo.UsersEntity;


@Repository
public class PhotoDao extends BaseDao<PhotosEntity>{

    public PhotoDao() {
        super(PhotosEntity.class);
    }

    @Override
    public void remove(PhotosEntity photo) {

    }

    public List<PhotosEntity> findPhotosByTrip(TripEntity t) {
        return em.createNamedQuery("PhotosEntity.findPhotosByTrip", PhotosEntity.class).setParameter("trip_id", t).getResultList();
    }
}

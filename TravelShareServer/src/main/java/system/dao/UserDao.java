package system.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.NoResultException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import system.bo.UsersEntity;


@Repository
public class UserDao extends BaseDao<UsersEntity>{
    
    public UserDao() {
        super(UsersEntity.class);
    }

    @Override
    public void remove(UsersEntity user) {

    }

    @Override
    public UsersEntity find(Integer id) {
        return super.find(id);
    }

    public Map<String, String> findUserDetails(Integer id) {
        Map<String, String> userDetails = new HashMap<>();
        long photoCount;
        long tripCount;
        String email;
        email = this.find(id).getEmail();
        tripCount = em.createNamedQuery("UsersEntity.findTripCount", Long.class).setParameter("user_id", this.find(id)).getSingleResult();
        photoCount = em.createNamedQuery("UsersEntity.findPhotoCount", Long.class).setParameter("user_id", this.find(id)).getSingleResult();
        userDetails.put("photoCount", Long.toString(photoCount));
        userDetails.put("tripCount", Long.toString(tripCount));
        userDetails.put("email", email);
        return userDetails;
    }

    public UsersEntity findByEmail(String email) {
        Objects.requireNonNull(email);
        try {
            return em.createNamedQuery("UsersEntity.findByEmail", UsersEntity.class).setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean exists(String login) {
        final List result = em.createNamedQuery("UsersEntity.findByEmail", UsersEntity.class).setParameter("login", login)
                .getResultList();
        return !result.isEmpty();
    }
}

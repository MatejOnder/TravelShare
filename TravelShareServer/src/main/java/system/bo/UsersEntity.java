package system.bo;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "users", schema = "public", catalog = "travelshare")
@NamedQueries({
        @NamedQuery(name = "UsersEntity.findAll", query = "SELECT u FROM UsersEntity u"),
        @NamedQuery(name = "UsersEntity.findByEmail", query = "SELECT u FROM UsersEntity u WHERE u.email = :email"),})
public class UsersEntity implements Serializable{
    private Integer id;
    private String email;
    private String password;
    private Collection<PhotosCommentsEntity> photosCommentsById;
    private Collection<TripEntity> tripsById;
    private Collection<UsersEntity> usersFriendsById;

    public UsersEntity() {}
    public UsersEntity(int id) {this.id = id;}
    public UsersEntity(String id) {this.id = Integer.parseInt(id);}

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Basic
    @Column(name = "email", nullable = false, length = 255)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Basic
    @Column(name = "password", nullable = false, length = 255)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsersEntity that = (UsersEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(email, that.email) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, email, password);
    }

    @OneToMany(mappedBy = "usersByUserId")
    @JsonManagedReference
    public Collection<PhotosCommentsEntity> getPhotosCommentsById() {
        return photosCommentsById;
    }

    public void setPhotosCommentsById(Collection<PhotosCommentsEntity> photosCommentsById) {
        this.photosCommentsById = photosCommentsById;
    }

    @OneToMany(mappedBy = "usersByUserId")
    @JsonManagedReference
    public Collection<TripEntity> getTripsById() {
        return tripsById;
    }

    public void setTripsById(Collection<TripEntity> tripsById) {
        this.tripsById = tripsById;
    }

    @ManyToMany
    @JoinTable(name="users_friends")
    public Collection<UsersEntity> getUsersFriendsById() {
        return usersFriendsById;
    }

    public void setUsersFriendsById(Collection<UsersEntity> usersFriendsById) {
        this.usersFriendsById = usersFriendsById;
    }
}

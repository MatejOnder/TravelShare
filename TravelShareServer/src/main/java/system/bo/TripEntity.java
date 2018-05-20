package system.bo;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import system.rest.utils.View;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "trip", schema = "public", catalog = "travelshare")
@NamedQueries({
        @NamedQuery(name = "TripEntity.findCurrentByUserId", query = "SELECT t FROM TripEntity t WHERE t.usersByUserId = :user_id AND t.isActive = true"),
        @NamedQuery(name = "TripEntity.findAllByUserId", query = "SELECT t FROM TripEntity t WHERE t.usersByUserId = :user_id AND t.isActive = false ORDER BY t.id DESC"),
})
public class TripEntity implements Serializable{
    private Integer id;
    private Double startlat;
    private Double startlon;
    private Double endlat;
    private Double endlon;
    private boolean isActive;
    private Collection<PhotosEntity> photosById;
    private UsersEntity usersByUserId;

    public TripEntity() {}
    public TripEntity(int id) {this.id = id;}

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
    @Column(name = "startlat", nullable = false, precision = 0)
    public Double getStartlat() {
        return startlat;
    }

    public void setStartlat(Double startlat) {
        this.startlat = startlat;
    }

    @Basic
    @Column(name = "isactive", nullable = false)
    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean v) {
        this.isActive = v;
    }

    @Basic
    @Column(name = "startlon", nullable = false, precision = 0)
    public Double getStartlon() {
        return startlon;
    }

    public void setStartlon(Double startlon) {
        this.startlon = startlon;
    }

    @Basic
    @Column(name = "endlat", nullable = true, precision = 0)
    public Double getEndlat() {
        return endlat;
    }

    public void setEndlat(Double endlat) {
        this.endlat = endlat;
    }

    @Basic
    @Column(name = "endlon", nullable = true, precision = 0)
    public Double getEndlon() {
        return endlon;
    }

    public void setEndlon(Double endlon) {
        this.endlon = endlon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TripEntity that = (TripEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(startlat, that.startlat) &&
                Objects.equals(startlon, that.startlon) &&
                Objects.equals(endlat, that.endlat) &&
                Objects.equals(endlon, that.endlon);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, startlat, startlon, endlat, endlon);
    }

    @OneToMany(mappedBy = "tripByTripId")
    @JsonManagedReference
    public Collection<PhotosEntity> getPhotosById() {
        return photosById;
    }
    public void addPhotosById(PhotosEntity p) {
        this.photosById.add(p);
    }
    public void setPhotosById(Collection<PhotosEntity> photosById) {
        this.photosById = photosById;
    }

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    public UsersEntity getUsersByUserId() {
        return usersByUserId;
    }

    public void setUsersByUserId(UsersEntity usersByUserId) {
        this.usersByUserId = usersByUserId;
    }
}

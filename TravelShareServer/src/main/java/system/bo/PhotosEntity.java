package system.bo;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "photos", schema = "public", catalog = "travelshare")
@NamedQueries({
        @NamedQuery(name = "PhotosEntity.findPhotosByTrip", query = "SELECT p FROM PhotosEntity p WHERE p.tripByTripId = :trip_id"),
})
public class PhotosEntity {
    private Integer id;
    private String location;
    private Float latitude;
    private Float longitude;
    private TripEntity tripByTripId;
    private Collection<PhotosCommentsEntity> photosCommentsById;

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
    @Column(name = "location", nullable = false, length = 255)
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Basic
    @Column(name = "latitude", nullable = false, precision = 0)
    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    @Basic
    @Column(name = "longitude", nullable = false, precision = 0)
    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhotosEntity that = (PhotosEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(location, that.location) &&
                Objects.equals(latitude, that.latitude) &&
                Objects.equals(longitude, that.longitude);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, location, latitude, longitude);
    }

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "trip_id", referencedColumnName = "id", nullable = false)
    public TripEntity getTripByTripId() {
        return tripByTripId;
    }

    public void setTripByTripId(TripEntity tripByTripId) {
        this.tripByTripId = tripByTripId;
    }

    @OneToMany(mappedBy = "photosByPhotoId")
    public Collection<PhotosCommentsEntity> getPhotosCommentsById() {
        return photosCommentsById;
    }

    public void setPhotosCommentsById(Collection<PhotosCommentsEntity> photosCommentsById) {
        this.photosCommentsById = photosCommentsById;
    }
}

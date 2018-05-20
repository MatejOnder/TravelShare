package system.bo;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "photos_comments", schema = "public", catalog = "travelshare")
public class PhotosCommentsEntity {
    private Integer id;
    private String comment;
    private PhotosEntity photosByPhotoId;
    private UsersEntity usersByUserId;

    @Id
    @Column(name = "id", nullable = false)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Basic
    @Column(name = "comment", nullable = false, length = 255)
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhotosCommentsEntity that = (PhotosCommentsEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, comment);
    }

    @ManyToOne
    @JoinColumn(name = "photo_id", referencedColumnName = "id", nullable = false)
    public PhotosEntity getPhotosByPhotoId() {
        return photosByPhotoId;
    }

    public void setPhotosByPhotoId(PhotosEntity photosByPhotoId) {
        this.photosByPhotoId = photosByPhotoId;
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

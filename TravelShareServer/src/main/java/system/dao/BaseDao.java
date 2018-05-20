package system.dao;

import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.util.List;
import java.util.Objects;

public abstract class BaseDao<T> implements GenericDao<T> {

    @PersistenceContext
    EntityManager em;

    protected final Class<T> type;

    protected BaseDao(Class<T> type) {
        this.type = type;
    }

    @Override
    public T find(Integer id) {
        Objects.requireNonNull(id);
        return em.find(type, id);
    }

    @Override
    public List<T> findAll() {
        try {
            return em.createQuery("SELECT e FROM " + type.getSimpleName() + " e", type).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void persist(T entity) {
        Objects.requireNonNull(entity);
        try {
            em.persist(entity);
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }


    @Override
    public T update(T entity) {
        Objects.requireNonNull(entity);
        try {
            return em.merge(entity);
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void remove(T entity) {
        Objects.requireNonNull(entity);
        try {
            final T toRemove = em.merge(entity);
            if (toRemove != null) {
                em.remove(toRemove);
            }
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public boolean exists(Integer id) {
        return id != null && em.find(type, id) != null;
    }
}

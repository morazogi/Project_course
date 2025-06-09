package InfrastructureLayer;

import java.util.List;

public interface IRepo<T> {
    T save(T entity);
    T update(T entity);
    T getById(String id);
    List<T> getAll();
    void deleteById(String id);
    void delete(T entity);
    boolean existsById(String id);
}

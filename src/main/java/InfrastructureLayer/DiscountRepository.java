package InfrastructureLayer;

import DomainLayer.Discount;
import DomainLayer.IDiscountRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class DiscountRepository implements IDiscountRepository {
    public DiscountRepository() {}

    @Override
    public void flush() {

    }

    @Override
    public <S extends Discount> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends Discount> List<S> saveAllAndFlush(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public void deleteAllInBatch(Iterable<Discount> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<String> strings) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Discount getOne(String s) {
        return null;
    }

    @Override
    public Discount getById(String s) {
        return null;
    }

    @Override
    public Discount getReferenceById(String s) {
        return null;
    }

    @Override
    public <S extends Discount> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Discount> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends Discount> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends Discount> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Discount> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Discount> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends Discount, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends Discount> S save(S entity) {
        return null;
    }

    @Override
    public <S extends Discount> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public Optional<Discount> findById(String s) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(String s) {
        return false;
    }

    @Override
    public List<Discount> findAll() {
        return List.of();
    }

    @Override
    public List<Discount> findAllById(Iterable<String> strings) {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(String s) {

    }

    @Override
    public void delete(Discount entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends String> strings) {

    }

    @Override
    public void deleteAll(Iterable<? extends Discount> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public List<Discount> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<Discount> findAll(Pageable pageable) {
        return null;
    }
}

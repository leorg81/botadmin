package org.vaadin.example.services;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.vaadin.example.data.Usr;
import org.vaadin.example.data.UsrRepository;

@Service
public class UsrService {

    private final UsrRepository repository;

    public UsrService(UsrRepository repository) {
        this.repository = repository;
    }

    public Optional<Usr> get(Long id) {
        return repository.findById(id);
    }

    public Usr save(Usr entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Usr> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Usr> list(Pageable pageable, Specification<Usr> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}

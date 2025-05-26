package org.vaadin.example.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UsrRepository extends JpaRepository<Usr, Long>, JpaSpecificationExecutor<Usr> {

}

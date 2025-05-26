package gub.rionegro.syslr.data;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByTelefono(String telefono);

    Optional<Client> findByDocumento(String documento);

    List<Client> findByNombreContainingIgnoreCase(String nombre);

    List<Client> findByChannel(ChannelType channel);

    @Query("SELECT c FROM Client c WHERE c.telefono LIKE %:telefono%")
    List<Client> searchByTelefono(@Param("telefono") String telefono);

    @Query("SELECT c FROM Client c WHERE c.documento LIKE %:documento%")
    List<Client> searchByDocumento(@Param("documento") String documento);

    Optional<Client> findByEmail(String email);

    @Query("SELECT COUNT(c) FROM Client c WHERE c.fechaRegistro >= :from AND c.fechaRegistro <= :to")
    Long countByRegistrationDateRange(@Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT c FROM Client c WHERE c.ultimoAcceso < :threshold")
    List<Client> findInactiveClients(@Param("threshold") Instant threshold);
}
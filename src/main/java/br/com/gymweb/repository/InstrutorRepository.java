package br.com.gymweb.repository;

import br.com.gymweb.model.Instrutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstrutorRepository extends JpaRepository<Instrutor, Long> {

    Optional<Instrutor> findByCpf(String cpf);

    @Query(value = "SELECT cargo FROM instrutor WHERE cpf = :cpf", nativeQuery = true)
    Optional<String> findCargoByCpf(@Param("cpf") String cpf);
    
    // Buscar instrutor por email (para validação de unicidade)
    Optional<Instrutor> findByEmail(String email);
    
    // Buscar instrutores por especialidade
    List<Instrutor> findByEspecialidade(String especialidade);
    
    // Buscar instrutores por nome (contendo)
    List<Instrutor> findByNomeContaining(String nome);
}

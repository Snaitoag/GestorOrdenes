package com.gestordeordenes.gestorordenes.repository;

import com.gestordeordenes.gestorordenes.model.Orden;
import com.gestordeordenes.gestorordenes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {

    Optional<Orden> findByNumeroOrden(String numeroOrden);
    List<Orden> findByTecnicoAsignado(Usuario tecnico);
    List<Orden> findByTecnicoAsignadoIsNull();
    List<Orden> findAllByOrderByFechaCreacionDesc();
}
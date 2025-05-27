package com.gestordeordenes.gestorordenes.repository; // O tu paquete

import com.gestordeordenes.gestorordenes.model.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DispositivoRepository extends JpaRepository<Dispositivo, Long> {
    // Podríamos querer buscar un dispositivo por serial para evitar duplicados si el serial es conocido y único
    Optional<Dispositivo> findBySerial(String serial);

    // Otros métodos de búsqueda si son necesarios en el futuro
    // List<Dispositivo> findByTipoAndMarcaAndModelo(String tipo, String marca, String modelo);
}
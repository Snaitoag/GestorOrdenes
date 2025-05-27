package com.gestordeordenes.gestorordenes.service;

import com.gestordeordenes.gestorordenes.dto.OrdenCreacionDTO;
import com.gestordeordenes.gestorordenes.dto.OrdenActualizarEstadoDTO;
import com.gestordeordenes.gestorordenes.dto.OrdenVistaDTO;
import com.gestordeordenes.gestorordenes.enums.EstadoOrden;
import com.gestordeordenes.gestorordenes.exception.OrdenNotFoundException;
import com.gestordeordenes.gestorordenes.model.Dispositivo;
import com.gestordeordenes.gestorordenes.model.Orden;
import com.gestordeordenes.gestorordenes.model.Usuario;
import com.gestordeordenes.gestorordenes.repository.DispositivoRepository;
import com.gestordeordenes.gestorordenes.repository.OrdenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrdenService {

    private static final Logger logger = LoggerFactory.getLogger(OrdenService.class);

    private final OrdenRepository ordenRepository;
    private final DispositivoRepository dispositivoRepository;

    @Autowired
    public OrdenService(OrdenRepository ordenRepository, DispositivoRepository dispositivoRepository) {
        this.ordenRepository = ordenRepository;
        this.dispositivoRepository = dispositivoRepository;
    }

    public OrdenVistaDTO convertirAOrdenVistaDTO(Orden orden) {
        if (orden == null) {
            return null;
        }
        OrdenVistaDTO dto = new OrdenVistaDTO();
        dto.setId(orden.getId());
        dto.setNumeroOrden(orden.getNumeroOrden());

        dto.setClienteNombre(orden.getClienteNombre());
        dto.setClienteEmail(orden.getClienteEmail());
        dto.setClienteTelefono(orden.getClienteTelefono());

        if (orden.getDispositivo() != null) {
            dto.setDispositivoTipo(orden.getDispositivo().getTipo());
            dto.setDispositivoMarca(orden.getDispositivo().getMarca());
            dto.setDispositivoModelo(orden.getDispositivo().getModelo());
            dto.setDispositivoSerial(orden.getDispositivo().getSerial());
        }

        dto.setProblemaDescripcion(orden.getProblemaDescripcion());
        dto.setObservacionesRecepcion(orden.getObservacionesRecepcion());
        dto.setDiagnosticoTecnico(orden.getDiagnosticoTecnico());

        dto.setEstadoDisplayName(orden.getEstado().getDisplayName());
        dto.setFechaCreacion(orden.getFechaCreacion());
        dto.setFechaActualizacionEstado(orden.getFechaActualizacionEstado());

        if (orden.getTecnicoAsignado() != null) {
            dto.setTecnicoAsignadoNombre(orden.getTecnicoAsignado().getNombreCompleto());
        }
        return dto;
    }

    @Transactional
    public OrdenVistaDTO crearOrden(OrdenCreacionDTO creacionDTO, Usuario tecnicoLogueado) {
        Dispositivo dispositivo;
        String serialRecibido = creacionDTO.getDispositivoSerial();
        String serialNormalizadoParaGuardar = null; // Para almacenar el serial normalizado o null

        // 1. Normalizar y procesar el serial recibido
        if (serialRecibido != null && !serialRecibido.trim().isEmpty()) {
            serialNormalizadoParaGuardar = serialRecibido.trim().toUpperCase(); // NORMALIZAR A MAYÚSCULAS
        }
        // Si serialNormalizadoParaGuardar sigue siendo null, significa que el serial original era null, vacío o solo espacios.

        // 2. Manejar el Dispositivo
        if (serialNormalizadoParaGuardar != null) { // Solo buscar/crear con serial si tenemos uno válido y normalizado
            logger.info("Buscando dispositivo existente con serial normalizado: '{}'", serialNormalizadoParaGuardar);
            Optional<Dispositivo> optDispositivoExistente = dispositivoRepository.findBySerial(serialNormalizadoParaGuardar); // BÚSQUEDA CON SERIAL NORMALIZADO
            if (optDispositivoExistente.isPresent()) {
                dispositivo = optDispositivoExistente.get();
                logger.info("Dispositivo reutilizado con ID: {}, Serial: '{}'. Datos del formulario para tipo/marca/modelo serán ignorados para este dispositivo.",
                        dispositivo.getId(), serialNormalizadoParaGuardar);
            } else {
                // Serial proporcionado pero no encontrado, creamos uno nuevo con ese serial normalizado
                dispositivo = new Dispositivo();
                dispositivo.setTipo(creacionDTO.getDispositivoTipo());
                dispositivo.setMarca(creacionDTO.getDispositivoMarca());
                dispositivo.setModelo(creacionDTO.getDispositivoModelo());
                dispositivo.setSerial(serialNormalizadoParaGuardar); // GUARDAR SERIAL NORMALIZADO
                logger.info("Nuevo dispositivo creado con serial normalizado: {}", serialNormalizadoParaGuardar);
            }
        } else {
            // No se proporcionó serial válido (era null, vacío o solo espacios),
            // creamos un nuevo dispositivo con serial explícitamente NULL.
            dispositivo = new Dispositivo();
            dispositivo.setTipo(creacionDTO.getDispositivoTipo());
            dispositivo.setMarca(creacionDTO.getDispositivoMarca());
            dispositivo.setModelo(creacionDTO.getDispositivoModelo());
            dispositivo.setSerial(null); // Establecer a NULL explícitamente
            logger.info("Nuevo dispositivo creado con serial NULL (serial original era nulo, vacío o solo espacios).");
        }

        // 3. Crear la Orden
        Orden nuevaOrden = new Orden();
        nuevaOrden.setClienteNombre(creacionDTO.getClienteNombre());
        nuevaOrden.setClienteEmail(creacionDTO.getClienteEmail());
        nuevaOrden.setClienteTelefono(creacionDTO.getClienteTelefono());
        nuevaOrden.setDispositivo(dispositivo);
        nuevaOrden.setProblemaDescripcion(creacionDTO.getProblemaDescripcion());
        nuevaOrden.setObservacionesRecepcion(creacionDTO.getObservacionesRecepcion());
        nuevaOrden.setTecnicoAsignado(tecnicoLogueado);

        Orden ordenGuardada = ordenRepository.save(nuevaOrden);
        logger.info("Orden creada con número: {}", ordenGuardada.getNumeroOrden());
        return convertirAOrdenVistaDTO(ordenGuardada);
    }

    @Transactional(readOnly = true)
    public Optional<OrdenVistaDTO> buscarOrdenPorNumeroParaCliente(String numeroOrden) {
        // Considerar normalizar numeroOrden si los números de orden también pueden tener variaciones de mayúsculas/minúsculas
        return ordenRepository.findByNumeroOrden(numeroOrden)
                .map(this::convertirAOrdenVistaDTO);
    }

    @Transactional(readOnly = true)
    public Optional<OrdenVistaDTO> buscarOrdenPorIdParaTecnicoYConvertir(Long ordenId) {
        return ordenRepository.findById(ordenId)
                .map(this::convertirAOrdenVistaDTO);
    }

    @Transactional(readOnly = true)
    public Optional<Orden> buscarEntidadOrdenPorId(Long ordenId) {
        return ordenRepository.findById(ordenId);
    }

    @Transactional
    public OrdenVistaDTO actualizarOrdenPorTecnico(Long ordenId, OrdenActualizarEstadoDTO actualizarDTO, Usuario tecnicoLogueado) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new OrdenNotFoundException("Orden no encontrada con ID: " + ordenId));

        if (actualizarDTO.getNuevoEstado() != null) {
            orden.cambiarEstado(actualizarDTO.getNuevoEstado());
            logger.info("Estado de orden {} cambiado a {} por técnico {}", orden.getNumeroOrden(), actualizarDTO.getNuevoEstado(), tecnicoLogueado.getEmail());
        }
        if (actualizarDTO.getDiagnosticoTecnico() != null) {
            if (!actualizarDTO.getDiagnosticoTecnico().trim().isEmpty()) {
                orden.setDiagnosticoTecnico(actualizarDTO.getDiagnosticoTecnico().trim());
                logger.info("Diagnóstico de orden {} actualizado por técnico {}", orden.getNumeroOrden(), tecnicoLogueado.getEmail());
            } else {
                orden.setDiagnosticoTecnico(null);
                logger.info("Diagnóstico de orden {} eliminado por técnico {}", orden.getNumeroOrden(), tecnicoLogueado.getEmail());
            }
        }

        if (orden.getTecnicoAsignado() == null && (orden.getEstado() != EstadoOrden.RECIBIDO || orden.getEstado() == EstadoOrden.RECIBIDO)) {
            orden.setTecnicoAsignado(tecnicoLogueado);
        }

        if (actualizarDTO.getNuevoEstado() == null && actualizarDTO.getDiagnosticoTecnico() != null) {
            orden.setFechaActualizacionEstado(LocalDateTime.now());
        }

        Orden ordenActualizada = ordenRepository.save(orden);
        return convertirAOrdenVistaDTO(ordenActualizada);
    }

    @Transactional(readOnly = true)
    public List<OrdenVistaDTO> obtenerOrdenesPorTecnico(Usuario tecnico) {
        return ordenRepository.findByTecnicoAsignado(tecnico).stream()
                .map(this::convertirAOrdenVistaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrdenVistaDTO> obtenerTodasLasOrdenes() {
        return ordenRepository.findAllByOrderByFechaCreacionDesc().stream()
                .map(this::convertirAOrdenVistaDTO)
                .collect(Collectors.toList());
    }
}
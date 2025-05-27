// En UsuarioEdicionDTO.java
package com.gestordeordenes.gestorordenes.dto;

import com.gestordeordenes.gestorordenes.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size; // Sigue siendo necesario para nombreCompleto
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEdicionDTO {

    @NotNull
    private Long id;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre completo debe tener entre 3 y 100 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser una dirección de email válida")
    private String email;

    // SIN @Size aquí para que sea opcional
    private String nuevaPassword;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;

    private boolean activo;
}
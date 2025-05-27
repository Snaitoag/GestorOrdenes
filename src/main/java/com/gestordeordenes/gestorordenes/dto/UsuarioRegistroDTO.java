package com.gestordeordenes.gestorordenes.dto;

import com.gestordeordenes.gestorordenes.enums.Rol; // Asegúrate de importar tu enum Rol
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRegistroDTO {

    private Long id; // Útil si queremos usar el mismo DTO para actualizar

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre completo debe tener entre 3 y 100 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser una dirección de email válida")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") // Puedes ajustar la longitud mínima
    private String password;

    // Podríamos añadir un campo para confirmar contraseña si quisiéramos:
    // private String confirmPassword;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol; // El administrador seleccionará el rol

    private boolean activo = true; // Por defecto activo, el admin podría cambiarlo
}
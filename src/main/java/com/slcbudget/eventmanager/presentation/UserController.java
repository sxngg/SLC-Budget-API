package com.slcbudget.eventmanager.presentation;

import com.slcbudget.eventmanager.domain.ERole;
import com.slcbudget.eventmanager.domain.RoleEntity;
import com.slcbudget.eventmanager.domain.UserEntity;
import com.slcbudget.eventmanager.domain.dto.CreateUserDTO;
import com.slcbudget.eventmanager.domain.dto.EditUserDTO;
import com.slcbudget.eventmanager.persistence.UserRepository;
import com.slcbudget.eventmanager.service.StorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//@CrossOrigin //TODO AVERIGUAR
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MediaController mediaController; // Suponiendo que "FileController" es el controlador de archivos


    @Autowired
    private StorageService storageService;

    @GetMapping("/all")
    public List <UserEntity> getAllUsers() {
        return (List<UserEntity>) userRepository.findAll();
    }

    @GetMapping("/{id}")
    public UserEntity getUserById(@PathVariable Long id) {
        return userRepository.findById(id).get();
    }

    @GetMapping("/email/{email}")
    public UserEntity getUserByEmail(@PathVariable String email) {
        return userRepository.findByEmail(email).get();
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody EditUserDTO updatedUser) {
        // Recuperar el usuario existente de la base de datos
        Optional<UserEntity> optionalUser = userRepository.findById(id);

        if (optionalUser.isPresent()) {
            UserEntity existingUser = optionalUser.get();

            // Actualizar los campos del usuario con los nuevos valores
            existingUser.setName(updatedUser.getName());
            existingUser.setLastName(updatedUser.getLastName());
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));

            // Guardar el usuario actualizado en la base de datos
            userRepository.save(existingUser);

            return ResponseEntity.ok("Usuario actualizado correctamente");
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@Valid @RequestPart("profileImage") MultipartFile profileImage,
                                        @RequestPart("createUserDTO") CreateUserDTO createUserDTO) {

        String imageUrl = null;
        try {
            imageUrl = storageService.store(profileImage);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar la imagen de perfil");
        }

        Set<RoleEntity> roles = createUserDTO.getRoles().stream()
                .map(role -> RoleEntity.builder()
                        .name(ERole.valueOf(role))
                        .build())
                .collect(Collectors.toSet());

        UserEntity userEntity = UserEntity.builder()
                .username(createUserDTO.getUsername())
                .password(passwordEncoder.encode(createUserDTO.getPassword()))
                .email(createUserDTO.getEmail())
                .name(createUserDTO.getName())
                .lastName(createUserDTO.getLastName())
                .profileImage(imageUrl)
                .roles(roles)
                .build();

        userRepository.save(userEntity);

        return ResponseEntity.ok(userEntity);
    }


    @DeleteMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") String id){
        userRepository.deleteById(Long.parseLong(id));
        return "Se ha borrado el user con id".concat(id);
    }
}

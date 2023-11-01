package com.slcbudget.eventmanager.presentation;

import com.slcbudget.eventmanager.domain.ERole;
import com.slcbudget.eventmanager.domain.RoleEntity;
import com.slcbudget.eventmanager.domain.UserEntity;
import com.slcbudget.eventmanager.domain.dto.AddContactDTO;
import com.slcbudget.eventmanager.domain.dto.CreateUserDTO;
import com.slcbudget.eventmanager.domain.dto.EditUserDTO;
import com.slcbudget.eventmanager.persistence.UserRepository;
import com.slcbudget.eventmanager.service.StorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
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
    private MediaController mediaController;

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
    public ResponseEntity<String> updateUser(@PathVariable Long id,
                                             @RequestPart EditUserDTO updatedUser,
                                             @RequestPart(required = false) MultipartFile profileImage) {
        // Recuperar el usuario existente de la base de datos
        Optional<UserEntity> optionalUser = userRepository.findById(id);

        if (optionalUser.isPresent()) {
            UserEntity existingUser = optionalUser.get();

            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    String imageName = storageService.store(profileImage);

                    existingUser.setProfileImage(imageName);
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al almacenar la imagen");
                }
            }

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

    @GetMapping("users/{userId}/contacts")
    public ResponseEntity<Set<UserEntity>> getContactsByUserId(@PathVariable Long userId) {

        Optional<UserEntity> user = userRepository.findById(userId);

        if (user.isPresent()) {
            Set<UserEntity> contacts = user.get().getContacts();
            return ResponseEntity.ok(contacts);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("users/{userId}/add-contact")
    public ResponseEntity<?> addContact(@PathVariable Long userId, @RequestBody AddContactDTO contactId) {

        Optional<UserEntity> userOptional = userRepository.findById(userId);
        Optional<UserEntity> contactOptional = userRepository.findById(contactId.contactId());

        if (userOptional.isPresent() && contactOptional.isPresent()) {
            UserEntity user = userOptional.get();
            UserEntity contact = contactOptional.get();

            if (user.getId().equals(contact.getId())) {
                return ResponseEntity.badRequest().body("No puedes agregarte a ti mismo");
            }
            user.getContacts().add(contact);
            userRepository.save(user);

            return ResponseEntity.ok("Co0acto agregado con éxito");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

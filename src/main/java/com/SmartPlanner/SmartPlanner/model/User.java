package com.SmartPlanner.SmartPlanner.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * USER MODEL - Database mein user ka data store hota hai
 *
 * @Document - MongoDB collection ka naam (jaise SQL mein table)
 * @Data - Lombok: Automatically getter, setter, toString generate karta hai
 * @Id - Primary key (unique identifier)
 * @Indexed(unique=true) - Unique constraint (duplicate email nahi ho sakti)
 */
@Data                           // Getter/Setter auto-generate
@NoArgsConstructor              // Empty constructor: new User()
@AllArgsConstructor// All fields constructor: new User(id, username, ...)
@Document(collection = "users") // MongoDB collection name = "users"
public class User {

    @Id                         // Primary Key - MongoDB automatically ObjectId generate karta hai
    private String id;

    private String username;

    @Indexed(unique = true)     // Email unique honi chahiye (duplicate nahi)
    private String email;

    private String password;    // Hashed password store hoga (plain text nahi)

    private Role role = Role.USER;  // Default role USER hai, ADMIN manually set hoga

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // User preferences
    private String temperatureUnit = "C";  // C = Celsius, F = Fahrenheit
    private String speedUnit = "km/h";     // km/h ya mph
    private String theme = "light";        // light ya dark
}
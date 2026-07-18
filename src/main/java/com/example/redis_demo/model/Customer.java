package com.example.redis_demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.io.Serializable;

// Serializable là để Redis hiểu cách chuyển đổi class này
@Getter
@Setter
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Customer implements Serializable {
    @Id
    private String id;
    private String name;
}

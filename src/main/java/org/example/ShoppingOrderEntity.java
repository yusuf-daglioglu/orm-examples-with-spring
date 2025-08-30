package org.example;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;

import java.util.List;

@Entity(name = "ShoppingOrder")
@Data
public class ShoppingOrderEntity {

    @Id
    @SequenceGenerator(name = "SEQ_GEN_1", sequenceName = "SEQ_1", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GEN_1")
    @Column
    private Long id;

    @Column
    private String name;

    @Column
    private Boolean delivered;

    @OneToMany(mappedBy="shoppingOrder", fetch = FetchType.LAZY)
    private List<ItemEntity> itemList;
}
package entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table (name = "vendas")
public class Venda {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)

    private Long id;
    private LocalDateTime data;
    private Double total;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemVenda> itens;



}

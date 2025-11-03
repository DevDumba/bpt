package com.example.bpt.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "source_old_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal sourceOldBalance;

    @Column(name = "source_new_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal sourceNewBalance;

    @Column(name = "destination_old_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal destinationOldBalance;

    @Column(name = "destination_new_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal destinationNewBalance;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne(optional = false)
    @JoinColumn(name = "performed_by")
    private User performedBy;
}

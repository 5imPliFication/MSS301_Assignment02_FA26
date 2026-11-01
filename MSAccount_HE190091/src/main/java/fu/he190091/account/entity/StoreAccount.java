package fu.he190091.account.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "StoreAccounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccountID")
    private Integer accountId;

    @Column(name = "Username", length = 20, nullable = false, unique = true)
    private String username;

    @Column(name = "Email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "Password", length = 100, nullable = false)
    private String password;

    @Column(name = "Role", nullable = false)
    private Integer role; // 1: Administrator, 2: Staff, 3: Member

    @Column(name = "IsActive", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}

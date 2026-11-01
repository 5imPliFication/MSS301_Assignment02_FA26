package fu.he190091.account.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDTO {
    private Integer accountId;
    private String username;
    private String email;
    private Integer role;
    private String roleName;
    private Boolean isActive;

    public static String resolveRoleName(Integer role) {
        if (role == null) return "UNKNOWN";
        return switch (role) {
            case 1 -> "ADMIN";
            case 2 -> "STAFF";
            case 3 -> "MEMBER";
            default -> "UNKNOWN";
        };
    }
}

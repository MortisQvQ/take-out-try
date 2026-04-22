package com.example.takeouttry.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserQuery {
    private Long id;
    private String username;
    private Integer role;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
}

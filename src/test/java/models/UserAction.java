package models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserAction {
    LOGIN("LOGIN"),
    ACTION("ACTION"),
    LOGOUT("LOGOUT");

    private final String value;
}

package ru.bicev.notes.dto;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoteDto {

    private Long id;

    private String userEmail;

    @NotBlank
    private String text;

    private List<String> tags;

    public void setTags(List<String> tags) {
        this.tags = tags.stream().map(String::toUpperCase).collect(Collectors.toList());
    }

}

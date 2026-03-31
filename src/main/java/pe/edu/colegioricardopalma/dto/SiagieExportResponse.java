package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiagieExportResponse {
    private UUID exportacionId;
    private String fileName;
    private String downloadUrl;
    private LocalDateTime generatedAt;
}

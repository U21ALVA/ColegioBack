package pe.edu.colegioricardopalma.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.*;
import pe.edu.colegioricardopalma.entity.*;
import pe.edu.colegioricardopalma.repository.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiagieExportService {

    private final ConfiguracionSiagieRepository configuracionSiagieRepository;
    private final ExportacionSiagieRepository exportacionSiagieRepository;
    private final UsuarioRepository usuarioRepository;
    private final AnioEscolarRepository anioEscolarRepository;
    private final BimestreRepository bimestreRepository;
    private final CursoRepository cursoRepository;
    private final AlumnoRepository alumnoRepository;
    private final NotaRepository notaRepository;
    private final GradoRepository gradoRepository;
    private final SeccionRepository seccionRepository;

    @Value("${siagie.template-path:/home/alva/Documentos/ColegioRP/PLantilla siagie.xlsx}")
    private String templatePath;

    @Value("${siagie.storage-dir:/tmp/siagie}")
    private String storageDir;

    @Transactional
    public SiagieExportResponse exportar(SiagieExportRequest request, String username) {
        ConfiguracionSiagie config = configuracionSiagieRepository.findTopByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new IllegalStateException("Debe configurar SIAGIE antes de exportar"));

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));

        AnioEscolar anioEscolar = anioEscolarRepository.findById(request.getAnioEscolarId())
                .orElseThrow(() -> new EntityNotFoundException("Año escolar no encontrado: " + request.getAnioEscolarId()));

        Bimestre bimestre = bimestreRepository.findByIdWithAnioEscolar(request.getBimestreId())
                .orElseThrow(() -> new EntityNotFoundException("Bimestre no encontrado: " + request.getBimestreId()));

        if (!bimestre.getAnioEscolar().getId().equals(anioEscolar.getId())) {
            throw new IllegalArgumentException("El bimestre no pertenece al año escolar seleccionado");
        }

        List<Curso> cursosSeleccionados = resolverCursos(request.getCursoIds());
        List<Alumno> alumnos = alumnoRepository.findActivosForSiagie(request.getGradoId(), request.getSeccionId());

        if (alumnos.isEmpty()) {
            throw new IllegalArgumentException("No hay alumnos activos para los filtros seleccionados");
        }

        Map<UUID, String> nombresCursos = cursosSeleccionados.stream()
                .collect(Collectors.toMap(Curso::getId, Curso::getNombre));

        String periodo = "Bimestre " + bimestre.getNumero() + " - Año " + anioEscolar.getAnio();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String formato = request.getFormato() != null ? request.getFormato().toUpperCase() : "XLSX";
        String extension = switch (formato) {
            case "CSV" -> "csv";
            case "PDF" -> "pdf";
            default -> "xlsx";
        };
        String fileName = "siagie_" + anioEscolar.getAnio() + "_b" + bimestre.getNumero() + "_" + time + "." + extension;

        Path dir = Paths.get(storageDir);
        Path targetFile = dir.resolve(fileName);

        List<CursoNotasData> cursosData = buildCursosData(cursosSeleccionados, alumnos, bimestre.getId());

        try {
            Files.createDirectories(dir);
            switch (formato) {
                case "CSV" -> generateCsv(targetFile, config, request, anioEscolar, bimestre, cursosData);
                case "PDF" -> generatePdf(targetFile, config, request, anioEscolar, bimestre, cursosData);
                default -> {
                    try (Workbook workbook = loadTemplateWorkbook()) {
                        fillGeneralidades(workbook, config, request, anioEscolar, bimestre, nombresCursos);
                        fillCursos(workbook, cursosSeleccionados, alumnos, bimestre.getId());
                        try (OutputStream os = Files.newOutputStream(targetFile)) {
                            workbook.write(os);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el archivo SIAGIE", e);
        }

        ExportacionSiagie exportacion = ExportacionSiagie.builder()
                .tipo(request.getTipo())
                .formato(formato)
                .periodo(periodo)
                .archivoUrl(fileName)
                .usuario(usuario)
                .fecha(LocalDateTime.now())
                .build();

        ExportacionSiagie saved = exportacionSiagieRepository.save(exportacion);

        return SiagieExportResponse.builder()
                .exportacionId(saved.getId())
                .fileName(fileName)
                .downloadUrl("/api/siagie/exportaciones/" + saved.getId() + "/download")
                .generatedAt(saved.getFecha())
                .build();
    }

    public PageResponse<ExportacionSiagieDto> listExportaciones(Pageable pageable) {
        Page<ExportacionSiagieDto> page = exportacionSiagieRepository.findAllWithUsuario(pageable)
                .map(ExportacionSiagieDto::fromEntity);
        return PageResponse.from(page);
    }

    public ExportFileData getExportFile(UUID exportacionId) {
        ExportacionSiagie exportacion = exportacionSiagieRepository.findByIdWithUsuario(exportacionId)
                .orElseThrow(() -> new EntityNotFoundException("Exportación SIAGIE no encontrada: " + exportacionId));

        Path filePath = Paths.get(storageDir).resolve(exportacion.getArchivoUrl());
        if (!Files.exists(filePath)) {
            throw new EntityNotFoundException("Archivo de exportación no encontrado en storage");
        }

        String contentType = switch ((exportacion.getFormato() != null ? exportacion.getFormato() : "XLSX").toUpperCase()) {
            case "CSV" -> "text/csv";
            case "PDF" -> "application/pdf";
            default -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        };

        return new ExportFileData(filePath, exportacion.getArchivoUrl(), contentType);
    }

    private List<CursoNotasData> buildCursosData(List<Curso> cursos, List<Alumno> alumnos, UUID bimestreId) {
        List<UUID> alumnoIds = alumnos.stream().map(Alumno::getId).toList();
        List<CursoNotasData> out = new ArrayList<>();
        for (Curso curso : cursos) {
            List<Nota> notas = alumnoIds.isEmpty()
                    ? List.of()
                    : notaRepository.findByCursoBimestreAndAlumnoIdsWithAlumno(curso.getId(), bimestreId, alumnoIds);
            Map<UUID, Nota> notaPorAlumno = notas.stream()
                    .collect(Collectors.toMap(n -> n.getAlumno().getId(), n -> n, (a, b) -> a));
            out.add(new CursoNotasData(curso, notaPorAlumno, alumnos));
        }
        return out;
    }

    private void generateCsv(Path file, ConfiguracionSiagie config, SiagieExportRequest request,
                             AnioEscolar anioEscolar, Bimestre bimestre, List<CursoNotasData> cursosData) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("INSTITUCION,").append(escapeCsv(config.getInstitucionEducativa())).append("\n");
        sb.append("CODIGO_MODULAR,").append(escapeCsv(config.getCodigoModularAnexo())).append("\n");
        sb.append("NIVEL,").append(escapeCsv(config.getNivel())).append("\n");
        sb.append("ANIO,").append(anioEscolar.getAnio()).append("\n");
        sb.append("BIMESTRE,").append(bimestre.getNumero()).append("\n\n");

        for (CursoNotasData data : cursosData) {
            sb.append("CURSO,").append(escapeCsv(data.curso().getNombre())).append("\n");
            sb.append("NRO,CODIGO,ALUMNO,N1,N2,N3,N4,FINAL\n");
            int i = 1;
            for (Alumno alumno : data.alumnos()) {
                Nota n = data.notaPorAlumno().get(alumno.getId());
                sb.append(i++).append(',')
                        .append(escapeCsv(safe(alumno.getCodigoEstudiante()))).append(',')
                        .append(escapeCsv(alumno.getApellidos() + " " + alumno.getNombres())).append(',')
                        .append(n != null && n.getN1() != null ? n.getN1() : "").append(',')
                        .append(n != null && n.getN2() != null ? n.getN2() : "").append(',')
                        .append(n != null && n.getN3() != null ? n.getN3() : "").append(',')
                        .append(n != null && n.getN4() != null ? n.getN4() : "").append(',')
                        .append(n != null && n.getNotaFinal() != null ? n.getNotaFinal() : "")
                        .append("\n");
            }
            sb.append("\n");
        }

        Files.writeString(file, sb.toString());
    }

    private void generatePdf(Path file, ConfiguracionSiagie config, SiagieExportRequest request,
                             AnioEscolar anioEscolar, Bimestre bimestre, List<CursoNotasData> cursosData) throws IOException {
        Document doc = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
        try {
            PdfWriter.getInstance(doc, Files.newOutputStream(file));
            doc.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font subtitle = FontFactory.getFont(FontFactory.HELVETICA, 10);

            doc.add(new Paragraph("REPORTE INSTITUCIONAL SIAGIE", title));
            doc.add(new Paragraph(config.getInstitucionEducativa(), subtitle));
            doc.add(new Paragraph("Año: " + anioEscolar.getAnio() + " | Bimestre: " + bimestre.getNumero(), subtitle));
            doc.add(new Paragraph(" "));

            for (CursoNotasData data : cursosData) {
                doc.add(new Paragraph("Curso: " + data.curso().getNombre(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
                PdfPTable table = new PdfPTable(new float[]{1.2f, 2f, 4f, 1f, 1f, 1f, 1f, 1f});
                table.setWidthPercentage(100);
                for (String h : List.of("N°", "Código", "Alumno", "N1", "N2", "N3", "N4", "Final")) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
                    table.addCell(cell);
                }
                int i = 1;
                for (Alumno alumno : data.alumnos()) {
                    Nota n = data.notaPorAlumno().get(alumno.getId());
                    table.addCell(String.valueOf(i++));
                    table.addCell(safe(alumno.getCodigoEstudiante()));
                    table.addCell(alumno.getApellidos() + " " + alumno.getNombres());
                    table.addCell(n != null && n.getN1() != null ? String.valueOf(n.getN1()) : "-");
                    table.addCell(n != null && n.getN2() != null ? String.valueOf(n.getN2()) : "-");
                    table.addCell(n != null && n.getN3() != null ? String.valueOf(n.getN3()) : "-");
                    table.addCell(n != null && n.getN4() != null ? String.valueOf(n.getN4()) : "-");
                    table.addCell(n != null && n.getNotaFinal() != null ? String.valueOf(n.getNotaFinal()) : "-");
                }
                doc.add(table);
                doc.add(new Paragraph(" "));
            }
        } catch (Exception e) {
            throw new IOException("Error al generar PDF SIAGIE", e);
        } finally {
            doc.close();
        }
    }

    private String escapeCsv(String value) {
        String v = value == null ? "" : value;
        return '"' + v.replace("\"", "\"\"") + '"';
    }

    public List<CursoDto> cursosDisponibles() {
        return cursoRepository.findAllActiveOrderByNivelAndNombre().stream()
                .map(CursoDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filtros() {
        Map<String, Object> payload = new HashMap<>();
        List<AnioEscolar> anios = bootstrapAnioEscolarConBimestres2026();

        payload.put("anios", anios.stream().map(AnioEscolarDto::fromEntity).toList());

        List<Bimestre> bimestres = bimestreRepository.findByAnioEscolarActivo();
        if (bimestres.isEmpty() && !anios.isEmpty()) {
            bimestres = bimestreRepository.findByAnioEscolarIdOrderByNumeroAsc(anios.get(0).getId());
        }
        payload.put("bimestres", bimestres.stream().map(BimestreDto::fromEntity).toList());

        List<Grado> grados = gradoRepository.findAllOrderByOrden();
        payload.put("grados", grados.stream().map(GradoDto::fromEntity).toList());

        List<Seccion> secciones = seccionRepository.findAll();
        payload.put("secciones", secciones.stream().map(SeccionDto::fromEntity).toList());
        return payload;
    }

    @Transactional(readOnly = true)
    protected List<AnioEscolar> bootstrapAnioEscolarConBimestres2026() {
        return anioEscolarRepository.findAllOrderByAnioDesc();
    }

    private List<Curso> resolverCursos(List<UUID> cursoIds) {
        if (cursoIds != null && cursoIds.size() > 3) {
            throw new IllegalArgumentException("Solo se permiten hasta 3 cursos por exportación");
        }

        List<Curso> activosOrdenados = cursoRepository.findAllActiveOrderByNivelAndNombre();
        if (activosOrdenados.isEmpty()) {
            throw new IllegalStateException("No existen cursos activos para exportar");
        }

        if (cursoIds == null || cursoIds.isEmpty()) {
            return activosOrdenados.stream().limit(3).toList();
        }

        Map<UUID, Curso> cursosMap = activosOrdenados.stream().collect(Collectors.toMap(Curso::getId, c -> c));
        List<Curso> selected = new ArrayList<>();
        for (UUID id : cursoIds) {
            Curso curso = cursosMap.get(id);
            if (curso == null) {
                throw new EntityNotFoundException("Curso no activo/no encontrado: " + id);
            }
            selected.add(curso);
        }

        if (selected.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un curso");
        }

        return selected;
    }

    private Workbook loadTemplateWorkbook() throws IOException {
        Path path = Paths.get(templatePath);
        if (Files.exists(path)) {
            try (InputStream is = new FileInputStream(path.toFile())) {
                return new XSSFWorkbook(is);
            }
        }

        Resource cpResource = new ClassPathResource("PLantilla siagie.xlsx");
        if (cpResource.exists()) {
            try (InputStream is = cpResource.getInputStream()) {
                return new XSSFWorkbook(is);
            }
        }

        throw new IOException("No se encontró la plantilla SIAGIE en path absoluto ni classpath");
    }

    private void fillGeneralidades(
            Workbook workbook,
            ConfiguracionSiagie config,
            SiagieExportRequest request,
            AnioEscolar anioEscolar,
            Bimestre bimestre,
            Map<UUID, String> cursos
    ) {
        Sheet sheet = workbook.getSheet("Generalidades");
        if (sheet == null) {
            sheet = workbook.createSheet("Generalidades");
        }

        int rowIndex = 1;
        rowIndex = writeKeyValue(sheet, rowIndex, "Institución Educativa", config.getInstitucionEducativa());
        rowIndex = writeKeyValue(sheet, rowIndex, "Código Modular/Anexo", config.getCodigoModularAnexo());
        rowIndex = writeKeyValue(sheet, rowIndex, "Nivel", config.getNivel());
        rowIndex = writeKeyValue(sheet, rowIndex, "Nombre Reporte", config.getNombreReporte());
        rowIndex = writeKeyValue(sheet, rowIndex, "Año Académico (config)", String.valueOf(config.getAnioAcademico()));
        rowIndex = writeKeyValue(sheet, rowIndex, "Diseño Modular", safe(config.getDisenoModular()));
        rowIndex = writeKeyValue(sheet, rowIndex, "Tipo", request.getTipo());
        rowIndex = writeKeyValue(sheet, rowIndex, "Año Escolar", String.valueOf(anioEscolar.getAnio()));
        rowIndex = writeKeyValue(sheet, rowIndex, "Bimestre", String.valueOf(bimestre.getNumero()));
        rowIndex = writeKeyValue(sheet, rowIndex, "Cursos", String.join(", ", cursos.values()));
        rowIndex = writeKeyValue(sheet, rowIndex, "Periodo", safe(config.getPeriodo()));
        rowIndex = writeKeyValue(sheet, rowIndex, "Grado", safe(config.getGrado()));
        writeKeyValue(sheet, rowIndex, "Sección", safe(config.getSeccion()));
    }

    private int writeKeyValue(Sheet sheet, int rowIndex, String key, String value) {
        Row row = getOrCreateRow(sheet, rowIndex);
        setCell(row, 0, key);
        setCell(row, 1, value);
        return rowIndex + 1;
    }

    private void fillCursos(Workbook workbook, List<Curso> cursos, List<Alumno> alumnos, UUID bimestreId) {
        List<UUID> alumnoIds = alumnos.stream().map(Alumno::getId).toList();

        for (int i = 0; i < 3; i++) {
            Sheet sheet = workbook.getSheet("Curso " + (i + 1));
            if (sheet == null) {
                sheet = workbook.createSheet("Curso " + (i + 1));
            }

            if (i >= cursos.size()) {
                continue;
            }

            Curso curso = cursos.get(i);
            List<Nota> notas = alumnoIds.isEmpty()
                    ? List.of()
                    : notaRepository.findByCursoBimestreAndAlumnoIdsWithAlumno(curso.getId(), bimestreId, alumnoIds);

            Map<UUID, Nota> notaPorAlumno = notas.stream()
                    .collect(Collectors.toMap(n -> n.getAlumno().getId(), n -> n, (a, b) -> a));

            Row titleRow = getOrCreateRow(sheet, 0);
            setCell(titleRow, 0, "Curso: " + curso.getNombre());

            int rowIndex = 1;
            for (Alumno alumno : alumnos) {
                Nota nota = notaPorAlumno.get(alumno.getId());
                Row row = getOrCreateRow(sheet, rowIndex++);

                setCell(row, 0, rowIndex - 1);
                setCell(row, 1, safe(alumno.getCodigoEstudiante()));
                setCell(row, 2, alumno.getApellidos() + " " + alumno.getNombres());
                setCell(row, 3, nota != null ? nota.getN1() : null);
                setCell(row, 4, nota != null ? nota.getN2() : null);
                setCell(row, 5, nota != null ? nota.getN3() : null);
                setCell(row, 6, nota != null ? nota.getN4() : null);
                setCell(row, 7, nota != null ? nota.getNotaFinal() : null);
            }
        }
    }

    private Row getOrCreateRow(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        return row != null ? row : sheet.createRow(rowIndex);
    }

    private void setCell(Row row, int col, String value) {
        Cell cell = row.getCell(col);
        if (cell == null) cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
    }

    private void setCell(Row row, int col, Integer value) {
        Cell cell = row.getCell(col);
        if (cell == null) cell = row.createCell(col);
        if (value == null) {
            cell.setBlank();
        } else {
            cell.setCellValue(value);
        }
    }

    private void setCell(Row row, int col, BigDecimal value) {
        Cell cell = row.getCell(col);
        if (cell == null) cell = row.createCell(col);
        if (value == null) {
            cell.setBlank();
        } else {
            cell.setCellValue(value.doubleValue());
        }
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    @Getter
    @AllArgsConstructor
    public static class ExportFileData {
        private Path filePath;
        private String fileName;
        private String contentType;
    }

    private record CursoNotasData(Curso curso, Map<UUID, Nota> notaPorAlumno, List<Alumno> alumnos) {}
}

package pe.edu.colegioricardopalma.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.colegioricardopalma.dto.ActividadRecienteDto;
import pe.edu.colegioricardopalma.service.DashboardService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/actividad-reciente")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ActividadRecienteDto> getActividadReciente(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return dashboardService.getActividadReciente(limit);
    }
}

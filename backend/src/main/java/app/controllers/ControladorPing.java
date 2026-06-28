package app.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ControladorPing {

    /**
     * Endpoint de verificación de disponibilidad del servidor.
     *
     * @return "pong" si el servidor está activo
     */
    @RequestMapping("/ping")
    public String ping() {
        return "pong";
    }

}

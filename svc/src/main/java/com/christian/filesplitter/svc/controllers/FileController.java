package com.christian.filesplitter.svc.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.christian.filesplitter.svc.segment.FileSegment;
import com.christian.filesplitter.svc.services.EmailService;
import com.christian.filesplitter.svc.services.FileService;

import jakarta.mail.MessagingException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                            @RequestParam("size") int segmentSize,
                            Model model) throws InterruptedException {
        // System.out.println("file: " + file);
        // System.out.println("size: " + segmentSize);
        try {
            // Simulación de procesamiento (espera 3 segundos)
            Thread.sleep(3000);
            List<FileSegment> segments = fileService.splitFile(file, segmentSize);
            model.addAttribute("segments", segments);
            model.addAttribute("originalName", file.getOriginalFilename());
            return "result"; // Mostrar los segmentos generados en la vista 'result.html'
        } catch (IOException e) {
            model.addAttribute("error", "Error al procesar el archivo: " + e.getMessage());
            return "index";
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) throws IOException {
        // System.out.println("filename: " + filename);
        Path filePath = Paths.get("uploads").resolve(filename).normalize();
        Resource resource;

        try {
            resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @PostMapping("/send-email")
    public String sendEmail(@RequestParam("email") String recipientEmail,
                            @RequestParam("originalName") String originalName,
                            Model model) {
        try {
            // Obtener los segmentos generados
            List<File> segments = fileService.getGeneratedSegments(originalName);

            // Enviar correo con los archivos adjuntos
            emailService.sendEmailWithAttachments(
                    recipientEmail,
                    "Segmentos del archivo " + originalName,
                    "Se adjuntan los segmentos del archivo " + originalName,
                    segments
            );

            model.addAttribute("message", "Correo enviado con éxito a " + recipientEmail);
        } catch (MessagingException e) {
            model.addAttribute("error", "Error al enviar el correo: " + e.getMessage());
        }

        return "result";
    }

    @PostMapping("/merge")
    public String mergeFiles(@RequestParam("originalName") String originalName, Model model) {
        try {
            File mergedFile = fileService.mergeFiles(originalName);
            model.addAttribute("message", "Archivo recombinado correctamente ");
            model.addAttribute("recombinedFileName", mergedFile.getName());
        } catch (IOException e) {
            model.addAttribute("error", "Error al recombinar el archivo: " + e.getMessage());
        }
        return "result";
    }

}

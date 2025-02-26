package com.christian.filesplitter.svc.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.christian.filesplitter.svc.segment.FileSegment;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class FileService {

    private static final String UPLOAD_DIR = "uploads/";

    public File mergeFiles(String originalName) throws IOException {
        String onlyName = originalName.substring(0, originalName.lastIndexOf('.'));
        File directory = new File(UPLOAD_DIR); // Directorio donde se guardan los archivos
        File[] segments = directory.listFiles((dir, name) -> name.startsWith(onlyName + "."));
    
        if (segments == null || segments.length == 0) {
            throw new IOException("No se encontraron segmentos para recombinar.");
        }
    
        Arrays.sort(segments, Comparator.comparing(File::getName)); // Ordenar los segmentos
    
        // Archivo final recombinado
        File mergedFile = new File(directory, originalName);
    
        // Detectar el sistema operativo
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder processBuilder;
    
        if (os.contains("win")) {
            // Windows: usar cmd.exe /c "copy /b file1 + file2 + file3 output"
            StringBuilder command = new StringBuilder("copy /b ");
            for (File segment : segments) {
                command.append("\"").append(segment.getAbsolutePath()).append("\" + ");
            }
            command.delete(command.length() - 3, command.length()); // Quitar último " + "
            command.append(" \"").append(mergedFile.getAbsolutePath()).append("\"");
    
            processBuilder = new ProcessBuilder("cmd.exe", "/c", command.toString());
    
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            // Linux/Mac: usar bash/sh -c "cat file1 file2 file3 > output"
            StringBuilder command = new StringBuilder("cat ");
            for (File segment : segments) {
                command.append("\"").append(segment.getAbsolutePath()).append("\" ");
            }
            command.append("> \"").append(mergedFile.getAbsolutePath()).append("\"");
    
            processBuilder = new ProcessBuilder("bash", "-c", command.toString());
    
        } else {
            throw new IOException("Sistema operativo no soportado para recombinación.");
        }
    
        // System.out.println("Ejecutando: " + processBuilder.command());
        
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
    
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // Mostrar salida del proceso
            }
        }
    
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Error al ejecutar el comando de recombinación. Código de salida: " + exitCode);
            }
        } catch (InterruptedException e) {
            throw new IOException("Proceso interrumpido durante la recombinación.", e);
        }
    
        return mergedFile;
    }


    public List<FileSegment> splitFile(MultipartFile file, int segmentSize) throws IOException {
        // List<File> segments = new ArrayList<>();
        List<FileSegment> segments = new ArrayList<>();

        // Crear directorio si no existe
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        //eliminar todos los archivos en el directorio
        File[] files = uploadDir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        // Obtener nombre base del archivo
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("El archivo no tiene un nombre válido.");
        }
        String baseName = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));

        // Leer el archivo en bytes
        InputStream inputStream = file.getInputStream();
        byte[] buffer = new byte[segmentSize];
        int bytesRead;
        int partNumber = 0;

        while ((bytesRead = inputStream.read(buffer)) > 0) {
            String segmentName = baseName + "." + String.format("%010d", partNumber) + extension;
            File segment = new File(UPLOAD_DIR + segmentName);
            try (FileOutputStream outputStream = new FileOutputStream(segment)) {
                outputStream.write(buffer, 0, bytesRead);
            }
            String downloadUrl = "/download/" + segmentName;
            segments.add(new FileSegment(segmentName, downloadUrl));
            // segments.add(segment);
            partNumber++;
        }
        return segments;
    }

    public List<File> getGeneratedSegments(String originalFilename) {
        String baseName = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        File uploadDir = new File(UPLOAD_DIR);
        // File[] files = uploadDir.listFiles((dir, name) -> name.startsWith(originalFilename + "."));
        File[] files = uploadDir.listFiles((dir, name) -> name.matches(Pattern.quote(baseName) + "\\.\\d{10}.*"));
    
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName)); // Ordenar correctamente
            return List.of(files);
        }
    
        return List.of();
    }
}

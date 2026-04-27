package com.itaccess.service;

import com.itaccess.dto.ApkFileDTO;
import com.itaccess.entity.ApkFile;
import com.itaccess.exception.ResourceNotFoundException;
import com.itaccess.repository.ApkFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApkService {
    
    private final ApkFileRepository apkFileRepository;
    
    @Value("${app.upload.dir:uploads/apk}")
    private String uploadDir;
    
    public ApkFileDTO uploadApk(MultipartFile file, Long applicationId, Long uploadedBy, 
                                  String version, String packageName, String description) throws IOException {
        log.info("Starting APK upload: file={}, size={}, user={}", file.getOriginalFilename(), file.getSize(), uploadedBy);
        
        // Créer le répertoire s'il n'existe pas
        Path uploadPath = Paths.get(uploadDir);
        log.info("Upload directory: {}", uploadPath.toAbsolutePath());
        
        if (!Files.exists(uploadPath)) {
            log.info("Directory does not exist, creating: {}", uploadPath);
            try {
                Files.createDirectories(uploadPath);
                log.info("Directory created successfully");
            } catch (IOException e) {
                log.error("Failed to create upload directory: {}", e.getMessage(), e);
                throw new IOException("Impossible de créer le répertoire d'upload: " + e.getMessage());
            }
        }
        
        // Vérifier si le répertoire est accessible en écriture
        if (!Files.isWritable(uploadPath)) {
            log.error("Upload directory is not writable: {}", uploadPath);
            throw new IOException("Le répertoire d'upload n'est pas accessible en écriture");
        }
        
        // Générer un nom de fichier unique
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new IOException("Nom de fichier invalide");
        }
        
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFileName);
        
        log.info("Saving file to: {}", filePath.toAbsolutePath());
        
        // Sauvegarder le fichier
        try {
            Files.copy(file.getInputStream(), filePath);
            log.info("File saved successfully");
        } catch (IOException e) {
            log.error("Failed to save file: {}", e.getMessage(), e);
            throw new IOException("Impossible de sauvegarder le fichier: " + e.getMessage());
        }
        
        // Créer l'entité
        ApkFile apkFile = ApkFile.builder()
                .fileName(uniqueFileName)
                .originalFileName(originalFileName)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .version(version)
                .packageName(packageName)
                .description(description)
                .applicationId(applicationId)
                .uploadedBy(uploadedBy)
                .build();
        
        ApkFile saved = apkFileRepository.save(apkFile);
        log.info("APK uploaded successfully: {} by user {}", originalFileName, uploadedBy);
        
        return toDTO(saved);
    }
    
    public byte[] downloadApk(Long id) throws IOException {
        ApkFile apkFile = apkFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("APK non trouvé"));
        
        // Incrémenter le compteur de téléchargements
        apkFile.setDownloadCount(apkFile.getDownloadCount() + 1);
        apkFileRepository.save(apkFile);
        
        Path filePath = Paths.get(apkFile.getFilePath());
        return Files.readAllBytes(filePath);
    }
    
    public List<ApkFileDTO> getAllApks() {
        return apkFileRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<ApkFileDTO> getApksByApplication(Long applicationId) {
        return apkFileRepository.findByApplicationId(applicationId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public ApkFileDTO getApkById(Long id) {
        ApkFile apkFile = apkFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("APK non trouvé"));
        return toDTO(apkFile);
    }
    
    public void deleteApk(Long id) throws IOException {
        ApkFile apkFile = apkFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("APK non trouvé"));
        
        // Supprimer le fichier physique
        Path filePath = Paths.get(apkFile.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        // Supprimer l'entité
        apkFileRepository.delete(apkFile);
        log.info("APK deleted: {}", apkFile.getOriginalFileName());
    }
    
    private ApkFileDTO toDTO(ApkFile apkFile) {
        return ApkFileDTO.builder()
                .id(apkFile.getId())
                .fileName(apkFile.getFileName())
                .originalFileName(apkFile.getOriginalFileName())
                .fileSize(apkFile.getFileSize())
                .version(apkFile.getVersion())
                .packageName(apkFile.getPackageName())
                .description(apkFile.getDescription())
                .applicationId(apkFile.getApplicationId())
                .uploadedBy(apkFile.getUploadedBy())
                .uploadDate(apkFile.getUploadDate())
                .downloadCount(apkFile.getDownloadCount())
                .build();
    }
}

package com.ubuntu.ubuntu_app.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ubuntu.ubuntu_app.infra.statuses.CloudinaryResponseDoc;
import com.ubuntu.ubuntu_app.service.ImageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Cloudinary")
@RestController
@RequestMapping("/api/cloudinary")
public class CloudinaryController {

    @Autowired
    private ImageService imageService;

    @Operation(summary = "Subir imagen a Cloudinary")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(defaultValue = CloudinaryResponseDoc.img_ok)))
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(defaultValue = CloudinaryResponseDoc.img_errors)))
    @PostMapping("/upload")
    public ResponseEntity<?> fileUploader(@RequestPart(required = true) MultipartFile[] images) throws IOException {
        return imageService.uploadImages(images);
    }

    @Operation(summary = "Eliminar imagen de Cloudinary")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(defaultValue = CloudinaryResponseDoc.img_deleted)))
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(defaultValue = CloudinaryResponseDoc.img_notFound)))
    @PostMapping("/delete")
    public ResponseEntity<?> deleteImage(@RequestParam(required = true) String public_id) throws IOException {
        return imageService.deleteImage(public_id);
    }

    @Operation(summary = "Reemplazar imagen de Cloudinary")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(defaultValue = CloudinaryResponseDoc.img_notFound)))
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(defaultValue = CloudinaryResponseDoc.img_edit_ok)))
    @PostMapping("/edit")
    public ResponseEntity<?> editImage(@RequestPart(required = true) MultipartFile image,
            @RequestParam(required = true) String public_id) throws IOException {
        return imageService.editImage(image, public_id);
    }
}

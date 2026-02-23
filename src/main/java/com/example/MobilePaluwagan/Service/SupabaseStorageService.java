package com.example.MobilePaluwagan.Service;

import jakarta.mail.Multipart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.print.DocFlavor;
import java.io.IOException;
import java.util.List;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucket;

    public String uploadProfileImage(MultipartFile file, long userId) throws IOException{

        long maxSize = 150 * 1024;
        if (file.getSize() > maxSize) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "file size exceeds 150kb limit");
        }

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Please select an image to upload");
        }

        List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/webp");
        if (!allowedTypes.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid file type! Only JPG, PNG, WEBP allowed");
        }

        String fileName = "profile_" + userId;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("Content-Type", file.getContentType());
        headers.set("x-upsert", "true");

        HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);

        String uploadUrl = supabaseUrl +  "/storage/v1/object/" + bucket + "/" + fileName;
        restTemplate.exchange(uploadUrl, HttpMethod.POST, entity, String.class);

        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + fileName;
    }


}

package org.shub.postservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final Cloudinary cloudinary;

    public CloudinaryUploadResult uploadPostImage(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "posts",
                            "resource_type", "image"
                    )
            );
            String url = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            return new CloudinaryUploadResult(url, publicId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload post image to Cloudinary", e);
        }
    }

    /**
     * Deletes an image from Cloudinary by its public_id - needed when a
     * post is deleted or its image is replaced, so orphaned files don't
     * accumulate in the Cloudinary account.
     */
    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image from Cloudinary", e);
        }
    }
}
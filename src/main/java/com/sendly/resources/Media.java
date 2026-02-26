package com.sendly.resources;

import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.MediaFile;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.io.File;
import java.net.URLConnection;

public class Media {
    private final Sendly client;

    public Media(Sendly client) {
        this.client = client;
    }

    /**
     * Upload a media file for MMS.
     *
     * @param file The file to upload
     * @return The uploaded media file metadata
     * @throws SendlyException if the request fails
     */
    public MediaFile upload(File file) throws SendlyException {
        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return upload(file, contentType);
    }

    /**
     * Upload a media file for MMS with an explicit content type.
     *
     * @param file        The file to upload
     * @param contentType The MIME content type of the file
     * @return The uploaded media file metadata
     * @throws SendlyException if the request fails
     */
    public MediaFile upload(File file, String contentType) throws SendlyException {
        if (file == null || !file.exists()) {
            throw new ValidationException("File is required and must exist");
        }
        if (contentType == null || contentType.isEmpty()) {
            throw new ValidationException("Content type is required");
        }

        RequestBody fileBody = RequestBody.create(file, okhttp3.MediaType.parse(contentType));
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        JsonObject response = client.postMultipart("/media", requestBody);
        return client.getGson().fromJson(response, MediaFile.class);
    }
}

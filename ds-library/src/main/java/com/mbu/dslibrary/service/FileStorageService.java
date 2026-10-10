package com.mbu.dslibrary.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mbu.dslibrary.exception.NotFoundException;
import com.mbu.dslibrary.model.StoredFile;
import com.mongodb.client.gridfs.model.GridFSFile;

/** Saves, finds and deletes uploaded files in MongoDB GridFS (so they survive redeploys). */
@Service
public class FileStorageService {

    private static final byte[] PDF_MAGIC = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private final GridFsTemplate gridFs;

    public FileStorageService(GridFsTemplate gridFs) {
        this.gridFs = gridFs;
    }

    /** Checks the upload really is a PDF (looks at the file header, not just the name) and stores it. */
    public StoredFile storePdf(MultipartFile file, String title) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please choose a PDF file.");
        }
        try (InputStream in = file.getInputStream()) {
            byte[] head = in.readNBytes(PDF_MAGIC.length);
            if (!Arrays.equals(head, PDF_MAGIC)) {
                throw new IllegalArgumentException("Only PDF files are allowed.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the uploaded file.");
        }
        String filename = cleanName(file.getOriginalFilename());
        try (InputStream in = file.getInputStream()) {
            ObjectId id = gridFs.store(in, filename, "application/pdf");
            String shownTitle = (title == null || title.isBlank()) ? filename : title.trim();
            return new StoredFile(id.toHexString(), filename, shownTitle, file.getSize());
        } catch (IOException e) {
            throw new IllegalStateException("Could not save the file.", e);
        }
    }

    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    /** Accepts PNG, JPEG, GIF or WebP up to 5 MB (detected from the file header; SVG is refused on purpose). */
    public StoredFile storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please choose an image file.");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Image is too large (limit 5 MB).");
        }
        String type;
        try (InputStream in = file.getInputStream()) {
            type = detectImageType(in.readNBytes(12));
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the uploaded file.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Only PNG, JPEG, GIF or WebP images are allowed.");
        }
        String filename = cleanName(file.getOriginalFilename());
        try (InputStream in = file.getInputStream()) {
            ObjectId id = gridFs.store(in, filename, type);
            return new StoredFile(id.toHexString(), filename, filename, file.getSize());
        } catch (IOException e) {
            throw new IllegalStateException("Could not save the file.", e);
        }
    }

    private String detectImageType(byte[] h) {
        if (h.length >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') return "image/png";
        if (h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) return "image/jpeg";
        if (h.length >= 4 && h[0] == 'G' && h[1] == 'I' && h[2] == 'F' && h[3] == '8') return "image/gif";
        if (h.length >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') return "image/webp";
        return null;
    }

    public GridFsResource load(String fileId) {
        GridFSFile f = find(fileId);
        if (f == null) throw new NotFoundException("File not found");
        return gridFs.getResource(f);
    }

    public void delete(String fileId) {
        if (fileId == null || !ObjectId.isValid(fileId)) return;
        gridFs.delete(Query.query(Criteria.where("_id").is(new ObjectId(fileId))));
    }

    private GridFSFile find(String fileId) {
        if (fileId == null || !ObjectId.isValid(fileId)) return null;
        return gridFs.findOne(Query.query(Criteria.where("_id").is(new ObjectId(fileId))));
    }

    /** Keeps only the file name (no folders) and replaces odd characters. */
    private String cleanName(String original) {
        String name = original == null ? "file.pdf" : original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[^A-Za-z0-9._ -]", "_").trim();
        if (name.isEmpty()) name = "file.pdf";
        return name.length() > 120 ? name.substring(name.length() - 120) : name;
    }
}

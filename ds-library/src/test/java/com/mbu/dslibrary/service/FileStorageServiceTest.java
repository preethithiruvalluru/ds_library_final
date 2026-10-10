package com.mbu.dslibrary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.mock.web.MockMultipartFile;

import com.mbu.dslibrary.model.StoredFile;

/** Uploads are judged by their first bytes, never by the file name or the type the browser claims. */
class FileStorageServiceTest {

    private GridFsTemplate gridFs;
    private FileStorageService service;

    @BeforeEach
    void setUp() {
        gridFs = mock(GridFsTemplate.class);
        when(gridFs.store(any(InputStream.class), anyString(), anyString())).thenReturn(new ObjectId());
        service = new FileStorageService(gridFs);
    }

    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static byte[] png() {
        return new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    }

    @Test
    void storesARealPdf() {
        MockMultipartFile f = new MockMultipartFile("file", "notes.pdf", "application/pdf", bytes("%PDF-1.4 hello"));
        StoredFile saved = service.storePdf(f, "My notes");
        assertEquals("notes.pdf", saved.getFilename());
        assertEquals("My notes", saved.getTitle());
        verify(gridFs).store(any(InputStream.class), eq("notes.pdf"), eq("application/pdf"));
    }

    @Test
    void refusesATextFileDisguisedAsPdf() {
        MockMultipartFile f = new MockMultipartFile("file", "evil.pdf", "application/pdf", bytes("<script>alert(1)</script>"));
        assertThrows(IllegalArgumentException.class, () -> service.storePdf(f, "x"));
        verify(gridFs, never()).store(any(InputStream.class), anyString(), anyString());
    }

    @Test
    void refusesEmptyUploads() {
        MockMultipartFile f = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> service.storePdf(f, "x"));
        assertThrows(IllegalArgumentException.class, () -> service.storePdf(null, "x"));
        assertThrows(IllegalArgumentException.class, () -> service.storeImage(null));
    }

    @Test
    void removesFoldersAndOddCharactersFromTheFileName() {
        MockMultipartFile f = new MockMultipartFile("file", "C:\\evil\\..\\..\\my file<1>.pdf", "application/pdf", bytes("%PDF-1.7"));
        StoredFile saved = service.storePdf(f, null);
        assertEquals("my file_1_.pdf", saved.getFilename());
        assertEquals("my file_1_.pdf", saved.getTitle());   // no title given: falls back to the clean file name
    }

    @Test
    void acceptsPngJpegGifAndWebp() {
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] gif = bytes("GIF89a......");
        byte[] webp = bytes("RIFF....WEBPVP8 ");
        service.storeImage(new MockMultipartFile("file", "a.png", "image/png", png()));
        service.storeImage(new MockMultipartFile("file", "a.jpg", "image/jpeg", jpeg));
        service.storeImage(new MockMultipartFile("file", "a.gif", "image/gif", gif));
        service.storeImage(new MockMultipartFile("file", "a.webp", "image/webp", webp));
        verify(gridFs).store(any(InputStream.class), eq("a.png"), eq("image/png"));
        verify(gridFs).store(any(InputStream.class), eq("a.jpg"), eq("image/jpeg"));
        verify(gridFs).store(any(InputStream.class), eq("a.gif"), eq("image/gif"));
        verify(gridFs).store(any(InputStream.class), eq("a.webp"), eq("image/webp"));
    }

    @Test
    void refusesSvgAndOtherNonImages() {
        MockMultipartFile svg = new MockMultipartFile("file", "x.svg", "image/svg+xml",
                bytes("<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>"));
        MockMultipartFile fakePng = new MockMultipartFile("file", "x.png", "image/png", bytes("<html>not an image</html>"));
        MockMultipartFile pdf = new MockMultipartFile("file", "x.png", "image/png", bytes("%PDF-1.4"));
        assertThrows(IllegalArgumentException.class, () -> service.storeImage(svg));
        assertThrows(IllegalArgumentException.class, () -> service.storeImage(fakePng));
        assertThrows(IllegalArgumentException.class, () -> service.storeImage(pdf));
        verify(gridFs, never()).store(any(InputStream.class), anyString(), anyString());
    }

    @Test
    void refusesImagesOverFiveMegabytes() {
        byte[] big = new byte[5 * 1024 * 1024 + 1];
        System.arraycopy(png(), 0, big, 0, 8);
        MockMultipartFile f = new MockMultipartFile("file", "big.png", "image/png", big);
        assertThrows(IllegalArgumentException.class, () -> service.storeImage(f));
        verify(gridFs, never()).store(any(InputStream.class), anyString(), anyString());
    }
}

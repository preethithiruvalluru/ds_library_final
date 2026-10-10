package com.mbu.dslibrary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** YouTube links are only accepted when they clearly point to ONE video on a real YouTube host. */
class ResourceServiceTest {

    private static final String ID = "dQw4w9WgXcQ";

    @Test
    void acceptsNormalYoutubeLinks() {
        assertEquals(ID, ResourceService.youtubeId("https://www.youtube.com/watch?v=" + ID));
        assertEquals(ID, ResourceService.youtubeId("https://youtube.com/watch?v=" + ID + "&t=30s"));
        assertEquals(ID, ResourceService.youtubeId("https://m.youtube.com/watch?feature=share&v=" + ID));
        assertEquals(ID, ResourceService.youtubeId("https://youtu.be/" + ID + "?t=5"));
        assertEquals(ID, ResourceService.youtubeId("https://www.youtube.com/shorts/" + ID));
        assertEquals(ID, ResourceService.youtubeId("https://www.youtube.com/embed/" + ID));
        assertEquals(ID, ResourceService.youtubeId("https://www.youtube-nocookie.com/embed/" + ID));
    }

    @Test
    void rejectsLookAlikeHosts() {
        assertNull(ResourceService.youtubeId("https://youtube.com.evil.com/watch?v=" + ID));
        assertNull(ResourceService.youtubeId("https://evil.com/watch?v=" + ID));
        assertNull(ResourceService.youtubeId("https://notyoutube.com/watch?v=" + ID));
    }

    @Test
    void rejectsLinksThatAreNotASingleVideo() {
        assertNull(ResourceService.youtubeId("https://www.youtube.com/playlist?list=PL12345"));
        assertNull(ResourceService.youtubeId("https://www.youtube.com/@somechannel"));
        assertNull(ResourceService.youtubeId("https://www.youtube.com/watch"));
        assertNull(ResourceService.youtubeId("https://youtu.be/"));
    }

    @Test
    void rejectsMalformedVideoIds() {
        assertNull(ResourceService.youtubeId("https://www.youtube.com/watch?v=short"));
        assertNull(ResourceService.youtubeId("https://www.youtube.com/watch?v=" + ID + "extra"));
        assertNull(ResourceService.youtubeId("https://www.youtube.com/watch?v=\"onload=alert(1)"));
    }

    @Test
    void rejectsGarbage() {
        assertNull(ResourceService.youtubeId("not a url"));
        assertNull(ResourceService.youtubeId(""));
    }
}

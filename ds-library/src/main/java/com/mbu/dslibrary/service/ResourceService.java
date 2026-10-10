package com.mbu.dslibrary.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mbu.dslibrary.dto.ResourceRequest;
import com.mbu.dslibrary.exception.NotFoundException;
import com.mbu.dslibrary.model.Resource;
import com.mbu.dslibrary.repository.ModuleRepository;
import com.mbu.dslibrary.repository.ResourceRepository;

@Service
public class ResourceService {

    private static final Pattern VIDEO_ID = Pattern.compile("^[A-Za-z0-9_-]{11}$");
    private static final Set<String> YT_HOSTS = Set.of("youtube.com", "www.youtube.com", "m.youtube.com", "youtu.be",
            "www.youtube-nocookie.com", "youtube-nocookie.com");

    private final ResourceRepository repo;
    private final ModuleRepository modules;
    private final FileStorageService files;
    private final MongoTemplate mongo;

    public ResourceService(ResourceRepository repo, ModuleRepository modules, FileStorageService files, MongoTemplate mongo) {
        this.repo = repo;
        this.modules = modules;
        this.files = files;
        this.mongo = mongo;
    }

    public List<Resource> search(String q, String type, String subject, String moduleId, String tag) {
        List<Criteria> parts = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            Pattern p = Pattern.compile(Pattern.quote(q.trim()), Pattern.CASE_INSENSITIVE);
            parts.add(new Criteria().orOperator(Criteria.where("title").regex(p), Criteria.where("description").regex(p),
                    Criteria.where("tags").regex(p)));
        }
        if (type != null && !type.isBlank()) parts.add(Criteria.where("type").is(type));
        if (subject != null && !subject.isBlank()) parts.add(Criteria.where("subject").is(subject));
        if (moduleId != null && !moduleId.isBlank()) parts.add(Criteria.where("moduleId").is(moduleId));
        if (tag != null && !tag.isBlank()) parts.add(Criteria.where("tags").is(tag.trim().toLowerCase()));
        Query query = new Query().with(Sort.by(Sort.Direction.DESC, "createdAt"));
        if (!parts.isEmpty()) query.addCriteria(new Criteria().andOperator(parts.toArray(new Criteria[0])));
        return mongo.find(query, Resource.class);
    }

    public List<String> subjects() {
        return mongo.findDistinct(new Query(Criteria.where("subject").nin(null, "")), "subject", Resource.class, String.class)
                .stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    public List<String> tags() {
        return mongo.findDistinct(new Query(), "tags", Resource.class, String.class).stream().sorted().toList();
    }

    public Resource get(String id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Resource not found"));
    }

    public Resource create(ResourceRequest req) {
        Resource r = new Resource();
        apply(r, req);
        return repo.save(r);
    }

    public Resource update(String id, ResourceRequest req) {
        Resource r = get(id);
        apply(r, req);
        if (!"PDF".equals(r.getType()) && r.getFile() != null) {   // no longer a PDF resource: drop the stored file
            files.delete(r.getFile().getFileId());
            r.setFile(null);
        }
        return repo.save(r);
    }

    public void delete(String id) {
        Resource r = get(id);
        if (r.getFile() != null) files.delete(r.getFile().getFileId());
        repo.delete(r);
    }

    public Resource setFile(String id, MultipartFile file) {
        Resource r = get(id);
        if (!"PDF".equals(r.getType())) {
            throw new IllegalArgumentException("Only resources of type PDF can have an uploaded file.");
        }
        var saved = files.storePdf(file, r.getTitle());
        if (r.getFile() != null) files.delete(r.getFile().getFileId());
        r.setFile(saved);
        return repo.save(r);
    }

    public Resource removeFile(String id) {
        Resource r = get(id);
        if (r.getFile() != null) files.delete(r.getFile().getFileId());
        r.setFile(null);
        return repo.save(r);
    }

    private void apply(Resource r, ResourceRequest req) {
        String moduleId = req.getModuleId() == null || req.getModuleId().isBlank() ? null : req.getModuleId();
        if (moduleId != null && !modules.existsById(moduleId)) {
            throw new IllegalArgumentException("The selected module does not exist.");
        }
        String type = req.getType();
        String url = req.getUrl() == null ? "" : req.getUrl().trim();
        String videoId = null;
        if ("PDF".equals(type)) {
            url = "";   // a PDF resource is an uploaded file, not a link
        } else if (url.isEmpty()) {
            throw new IllegalArgumentException("A URL is required for this resource type.");
        } else {
            try {
                URI.create(url).toURL();   // rejects malformed addresses
            } catch (Exception e) {
                throw new IllegalArgumentException("That URL does not look valid.");
            }
            if ("YOUTUBE".equals(type)) {
                videoId = youtubeId(url);
                if (videoId == null) throw new IllegalArgumentException("Could not find a YouTube video in that link.");
            }
        }
        Set<String> tags = new LinkedHashSet<>();
        for (String t : req.getTags()) {
            if (t != null && !t.isBlank()) tags.add(t.trim().toLowerCase());
        }
        r.setTitle(req.getTitle().trim());
        r.setDescription(req.getDescription() == null ? "" : req.getDescription().trim());
        r.setType(type);
        r.setSubject(req.getSubject() == null ? "" : req.getSubject().trim());
        r.setModuleId(moduleId);
        r.setUrl(url);
        r.setVideoId(videoId);
        r.setTags(new ArrayList<>(tags));
    }

    /** Returns the 11-character video id for youtube.com / youtu.be links, or null if it is not a single video. */
    static String youtubeId(String url) {
        try {
            URI u = URI.create(url);
            String host = u.getHost() == null ? "" : u.getHost().toLowerCase();
            if (!YT_HOSTS.contains(host)) return null;
            String path = u.getPath() == null ? "" : u.getPath();
            String id = null;
            if (host.equals("youtu.be")) {
                id = path.length() > 1 ? path.substring(1).split("/")[0] : null;
            } else if (path.equals("/watch") && u.getQuery() != null) {
                for (String kv : u.getQuery().split("&")) {
                    if (kv.startsWith("v=")) id = kv.substring(2);
                }
            } else {
                for (String prefix : new String[] {"/embed/", "/shorts/", "/live/"}) {
                    if (path.startsWith(prefix)) id = path.substring(prefix.length()).split("/")[0];
                }
            }
            return id != null && VIDEO_ID.matcher(id).matches() ? id : null;
        } catch (Exception e) {
            return null;
        }
    }
}

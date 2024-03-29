package dev.kinau.resourcepackvalidator.atlas;

import com.google.gson.Gson;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.utils.OverlayNamespace;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
@Slf4j
public class TextureAtlas {

    private final OverlayNamespace namespace;
    private AtlasData data;

    public TextureAtlas(OverlayNamespace namespace) {
        this.namespace = namespace;
        try {
            File atlasDir = FileUtils.Directory.ATLASES.getFile(namespace);
            File blocksAtlas = new File(atlasDir, "blocks.json");
            if (!blocksAtlas.exists()) return;
            this.data = new Gson().fromJson(new FileReader(blocksAtlas), AtlasData.class);
            data.sources().add(new AtlasSource("directory", "item", "", null, null));
            data.sources().add(new AtlasSource("directory", "block", "", null, null));
        } catch (Exception ex) {
            log.error("Could not load blocks atlas for " + namespace.getName() + " at " + namespace.getAssetsDir().getPath(), ex);
        }
    }

    public boolean isPartOfAtlas(File file) {
        if (data == null) return false;
        return data.sources().stream().anyMatch(atlasSource -> atlasSource.isInAtlas(namespace, file));
    }

    @Getter
    @Accessors(fluent = true)
    @ToString
    public static class AtlasData {
        private final List<AtlasSource> sources = new ArrayList<>();
    }

    //TODO: Add "filter" and "unstitch" type and use inherited classes
    @Getter
    @Accessors(fluent = true)
    @ToString
    @AllArgsConstructor
    public static class AtlasSource {
        private String type;

        private String source;
        private String prefix;

        private String resource;
        private String sprite;

        public boolean isInAtlas(OverlayNamespace namespace, File file) {
            String path = file.getPath();
            String namespacePath = namespace.getAssetsDir().getPath();
            path = path.replace(namespacePath, "").replace("/textures", "");
            if (path.startsWith("/"))
                path = path.substring(1);

            if (type.equals("single")) {
                if (resource != null) {
                    String resource = resource().replace("minecraft:", "");
                    if (!resource.endsWith(".png"))
                        resource = resource + ".png";
                    return resource.equals(path);
                } else {
                    return false;
                }
            } else if (type.equals("directory")) {
                if (source != null) {
                    String resource = source().replace("minecraft:", "");
                    if (resource.startsWith(File.separator))
                        resource = resource.substring(1);
                    if (!resource.endsWith(File.separator))
                        resource = resource + File.separator;
                    return path.startsWith(resource);
                } else {
                    return false;
                }
            }
            return true;
        }
    }
}

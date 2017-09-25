package nl.weeaboo.vn.buildtools.optimizer.image;

import java.io.File;
import java.io.IOException;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3NativesLoader;
import com.badlogic.gdx.graphics.Pixmap;
import com.google.common.io.Files;

import nl.weeaboo.common.Dim;
import nl.weeaboo.filesystem.FilePath;
import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.vn.buildtools.optimizer.image.encoder.JngEncoder;
import nl.weeaboo.vn.buildtools.project.NvlistProjectConnection;
import nl.weeaboo.vn.buildtools.project.ProjectFolderConfig;
import nl.weeaboo.vn.gdx.graphics.PixmapLoader;
import nl.weeaboo.vn.impl.InitConfig;
import nl.weeaboo.vn.impl.image.desc.ImageDefinition;

public final class RunImageOptimizer {

    public static void main(String[] args) throws IOException {
        InitConfig.init();
        Lwjgl3NativesLoader.load();

        File dstFolder = new File("tmp");
        dstFolder.mkdirs();

        // NVList root project
        ProjectFolderConfig folderConfig = new ProjectFolderConfig(new File(""), new File(""));
        try (NvlistProjectConnection connection = NvlistProjectConnection.openProject(folderConfig)) {
            IFileSystem resFileSystem = connection.getResFileSystem();

            String filename = "testjng.jng";
            Pixmap pixmap = PixmapLoader.load(resFileSystem, FilePath.of("img/" + filename));
            ImageWithDef imageWithDef = new ImageWithDef(pixmap, new ImageDefinition(filename,
                    Dim.of(pixmap.getWidth(), pixmap.getHeight())));

            ImageResizerConfig config = new ImageResizerConfig();
            config.setScaleFactor(0.5);
            ImageResizer resizer = new ImageResizer(config);
            ImageWithDef optimized = resizer.optimize(imageWithDef);
            System.out.println(optimized.getDef());

            JngEncoder jngEncoder = new JngEncoder();
            EncodedImage encoded = jngEncoder.encode(optimized);

            Files.write(encoded.readImageBytes(), new File(dstFolder, "out.jng"));
        }
    }

}

package nl.weeaboo.vn.buildtools.optimizer;

import nl.weeaboo.vn.buildtools.optimizer.image.ImageOptimizer;
import nl.weeaboo.vn.buildtools.project.NvlistProjectConnection;

public final class ResourceOptimizer implements IResourceOptimizer {

    @Override
    public void optimizeResources(IOptimizerContext context) {
        ImageOptimizer imageOptimizer = new ImageOptimizer(context);
        imageOptimizer.optimizeResources();

        // Any files that don't have a specific optimizer are copied to the output folder
        NvlistProjectConnection project = context.getProject();
        ResourceOptimizerConfig config = context.getConfig();
        new UnoptimizedFileCopier().copyOtherResources(project.getResFileSystem(), context.getFileSet(),
                config.getOutputFolder());
    }

}

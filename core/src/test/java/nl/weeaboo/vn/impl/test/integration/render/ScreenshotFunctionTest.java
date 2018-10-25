package nl.weeaboo.vn.impl.test.integration.render;

import org.junit.Test;

import nl.weeaboo.vn.impl.render.DrawBuffer;

public class ScreenshotFunctionTest extends RenderIntegrationTest {

    @Test
    public void takeScreenshot() {
        loadScript("integration/screenshot/screenshotfunction");

        DrawBuffer drawBuffer = getDrawBuffer();

        novel.draw(drawBuffer);
        render();
        waitForAllThreads();

        drawBuffer.reset();
        novel.draw(drawBuffer);
        render();
        checkRenderResult("screenshot-function");
    }

}

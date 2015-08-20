package nl.weeaboo.gdx.scene2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Scene2dEnv implements Disposable {

    private final Stage stage;
    private final Skin skin;

    public Scene2dEnv(Viewport viewport) {
        stage = new Stage(viewport);
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void dispose() {
        Gdx.input.setInputProcessor(null);

        stage.dispose();
        skin.dispose();
    }

    public Stage getStage() {
        return stage;
    }

    public Skin getSkin() {
        return skin;
    }

}
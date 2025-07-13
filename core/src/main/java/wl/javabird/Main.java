package wl.javabird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import wl.javabird.objects.Droplet;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Main extends ApplicationAdapter {
    private SpriteBatch spriteBatch;
    private Texture image;

    FitViewport viewport;

    Texture backgroundTexture;
    Texture bucketTexture;
    Texture dropTexture;
    Sound dropSfx;
    Music bgMusic;

    float worldWidth;
    float worldHeight;

    Sprite bucketSprite;

    Vector2 touchVector;

    Array<Droplet> droplets;

    float dropSpeed;
    float dropTimer;

    Rectangle bucketCollider;
    Rectangle dropCollider;

    BitmapFont scoreFont;
    int score;

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);
        image = new Texture("libgdx.png");
        backgroundTexture = new Texture("background.png");
        dropTexture = new Texture("drop.png");
        dropSfx = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));

        bgMusic = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        bgMusic.setLooping(true);
        bgMusic.setVolume(0.5f);
        bgMusic.play();

        worldWidth = viewport.getWorldWidth();
        worldHeight = viewport.getWorldHeight();

        bucketTexture = new Texture("bucket.png");
        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(1, 1);

        touchVector = new Vector2();
        droplets = new Array<Droplet>();
        dropSpeed = 2f;

        bucketCollider = new Rectangle();
        dropCollider = new Rectangle();

        scoreFont = new BitmapFont();
        scoreFont.getRegion().getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        scoreFont.getData().setScale(0.1f, 0.1f);
        score = 0;
    }

    @Override
    public void render() {
        draw();
        update();
        takeInput();
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        image.dispose();
        dropTexture.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    private void takeInput() {
        // Has to be in a function that runs per frame because this is getting a delta.
        float delta = Gdx.graphics.getDeltaTime();
        float bucketSpeed = 4f;

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            bucketSprite.translateX(bucketSpeed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            bucketSprite.translateX(-(bucketSpeed * delta));
        }

        if (Gdx.input.isTouched()) {
            touchVector.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchVector);
            bucketSprite.setCenterX(touchVector.x);
        }

    }

    private void update() {
        float delta = Gdx.graphics.getDeltaTime();

        moveBucket();

        dropTimer += delta;
        if (dropTimer > 1f) {
            createDroplet();
            dropTimer = 0f;
        }

        makeItRain(delta);
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        // draw bucket with width / height of 1 meter
        // This is essentially passing a Rect - pygame equivalent
        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        // spriteBatch.draw(bucketTexture, 0, 0, 1, 1);

        scoreFont.draw(spriteBatch, String.format("%d", score), 0f, worldHeight / 2 - scoreFont.getCapHeight());

        // Very similar to how pygame draws on surfs? lol
        bucketSprite.draw(spriteBatch);

        for (Droplet droplet : droplets) {
            droplet.draw(spriteBatch);
        }

        spriteBatch.end();
    }

    public void createDroplet() {
        float dropHeight = 1;
        float dropWidth = 1;
        Sprite dropSprite = new Sprite(dropTexture);
        Vector2 topLeft = new Vector2(MathUtils.random(0f, worldWidth - dropWidth), worldHeight);

        // TODO: Perhaps let a Droplet own these properties instead of making it behave
        // like a factory? Thoughts for scaling.
        Droplet droplet = new Droplet(dropSprite, dropCollider, dropSpeed, dropWidth, dropHeight, topLeft);

        droplets.add(droplet);
    }

    private void makeItRain(float delta) {
        for (int i = droplets.size - 1; i >= 0; i--) {
            Droplet droplet = droplets.get(i);
            droplet.update();

            if (droplet.getY() < -droplet.getHeight()) {
                droplets.removeIndex(i);
                score = MathUtils.clamp(--score, 0, 999);
            } else if (dropCollider.overlaps(bucketCollider)) {
                droplets.removeIndex(i);
                dropSfx.play();
                score = MathUtils.clamp(++score, 0, 999);
            }
        }
    }

    private void moveBucket() {
        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketSprite.getWidth()));
        bucketCollider.set(bucketSprite.getX(), bucketSprite.getY(), bucketSprite.getWidth(), bucketSprite.getHeight());
    }
}

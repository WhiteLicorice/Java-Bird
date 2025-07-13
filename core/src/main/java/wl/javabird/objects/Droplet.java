package wl.javabird.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Droplet {
    public float width;
    public float height;

    private Sprite sprite;
    private Rectangle collider;
    private float speed;

    public Droplet(Sprite sprite, Rectangle collider, float speed, float width, float height, Vector2 topleft) {
        this.sprite = sprite;
        this.collider = collider;
        this.speed = speed;

        this.width = width;
        this.height = height;
        this.sprite.setSize(width, height);
        this.sprite.setX(topleft.x);
        this.sprite.setY(topleft.y);
    }

    public void update() {
        float delta = Gdx.graphics.getDeltaTime();
        sprite.translateY(-speed * delta);
        collider.set(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
    }

    public void draw(SpriteBatch surf) {
        sprite.draw(surf);
    }

    public boolean isColliding(Rectangle other) {
        return other.overlaps(this.collider);
    }

    public float getY() {
        return sprite.getY();
    }

    public float getX() {
        return sprite.getX();
    }

    public float getHeight() {
        return sprite.getHeight();
    }

}

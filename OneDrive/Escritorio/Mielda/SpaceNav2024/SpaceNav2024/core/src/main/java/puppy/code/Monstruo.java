package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Monstruo {
    protected Texture textura;
    protected float x, y;
    protected int vidas;
    protected Rectangle hitbox;

    public Monstruo(Texture textura, float x, float y, int vidas) {
        this.textura = textura;
        this.x = x;
        this.y = y;
        this.vidas = vidas;
        this.hitbox = new Rectangle(x, y, textura.getWidth(), textura.getHeight());
    }

    // Método abstracto para mover al monstruo
    public abstract void mover(float delta);

    // Método abstracto para realizar el ataque (cambiado a realizarAtaque para coincidir con la interfaz)
    public abstract void realizarAtaque(SpriteBatch batch);

    // Método para recibir daño y verificar si ha muerto
    public void recibirDanio(int danio) {
        vidas -= danio;
        if (vidas <= 0) {
            morir();
        }
    }

    // Método para dibujar al monstruo en pantalla
    public void render(SpriteBatch batch) {
        batch.draw(textura, x, y);
    }

    // Método para definir el comportamiento al morir
    protected void morir() {
        System.out.println("El monstruo ha muerto");
    }

    // Getters y setters para coordenadas y vidas
    public int getVidas() {
        return vidas;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
        hitbox.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
        hitbox.y = y;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }
}


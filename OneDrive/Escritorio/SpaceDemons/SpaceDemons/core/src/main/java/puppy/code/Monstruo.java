package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public abstract class Monstruo {
    protected Texture textura;
    protected float x, y;
    protected int vidas;
    protected Rectangle hitbox;
    protected Array<Bullet> disparos = new Array<>(); // Almacena los disparos del monstruo
    protected long ultimoAtaque = 0; // Tiempo del último ataque
    protected static final int INTERVALO_ATAQUE = 3000; // 3 segundos entre ataques
    
    protected Ataque estrategiaAtaque; // Estrategia de ataque del monstruo

    public Monstruo(Texture textura, float x, float y, int vidas, Ataque estrategiaAtaque) {
        this.textura = textura;
        this.x = x;
        this.y = y;
        this.vidas = vidas;
        this.hitbox = new Rectangle(x, y, textura.getWidth(), textura.getHeight());
        this.estrategiaAtaque = estrategiaAtaque;
    }

    // Método abstracto para el movimiento del monstruo (obligatorio para las subclases)
    public abstract void mover(float delta);

    // Método para ejecutar el ataque utilizando la estrategia
    public void realizarAtaque(SpriteBatch batch) {
        if (estrategiaAtaque != null) {
            estrategiaAtaque.realizarAtaque(batch, x + textura.getWidth() / 2, y, disparos); // Delegar a la estrategia
        }
    }

    // Actualización general del estado del monstruo
    public void actualizar(float delta, SpriteBatch batch) {
        mover(delta);             // Movimiento del monstruo
        if (debeAtacar()) {       // Control del tiempo para ataques
            realizarAtaque(batch);
            ultimoAtaque = System.currentTimeMillis(); // Registrar el tiempo del último ataque
        }
        actualizarDisparos(batch); // Manejo de disparos comunes
        render(batch);             // Dibuja al monstruo
    }

    // Lógica para actualizar, dibujar y eliminar disparos
    protected void actualizarDisparos(SpriteBatch batch) {
        for (int i = 0; i < disparos.size; i++) {
            Bullet disparo = disparos.get(i);
            disparo.update(); // Actualizar la posición del disparo
            if (disparo.isDestroyed() || disparo.getY() < 0) {
                disparos.removeIndex(i--); // Remover disparos destruidos o fuera de pantalla
            } else {
                disparo.draw(batch); // Dibujar disparo
            }
        }
    }

    // Verifica si el monstruo puede atacar
    protected boolean debeAtacar() {
        return System.currentTimeMillis() - ultimoAtaque > INTERVALO_ATAQUE;
    }

    // Método para recibir daño y verificar si el monstruo ha muerto
    public void recibirDanio(int danio) {
        vidas -= danio;
        if (vidas <= 0) {
            morir();
        }
    }

    // Método abstracto para definir el comportamiento al morir (puede ser sobreescrito)
    protected void morir() {
        System.out.println("El monstruo ha muerto");
    }

    // Método para dibujar al monstruo
    public void render(SpriteBatch batch) {
        batch.draw(textura, x, y);
    }

    // Getters y setters para estrategia de ataque
    public void setEstrategiaAtaque(Ataque estrategiaAtaque) {
        this.estrategiaAtaque = estrategiaAtaque;
    }

    public Ataque getEstrategiaAtaque() {
        return estrategiaAtaque;
    }

    // Getters y setters para atributos de posición, hitbox y disparos
    public int getVidas() {
        return vidas;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
        hitbox.x = x; // Actualizar hitbox
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
        hitbox.y = y; // Actualizar hitbox
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public Array<Bullet> getDisparos() {
        return disparos;
    }
}

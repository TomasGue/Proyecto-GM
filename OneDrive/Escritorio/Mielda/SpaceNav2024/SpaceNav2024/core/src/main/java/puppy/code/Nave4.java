package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Nave4 {

    private boolean destruida = false;
    private int vidas = 3;
    private float xVel = 0;
    private float yVel = 0;
    private Sprite spr;
    private Sound sonidoHerido;
    private Sound soundBala;
    private Texture txBala;
    private boolean herido = false;
    private int tiempoHeridoMax = 50; // Frames de inmunidad
    private int tiempoHerido;

    public Nave4(int x, int y, Texture tx, Sound soundChoque, Texture txBala, Sound soundBala) {
        sonidoHerido = soundChoque;
        this.soundBala = soundBala;
        this.txBala = txBala;
        spr = new Sprite(tx);
        spr.setPosition(x, y);
        spr.setBounds(x, y, 45, 45);
    }

    public void draw(SpriteBatch batch, PantallaJuego juego) {
        if (destruida) return; // No dibujar la nave si está destruida

        float x = spr.getX();
        float y = spr.getY();

        if (herido) {
            // Efecto de "temblor" cuando está herido
            spr.setX(spr.getX() + MathUtils.random(-2, 2));
            spr.draw(batch);
            spr.setX(x); // Restablece la posición para el próximo frame

            // Reducir el tiempo de inmunidad
            tiempoHerido--;
            if (tiempoHerido <= 0) {
                herido = false; // Termina la inmunidad
            }
        } else {
            // Movimiento continuo cuando no está herido
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                xVel = -3;
            } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                xVel = 3;
            } else {
                xVel = 0;
            }

            if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
                yVel = 3;
            } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
                yVel = -3;
            } else {
                yVel = 0;
            }

            // Mantener la nave dentro de los bordes de la pantalla
            if (x + xVel < 0 || x + xVel + spr.getWidth() > Gdx.graphics.getWidth())
                xVel = 0;
            if (y + yVel < 0 || y + yVel + spr.getHeight() > Gdx.graphics.getHeight())
                yVel = 0;

            // Aplicar la nueva posición
            spr.setPosition(x + xVel, y + yVel);
            spr.draw(batch);
        }

        // Disparo con la barra espaciadora
        if (!destruida && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            Bullet bala = new Bullet(spr.getX() + spr.getWidth() / 2 - 5, spr.getY() + spr.getHeight() - 5, 0, 3, txBala);
            juego.agregarBala(bala);
            soundBala.play();
        }
    }

    public boolean checkCollision(Ball2 b) {
        if (!herido && b.getArea().overlaps(spr.getBoundingRectangle())) {
            // Configuración de inmunidad tras recibir daño
            vidas--;
            herido = true;
            tiempoHerido = tiempoHeridoMax;
            sonidoHerido.play();
            if (vidas <= 0) {
                destruida = true; // Marcar la nave como destruida
                System.out.println("La nave ha sido destruida.");
            }
            return true;
        }
        return false;
    }

    public void restarVida(int cantidad) {
        if (!destruida && !herido) { // Solo aplicar daño si no está destruida ni en inmunidad
            vidas -= cantidad;
            if (vidas > 0) {
                sonidoHerido.play();
                herido = true;
                tiempoHerido = tiempoHeridoMax;
            } else if (vidas <= 0) {
                destruida = true;
                System.out.println("La nave ha sido destruida.");
            }
        }
    }

    public boolean estaDestruido() {
        return destruida;
    }

    public boolean estaHerido() {
        return herido;
    }

    public int getVidas() {
        return vidas;
    }

    public Rectangle getHitbox() {
        return spr.getBoundingRectangle();
    }
    public void setVidas(int vidas2) { // Agregado el método setVidas
        vidas = vidas2;
    }
}




/*package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Nave4 {

    private boolean destruida = false;
    private int vidas = 3;
    private float xVel = 0;
    private float yVel = 0;
    private Sprite spr;
    private Sound sonidoHerido;
    private Sound soundBala;
    private Texture txBala;
    private boolean herido = false;
    private int tiempoHeridoMax = 50; // Frames de inmunidad
    private int tiempoHerido;

    public Nave4(int x, int y, Texture tx, Sound soundChoque, Texture txBala, Sound soundBala) {
        sonidoHerido = soundChoque;
        this.soundBala = soundBala;
        this.txBala = txBala;
        spr = new Sprite(tx);
        spr.setPosition(x, y);
        spr.setBounds(x, y, 45, 45);
    }

    public void draw(SpriteBatch batch, PantallaJuego juego) {
        float x = spr.getX();
        float y = spr.getY();

        // Si la nave está en estado de "herido" (inmunidad activa), cuenta el tiempo de inmunidad
        if (!herido) {
            // Movimiento continuo con las teclas
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                xVel = -3;
            } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                xVel = 3;
            } else {
                xVel = 0;
            }

            if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
                yVel = 3;
            } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
                yVel = -3;
            } else {
                yVel = 0;
            }

            // Mantener la nave dentro de los bordes de la pantalla
            if (x + xVel < 0 || x + xVel + spr.getWidth() > Gdx.graphics.getWidth())
                xVel = 0;
            if (y + yVel < 0 || y + yVel + spr.getHeight() > Gdx.graphics.getHeight())
                yVel = 0;

            // Aplicar la nueva posición
            spr.setPosition(x + xVel, y + yVel);
            spr.draw(batch);

        } else {
            // Efecto de "herido" con un leve movimiento de la nave
            spr.setX(spr.getX() + MathUtils.random(-2, 2));
            spr.draw(batch);
            spr.setX(x);

            // Reducir el tiempo de inmunidad
            tiempoHerido--;
            if (tiempoHerido <= 0) {
                herido = false; // Termina la inmunidad
            }
        }

        // Disparo con la barra espaciadora
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            Bullet bala = new Bullet(spr.getX() + spr.getWidth() / 2 - 5, spr.getY() + spr.getHeight() - 5, 0, 3, txBala);
            juego.agregarBala(bala);
            soundBala.play();
        }
    }

    public boolean checkCollision(Ball2 b) {
        if (!herido && b.getArea().overlaps(spr.getBoundingRectangle())) {
            if (xVel == 0) xVel += b.getXSpeed() / 2;
            if (b.getXSpeed() == 0) b.setXSpeed(b.getXSpeed() + (int) xVel / 2);
            xVel = -xVel;
            b.setXSpeed(-b.getXSpeed());

            if (yVel == 0) yVel += b.getySpeed() / 2;
            if (b.getySpeed() == 0) b.setySpeed(b.getySpeed() + (int) yVel / 2);
            yVel = -yVel;
            b.setySpeed(-b.getySpeed());

            restarVida(1);
            herido = true;
            tiempoHerido = tiempoHeridoMax;
            return true;
        }
        return false;
    }

    public void restarVida(int cantidad) {
        if (!destruida) { // Verificar que la nave no esté ya destruida
            this.vidas -= cantidad;
            if (this.vidas > 0) {
                sonidoHerido.play();
            } else if (this.vidas <= 0) {
                destruida = true;
                System.out.println("La nave ha sido destruida.");
            }
        }
    }

    public boolean estaDestruido() {
        return destruida;
    }

    public boolean estaHerido() {
        return herido;
    }

    public int getVidas() {
        return vidas;
    }

    public Rectangle getHitbox() {
        return spr.getBoundingRectangle();
    }

    public int getX() {
        return (int) spr.getX();
    }

    public int getY() {
        return (int) spr.getY();
    }

    public void setVidas(int vidas2) {
        vidas = vidas2;
    }
}*/

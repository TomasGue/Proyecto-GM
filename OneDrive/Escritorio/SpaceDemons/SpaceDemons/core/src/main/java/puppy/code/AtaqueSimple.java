package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class AtaqueSimple implements Ataque {
    private Texture texturaBala;
    private int danio;
    private int velocidad;

    public AtaqueSimple(Texture texturaBala, int danio, int velocidad) {
        this.texturaBala = texturaBala;
        this.danio = danio;
        this.velocidad = velocidad;
    }

    @Override
    public void realizarAtaque(SpriteBatch batch, float x, float y, Array<Bullet> disparos) {
        Bullet disparo = new Bullet((int) (x + texturaBala.getWidth() / 2), (int) y, 0, velocidad, texturaBala);
        disparos.add(disparo); // Agregar el disparo al array del monstruo
    }


    @Override
    public int getDanio() {
        return danio;
    }
    @Override
    public void dispose() {
        if (texturaBala != null) {
            texturaBala.dispose();
        }
    }

}

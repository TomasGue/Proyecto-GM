package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class AtaqueTriple implements Ataque {
    private Texture texturaBala;
    private int danio;
    private int velocidad;
    
    

    public AtaqueTriple(Texture texturaBala, int danio, int velocidad) {
        this.texturaBala = texturaBala;
        this.danio = danio;
        this.velocidad = velocidad;
    }

    @Override
    public void realizarAtaque(SpriteBatch batch, float x, float y, Array<Bullet> disparos) {
        disparos.add(new Bullet((int) x - 10, (int) y, -5, velocidad, texturaBala));
        disparos.add(new Bullet((int) x, (int) y, 0, velocidad, texturaBala));
        disparos.add(new Bullet((int) x + 10, (int) y, 5, velocidad, texturaBala));
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

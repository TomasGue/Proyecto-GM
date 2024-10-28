package puppy.code;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SpaceNavigation extends Game {
    private String nombreJuego = "Space Navigation";
    private SpriteBatch batch;
    private BitmapFont font;
    private int highScore;
    private PantallaJuego pantallaJuego; // Añadimos una referencia a PantallaJuego

    public void create() {
        highScore = 0;
        batch = new SpriteBatch();
        font = new BitmapFont(); // Usa Arial font por defecto
        font.getData().setScale(2f);

        // Crear y establecer PantallaMenu inicialmente
        Screen ss = new PantallaMenu(this);
        this.setScreen(ss);
    }

    public void render() {
        super.render(); // Importante para renderizado continuo
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
    }

    // Getters
    public SpriteBatch getBatch() {
        return batch;
    }

    public BitmapFont getFont() {
        return font;
    }

    public int getHighScore() {
        return highScore;
    }

    // Setters
    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }

    // Métodos para PantallaJuego
    public PantallaJuego getPantallaJuego() {
        return pantallaJuego;
    }

    public void setPantallaJuego(PantallaJuego pantallaJuego) {
        this.pantallaJuego = pantallaJuego;
    }

    // Método para inicializar PantallaJuego
    public void iniciarPantallaJuego(int ronda, int vidas, int score, int velXAsteroides, int velYAsteroides, int cantAsteroides) {
        pantallaJuego = new PantallaJuego(this, ronda, vidas, score, velXAsteroides, velYAsteroides, cantAsteroides);
        setScreen(pantallaJuego);
    }
}


package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class PantallaMenu implements Screen {

    private final SpaceNavigation game;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;

    // Texturas para el fondo, estrellas, planetas y botones
    private final Texture background;
    private final Texture stars;
    private final Texture bigPlanet;
    private final Texture farPlanets;
    private final Texture ringPlanet;
    private final Texture buttonBackground;

    // Posiciones para el desplazamiento de estrellas y planetas
    private float starsX;
    private float bigPlanetX, bigPlanetY;
    private float farPlanetsX, farPlanetsY;
    private float ringPlanetX, ringPlanetY;

    // Sonidos
    private final Sound hoverSound;
    private final Sound clickSound;
    private final Music backgroundMusic;

    // Bounds para los botones
    private final Rectangle btnJugarBounds;
    private final Rectangle btnSalirBounds;

    private boolean jugarHover = false;
    private boolean salirHover = false;

    // GlyphLayout para medir texto
    private final GlyphLayout layout;

    public PantallaMenu(SpaceNavigation game) {
        this.game = game;

        // Configurar cámara y tamaño de pantalla
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1200, 800);
        batch = game.getBatch();
        layout = new GlyphLayout();

        // Cargar texturas para el fondo, estrellas, planetas y botones
        background = new Texture("parallax-space-backgound.png");
        stars = new Texture("parallax-space-stars.png");
        bigPlanet = new Texture("parallax-space-big-planet.png");
        farPlanets = new Texture("parallax-space-far-planets.png");
        ringPlanet = new Texture("parallax-space-ring-planet.png");
        buttonBackground = new Texture("Parallax60.png"); // Fondo del botón

        // Cargar sonidos
        hoverSound = Gdx.audio.newSound(Gdx.files.internal("falseClick.mp3"));
        clickSound = Gdx.audio.newSound(Gdx.files.internal("clickPresion.mp3"));
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("MyVeryOwnDeadShip.ogg"));

        // Configurar música de fondo
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.5f);
        backgroundMusic.play();

        // Configurar los botones con posiciones relativas al tamaño de la pantalla
        btnJugarBounds = new Rectangle(500, 400, 200, 50);
        btnSalirBounds = new Rectangle(500, 300, 200, 50);

        // Configurar posiciones iniciales de planetas
        starsX = 0;
        bigPlanetX = 1000; bigPlanetY = 300; // Posiciones iniciales
        farPlanetsX = 800; farPlanetsY = 500;
        ringPlanetX = 600; ringPlanetY = 200;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Dibujar fondo, estrellas y planetas en movimiento
        drawBackgroundLayers(delta);

        // Dibujar los botones
        drawButtons();

        // Manejar interacciones de botones
        handleButtonInteractions();
    }

    private void drawBackgroundLayers(float delta) {
        batch.begin();

        // Dibujar fondo estático
        batch.draw(background, 0, 0, 1200, 800);

        // Movimiento lento de las estrellas
        starsX -= 50 * delta;
        batch.draw(stars, starsX, 0, 1200, 800);
        batch.draw(stars, starsX + 1200, 0, 1200, 800); // Segunda copia para bucle
        if (starsX <= -1200) starsX = 0;

        // Movimiento de los planetas con tamaños más grandes
        bigPlanetX -= 30 * delta; // Movimiento lento hacia la izquierda
        farPlanetsX -= 20 * delta;
        ringPlanetX -= 40 * delta;

        // Dibujar planetas con tamaños ajustados
        batch.draw(bigPlanet, bigPlanetX, bigPlanetY, 150, 150); // Tamaño ajustado de bigPlanet
        batch.draw(farPlanets, farPlanetsX, farPlanetsY, 120, 120); // Tamaño ajustado de farPlanets
        batch.draw(ringPlanet, ringPlanetX, ringPlanetY, 130, 130); // Tamaño ajustado de ringPlanet

        // Reiniciar posiciones para que los planetas se muevan en bucle
        if (bigPlanetX <= -150) bigPlanetX = 1200;
        if (farPlanetsX <= -120) farPlanetsX = 1200;
        if (ringPlanetX <= -130) ringPlanetX = 1200;

        batch.end();
    }

    private void drawButtons() {
        batch.begin();

        // Dibujar fondo de los botones con efecto más notorio al hacer hover
        if (jugarHover) {
            batch.setColor(1, 1, 1, 0.8f); // Color blanco con algo de transparencia para el hover
        } else {
            batch.setColor(0.7f, 0.7f, 0.7f, 1); // Color gris oscuro cuando no está en hover
        }
        batch.draw(buttonBackground, btnJugarBounds.x, btnJugarBounds.y, btnJugarBounds.width, btnJugarBounds.height);

        if (salirHover) {
            batch.setColor(1, 1, 1, 0.8f); // Color blanco con algo de transparencia para el hover
        } else {
            batch.setColor(0.7f, 0.7f, 0.7f, 1); // Color gris oscuro cuando no está en hover
        }
        batch.draw(buttonBackground, btnSalirBounds.x, btnSalirBounds.y, btnSalirBounds.width, btnSalirBounds.height);

        batch.setColor(Color.WHITE); // Restablecer el color a blanco

        // Centralizar el texto "JUGAR"
        layout.setText(game.getFont(), "JUGAR");
        float jugarTextX = btnJugarBounds.x + (btnJugarBounds.width - layout.width) / 2;
        float jugarTextY = btnJugarBounds.y + (btnJugarBounds.height + layout.height) / 2;
        game.getFont().draw(batch, "JUGAR", jugarTextX, jugarTextY);

        // Centralizar el texto "SALIR"
        layout.setText(game.getFont(), "SALIR");
        float salirTextX = btnSalirBounds.x + (btnSalirBounds.width - layout.width) / 2;
        float salirTextY = btnSalirBounds.y + (btnSalirBounds.height + layout.height) / 2;
        game.getFont().draw(batch, "SALIR", salirTextX, salirTextY);

        // Dibujar el mensaje de bienvenida
        game.getFont().draw(batch, "Bienvenido a Space Navigation!", 400, 600);

        batch.end();
    }


    private void handleButtonInteractions() {
        int mouseX = Gdx.input.getX() * 1200 / Gdx.graphics.getWidth();
        int mouseY = (Gdx.graphics.getHeight() - Gdx.input.getY()) * 800 / Gdx.graphics.getHeight();

        if (btnJugarBounds.contains(mouseX, mouseY)) {
            if (!jugarHover) {
                hoverSound.play();
                jugarHover = true;
            }
            if (Gdx.input.justTouched()) {
                clickSound.play();
                Timer.schedule(new Task() {
                    @Override
                    public void run() {
                        game.setScreen(new PantallaJuego(game, 1, 3, 0, 1, 1, 3));
                        dispose();
                    }
                }, 0.2f);
            }
        } else {
            jugarHover = false;
        }

        if (btnSalirBounds.contains(mouseX, mouseY)) {
            if (!salirHover) {
                hoverSound.play();
                salirHover = true;
            }
            if (Gdx.input.justTouched()) {
                clickSound.play();
                Timer.schedule(new Task() {
                    @Override
                    public void run() {
                        Gdx.app.exit();
                    }
                }, 0.2f);
            }
        } else {
            salirHover = false;
        }
    }

    @Override
    public void dispose() {
        hoverSound.dispose();
        clickSound.dispose();
        backgroundMusic.dispose();
        background.dispose();
        stars.dispose();
        bigPlanet.dispose();
        farPlanets.dispose();
        ringPlanet.dispose();
        buttonBackground.dispose();
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}



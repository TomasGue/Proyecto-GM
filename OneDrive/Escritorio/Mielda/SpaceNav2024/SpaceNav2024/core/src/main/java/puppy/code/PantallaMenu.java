package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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

    // Fuentes para el título y los botones
    private final BitmapFont titleFont;
    private final BitmapFont buttonFont;

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

        // Cargar las fuentes
        titleFont = new BitmapFont(Gdx.files.internal("fond.fnt"));
        buttonFont = new BitmapFont(Gdx.files.internal("letraBotones.fnt"));

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
        bigPlanetX = 1000; bigPlanetY = 300;
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

        // Dibujar el título "SpaceDemons"
        drawTitle();

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
        batch.draw(stars, starsX + 1200, 0, 1200, 800);
        if (starsX <= -1200) starsX = 0;

        // Movimiento de los planetas
        bigPlanetX -= 30 * delta;
        farPlanetsX -= 20 * delta;
        ringPlanetX -= 40 * delta;

        // Dibujar planetas con tamaños ajustados
        batch.draw(bigPlanet, bigPlanetX, bigPlanetY, 150, 150);
        batch.draw(farPlanets, farPlanetsX, farPlanetsY, 120, 120);
        batch.draw(ringPlanet, ringPlanetX, ringPlanetY, 130, 130);

        if (bigPlanetX <= -150) bigPlanetX = 1200;
        if (farPlanetsX <= -120) farPlanetsX = 1200;
        if (ringPlanetX <= -130) ringPlanetX = 1200;

        batch.end();
    }
    private void drawButtons() {
        batch.begin();

        // Configurar color más claro para hover usando un color blanco brillante
        Color hoverColor = Color.valueOf("#F0F0F0"); // Más brillante que el blanco normal

        // Dibujar botón "JUGAR" con hover y un contorno adicional
        if (jugarHover) {
            batch.setColor(hoverColor);
            // Contorno adicional para el botón "JUGAR"
            batch.draw(buttonBackground, btnJugarBounds.x - 5, btnJugarBounds.y - 5, btnJugarBounds.width + 10, btnJugarBounds.height + 10);
        } else {
            batch.setColor(Color.DARK_GRAY);
        }
        batch.draw(buttonBackground, btnJugarBounds.x, btnJugarBounds.y, btnJugarBounds.width, btnJugarBounds.height);

        // Dibujar botón "SALIR" con hover y un contorno adicional
        if (salirHover) {
            batch.setColor(hoverColor);
            // Contorno adicional para el botón "SALIR"
            batch.draw(buttonBackground, btnSalirBounds.x - 5, btnSalirBounds.y - 5, btnSalirBounds.width + 10, btnSalirBounds.height + 10);
        } else {
            batch.setColor(Color.DARK_GRAY);
        }
        batch.draw(buttonBackground, btnSalirBounds.x, btnSalirBounds.y, btnSalirBounds.width, btnSalirBounds.height);

        batch.setColor(Color.WHITE); // Restaurar color a blanco para el texto

        // Usar la fuente para el texto "JUGAR"
        layout.setText(buttonFont, "JUGAR");
        float jugarTextX = btnJugarBounds.x + (btnJugarBounds.width - layout.width) / 2;
        float jugarTextY = btnJugarBounds.y + (btnJugarBounds.height + layout.height) / 2;
        buttonFont.draw(batch, layout, jugarTextX, jugarTextY);

        // Usar la fuente para el texto "SALIR"
        layout.setText(buttonFont, "SALIR");
        float salirTextX = btnSalirBounds.x + (btnSalirBounds.width - layout.width) / 2;
        float salirTextY = btnSalirBounds.y + (btnSalirBounds.height + layout.height) / 2;
        buttonFont.draw(batch, layout, salirTextX, salirTextY);

        batch.end();
    }

    private void drawTitle() {
        batch.begin();
        titleFont.getData().setScale(2f);

        layout.setText(titleFont, "SpaceDemons");
        float titleX = (1200 - layout.width) / 2;
        float titleY = 700;
        titleFont.draw(batch, layout, titleX, titleY);

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
        titleFont.dispose();
        buttonFont.dispose(); // Liberar la fuente de los botones
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}

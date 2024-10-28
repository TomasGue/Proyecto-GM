package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;

public class PantallaPausa implements Screen {
    private final SpaceNavigation game;
    private final PantallaJuego pantallaJuego;
    private final SpriteBatch batch;
    
    private final Texture background;
    private final BitmapFont buttonFont;
    private final Sound hoverSound;
    private final Sound clickSound;

    private Rectangle btnReanudarBounds;
    private Rectangle btnSalirBounds;
    private boolean reanudarHover = false;
    private boolean salirHover = false;

    private final GlyphLayout layout;

    public PantallaPausa(SpaceNavigation game, PantallaJuego pantallaJuego) {
        this.game = game;
        this.pantallaJuego = pantallaJuego;
        this.batch = game.getBatch();
        
        background = new Texture("fondoPantallaJuego.png");
        buttonFont = new BitmapFont(Gdx.files.internal("letraBotones.fnt"));
        
        hoverSound = Gdx.audio.newSound(Gdx.files.internal("falseClick.mp3"));
        clickSound = Gdx.audio.newSound(Gdx.files.internal("clickPresion.mp3"));
        
        layout = new GlyphLayout();
        setButtonBounds();
    }

    private void setButtonBounds() {
        layout.setText(buttonFont, "REANUDAR");
        float buttonWidth = layout.width + 40;
        float buttonHeight = layout.height + 20;
        btnReanudarBounds = new Rectangle((1200 - buttonWidth) / 2, 400, buttonWidth, buttonHeight);

        layout.setText(buttonFont, "SALIR");
        buttonWidth = layout.width + 40;
        buttonHeight = layout.height + 20;
        btnSalirBounds = new Rectangle((1200 - buttonWidth) / 2, 300, buttonWidth, buttonHeight);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);
        batch.begin();
        
        batch.setColor(0.2f, 0.2f, 0.2f, 1f);
        batch.draw(background, 0, 0, 1200, 800);
        batch.setColor(Color.WHITE);
        
        layout.setText(buttonFont, "Pausa");
        buttonFont.draw(batch, layout, (1200 - layout.width) / 2, 600);

        drawButtons();
        batch.end();

        handleButtonInteractions();
    }

    private void drawButtons() {
        if (reanudarHover) batch.setColor(1, 1, 1, 0.8f);
        else batch.setColor(0.6f, 0.6f, 0.6f, 1);
        batch.draw(background, btnReanudarBounds.x, btnReanudarBounds.y, btnReanudarBounds.width, btnReanudarBounds.height);
        
        if (salirHover) batch.setColor(1, 1, 1, 0.8f);
        else batch.setColor(0.6f, 0.6f, 0.6f, 1);
        batch.draw(background, btnSalirBounds.x, btnSalirBounds.y, btnSalirBounds.width, btnSalirBounds.height);
        
        batch.setColor(Color.WHITE);

        layout.setText(buttonFont, "REANUDAR");
        buttonFont.draw(batch, "REANUDAR", btnReanudarBounds.x + (btnReanudarBounds.width - layout.width) / 2, btnReanudarBounds.y + (btnReanudarBounds.height + layout.height) / 2);
        
        layout.setText(buttonFont, "SALIR");
        buttonFont.draw(batch, "SALIR", btnSalirBounds.x + (btnSalirBounds.width - layout.width) / 2, btnSalirBounds.y + (btnSalirBounds.height + layout.height) / 2);
    }

    private void handleButtonInteractions() {
        int mouseX = Gdx.input.getX() * 1200 / Gdx.graphics.getWidth();
        int mouseY = (Gdx.graphics.getHeight() - Gdx.input.getY()) * 800 / Gdx.graphics.getHeight();

        if (btnReanudarBounds.contains(mouseX, mouseY)) {
            if (!reanudarHover) hoverSound.play();
            reanudarHover = true;
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                clickSound.play();
                game.setScreen(pantallaJuego);
            }
        } else {
            reanudarHover = false;
        }

        if (btnSalirBounds.contains(mouseX, mouseY)) {
            if (!salirHover) hoverSound.play();
            salirHover = true;
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                clickSound.play();
                Gdx.app.exit();
            }
        } else {
            salirHover = false;
        }
    }

    @Override
    public void show() {}
    @Override
    public void resize(int width, int height) {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {
        background.dispose();
        hoverSound.dispose();
        clickSound.dispose();
        buttonFont.dispose();
    }
}



/*package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

public class PantallaPausa implements Screen {

    private final SpaceNavigation game;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    
    private final Texture background;
    private final BitmapFont font;
    private final Texture pauseButtonTexture;
    
    private Rectangle resumeBounds;
    private Rectangle exitBounds;
    private Rectangle pauseButtonBounds;
    
    private final Sound hoverSound;
    private final Sound clickSound;
    
    private boolean resumeHover = false;
    private boolean exitHover = false;
    
    private final GlyphLayout layout;

    public PantallaPausa(SpaceNavigation game) {
        this.game = game;
        
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1200, 800);
        batch = game.getBatch();
        
        background = new Texture("fondoPantallaJuego.png");
        font = new BitmapFont(Gdx.files.internal("letraBotones.fnt"));
        pauseButtonTexture = new Texture("pausa.png");
        
        hoverSound = Gdx.audio.newSound(Gdx.files.internal("falseClick.mp3"));
        clickSound = Gdx.audio.newSound(Gdx.files.internal("clickPresion.mp3"));
        
        layout = new GlyphLayout();
        setButtonBounds();
    }
    
    private void setButtonBounds() {
        layout.setText(font, "REANUDAR");
        float buttonWidth = layout.width + 40;
        float buttonHeight = layout.height + 20;
        resumeBounds = new Rectangle((1200 - buttonWidth) / 2, 450, buttonWidth, buttonHeight);

        layout.setText(font, "SALIR");
        buttonWidth = layout.width + 40;
        buttonHeight = layout.height + 20;
        exitBounds = new Rectangle((1200 - buttonWidth) / 2, 350, buttonWidth, buttonHeight);
        
        pauseButtonBounds = new Rectangle(10, 740, 50, 50); // Botón de pausa en la esquina superior izquierda
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        
        batch.draw(background, 0, 0, 1200, 800);
        font.draw(batch, "Pausa", (1200 - layout.width) / 2, 700);
        
        // Dibujar botón de pausa en la esquina superior izquierda
        batch.draw(pauseButtonTexture, pauseButtonBounds.x, pauseButtonBounds.y, pauseButtonBounds.width, pauseButtonBounds.height);

        drawButtons();
        
        batch.end();

        handleButtonInteractions();
    }

    private void drawButtons() {
        // Botón "Reanudar"
        if (resumeHover) {
            batch.setColor(1, 1, 1, 0.8f);
        } else {
            batch.setColor(0.6f, 0.6f, 0.6f, 1);
        }
        batch.draw(background, resumeBounds.x, resumeBounds.y, resumeBounds.width, resumeBounds.height);
        layout.setText(font, "REANUDAR");
        font.draw(batch, "REANUDAR", resumeBounds.x + (resumeBounds.width - layout.width) / 2, resumeBounds.y + (resumeBounds.height + layout.height) / 2);

        // Botón "Salir"
        if (exitHover) {
            batch.setColor(1, 1, 1, 0.8f);
        } else {
            batch.setColor(0.6f, 0.6f, 0.6f, 1);
        }
        batch.draw(background, exitBounds.x, exitBounds.y, exitBounds.width, exitBounds.height);
        layout.setText(font, "SALIR");
        font.draw(batch, "SALIR", exitBounds.x + (exitBounds.width - layout.width) / 2, exitBounds.y + (exitBounds.height + layout.height) / 2);

        batch.setColor(Color.WHITE);
    }

    private void handleButtonInteractions() {
        int mouseX = Gdx.input.getX() * 1200 / Gdx.graphics.getWidth();
        int mouseY = (Gdx.graphics.getHeight() - Gdx.input.getY()) * 800 / Gdx.graphics.getHeight();

        // Botón "Reanudar"
        if (resumeBounds.contains(mouseX, mouseY)) {
            if (!resumeHover) {
                hoverSound.play();
                resumeHover = true;
            }
            if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                clickSound.play();
                game.setScreen(game.getPantallaJuego());
            }
        } else {
            resumeHover = false;
        }

        // Botón "Salir"
        if (exitBounds.contains(mouseX, mouseY)) {
            if (!exitHover) {
                hoverSound.play();
                exitHover = true;
            }
            if (Gdx.input.justTouched()) {
                clickSound.play();
                Gdx.app.exit();
            }
        } else {
            exitHover = false;
        }
    }

    @Override
    public void show() {}

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        background.dispose();
        pauseButtonTexture.dispose();
        hoverSound.dispose();
        clickSound.dispose();
        font.dispose();
    }
}*/

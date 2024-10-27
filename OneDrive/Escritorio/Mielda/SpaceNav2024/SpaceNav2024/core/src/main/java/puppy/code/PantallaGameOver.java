package puppy.code;

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

public class PantallaGameOver implements Screen {

    private final SpaceNavigation game;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    
    // Textura y fondo
    private final Texture background;
    
    // Fuentes
    private final BitmapFont buttonFont;

    // Sonidos
    private final Sound hoverSound;
    private final Sound clickSound;
    
    // Rectángulos para los botones
    private Rectangle btnVolverBounds;
    private Rectangle btnSalirBounds;

    // Hover de los botones
    private boolean volverHover = false;
    private boolean salirHover = false;
    
    private final GlyphLayout layout;

    public PantallaGameOver(SpaceNavigation game) {
        this.game = game;
        
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1200, 800);
        batch = game.getBatch();
        
        // Cargar fondo y hacerlo más oscuro
        background = new Texture("fondoPantallaJuego.png");
        
        // Cargar la fuente para los botones
        buttonFont = new BitmapFont(Gdx.files.internal("letraBotones.fnt"));
        
        // Cargar sonidos
        hoverSound = Gdx.audio.newSound(Gdx.files.internal("falseClick.mp3"));
        clickSound = Gdx.audio.newSound(Gdx.files.internal("clickPresion.mp3"));
        
        layout = new GlyphLayout();
        
        // Configurar botones dinámicamente según el tamaño del texto
        setButtonBounds();
    }
    
    private void setButtonBounds() {
        // Configurar el tamaño del botón "Volver a jugar"
        layout.setText(buttonFont, "VOLVER A JUGAR");
        float buttonWidth = layout.width + 40; // Añadir un poco de espacio alrededor del texto
        float buttonHeight = layout.height + 20;
        btnVolverBounds = new Rectangle((1200 - buttonWidth) / 2, 400, buttonWidth, buttonHeight);

        // Configurar el tamaño del botón "Salir del juego"
        layout.setText(buttonFont, "SALIR DEL JUEGO");
        buttonWidth = layout.width + 40;
        buttonHeight = layout.height + 20;
        btnSalirBounds = new Rectangle((1200 - buttonWidth) / 2, 300, buttonWidth, buttonHeight);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        
        // Dibujar fondo con color oscuro
        batch.setColor(0.2f, 0.2f, 0.2f, 1f); // Fondo más oscuro
        batch.draw(background, 0, 0, 1200, 800);
        batch.setColor(Color.WHITE); // Restaurar color
        
        // Dibujar título "Game Over"
        layout.setText(buttonFont, "Game Over !!!");
        buttonFont.draw(batch, layout, (1200 - layout.width) / 2, 600);

        // Dibujar los botones
        drawButtons();
        
        batch.end();

        // Manejar interacciones de botones
        handleButtonInteractions();
    }

    private void drawButtons() {
        // Dibujar el botón "Volver a jugar" con hover
        if (volverHover) {
            batch.setColor(1, 1, 1, 0.8f); // Color claro en hover
        } else {
            batch.setColor(0.6f, 0.6f, 0.6f, 1); // Color gris normal
        }
        batch.draw(background, btnVolverBounds.x, btnVolverBounds.y, btnVolverBounds.width, btnVolverBounds.height);

        // Dibujar el botón "Salir del juego" con hover
        if (salirHover) {
            batch.setColor(1, 1, 1, 0.8f); // Color claro en hover
        } else {
            batch.setColor(0.6f, 0.6f, 0.6f, 1); // Color gris normal
        }
        batch.draw(background, btnSalirBounds.x, btnSalirBounds.y, btnSalirBounds.width, btnSalirBounds.height);

        batch.setColor(Color.WHITE); // Restaurar el color a blanco

        // Centralizar y dibujar el texto "VOLVER A JUGAR"
        layout.setText(buttonFont, "VOLVER A JUGAR");
        float volverTextX = btnVolverBounds.x + (btnVolverBounds.width - layout.width) / 2;
        float volverTextY = btnVolverBounds.y + (btnVolverBounds.height + layout.height) / 2;
        buttonFont.draw(batch, "VOLVER A JUGAR", volverTextX, volverTextY);

        // Centralizar y dibujar el texto "SALIR DEL JUEGO"
        layout.setText(buttonFont, "SALIR DEL JUEGO");
        float salirTextX = btnSalirBounds.x + (btnSalirBounds.width - layout.width) / 2;
        float salirTextY = btnSalirBounds.y + (btnSalirBounds.height + layout.height) / 2;
        buttonFont.draw(batch, "SALIR DEL JUEGO", salirTextX, salirTextY);
    }

    private void handleButtonInteractions() {
        int mouseX = Gdx.input.getX() * 1200 / Gdx.graphics.getWidth();
        int mouseY = (Gdx.graphics.getHeight() - Gdx.input.getY()) * 800 / Gdx.graphics.getHeight();

        // Botón "Volver a jugar"
        if (btnVolverBounds.contains(mouseX, mouseY)) {
            if (!volverHover) {
                hoverSound.play();
                volverHover = true;
            }
            if (Gdx.input.justTouched()) {
                clickSound.play();
                game.setScreen(new PantallaJuego(game, 1, 3, 0, 1, 1, 10));
                dispose();
            }
        } else {
            volverHover = false;
        }

        // Botón "Salir del juego"
        if (btnSalirBounds.contains(mouseX, mouseY)) {
            if (!salirHover) {
                hoverSound.play();
                salirHover = true;
            }
            if (Gdx.input.justTouched()) {
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
//
/*
package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;


public class PantallaGameOver implements Screen {

	private SpaceNavigation game;
	private OrthographicCamera camera;

	public PantallaGameOver(SpaceNavigation game) {
		this.game = game;
        
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1200, 800);
	}

	@Override
	public void render(float delta) {
		ScreenUtils.clear(0, 0, 0.2f, 1);

		camera.update();
		game.getBatch().setProjectionMatrix(camera.combined);

		game.getBatch().begin();
		game.getFont().draw(game.getBatch(), "Game Over !!! ", 120, 400,400,1,true);
		game.getFont().draw(game.getBatch(), "Pincha en cualquier lado para reiniciar ...", 100, 300);
	
		game.getBatch().end();

		if (Gdx.input.isTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
			Screen ss = new PantallaJuego(game,1,3,0,1,1,10);
			ss.resize(1200, 800);
			game.setScreen(ss);
			dispose();
		}
	}
 
	
	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}*/
   

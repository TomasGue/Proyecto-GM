package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Ball2 {
    private int x;
    private int y;
    private int xSpeed;
    private int ySpeed;
    private Sprite spr;
    private int radius;

    public Ball2(int x, int y, int radius, int xSpeed, int ySpeed, Texture tx) {
        spr = new Sprite(tx);
        this.radius = radius;

        // Asignación directa de las velocidades
        this.xSpeed = xSpeed != 0 ? xSpeed : 1; // Asegurar que la velocidad no sea 0
        this.ySpeed = ySpeed != 0 ? ySpeed : 1; // Asegurar que la velocidad no sea 0

        this.x = x;
        this.y = y;

        // Validar que los bordes de la esfera no queden fuera de los límites de la pantalla
        if (x - radius < 0) this.x = x + radius;
        if (x + radius > Gdx.graphics.getWidth()) this.x = x - radius;

        if (y - radius < 0) this.y = y + radius;
        if (y + radius > Gdx.graphics.getHeight()) this.y = y - radius;

        spr.setPosition(this.x, this.y);
    }
    
    public void update() {
        x += xSpeed;
        y += ySpeed;

        // Rebote en los bordes de la pantalla
        if (x - radius < 0) {
            xSpeed *= -1;
            x = radius; // Corregir la posición para que no se salga
        }
        if (x + radius > Gdx.graphics.getWidth()) {
            xSpeed *= -1;
            x = Gdx.graphics.getWidth() - radius; // Corregir la posición para que no se salga
        }
        if (y - radius < 0) {
            ySpeed *= -1;
            y = radius; // Corregir la posición para que no se salga
        }
        if (y + radius > Gdx.graphics.getHeight()) {
            ySpeed *= -1;
            y = Gdx.graphics.getHeight() - radius; // Corregir la posición para que no se salga
        }

        spr.setPosition(x - radius, y - radius);
    }

    public Rectangle getArea() {
        return spr.getBoundingRectangle();
    }

    public void draw(SpriteBatch batch) {
        spr.draw(batch);
    }
    
    public int getRadius() {
        return this.radius;
    }

    public void checkCollision(Ball2 b2) {
        if (spr.getBoundingRectangle().overlaps(b2.spr.getBoundingRectangle())) {
            // Rebotar en caso de colisión
            if (xSpeed == 0) xSpeed += b2.xSpeed / 2;
            if (b2.xSpeed == 0) b2.xSpeed += xSpeed / 2;
            xSpeed *= -1;
            b2.xSpeed *= -1;

            if (ySpeed == 0) ySpeed += b2.ySpeed / 2;
            if (b2.ySpeed == 0) b2.ySpeed += ySpeed / 2;
            ySpeed *= -1;
            b2.ySpeed *= -1;

            // Separar las esferas para evitar que se queden pegadas
            float overlap = 0.5f * (radius + b2.radius);
            this.x += overlap * Math.signum(this.xSpeed);
            this.y += overlap * Math.signum(this.ySpeed);

            b2.x += overlap * Math.signum(b2.xSpeed);
            b2.y += overlap * Math.signum(b2.ySpeed);
        }
    }

    // Getters para X y Y (¡nuevos!)
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // Getters and Setters
    public int getXSpeed() {
        return xSpeed;
    }

    public void setXSpeed(int xSpeed) {
        this.xSpeed = xSpeed;
    }

    public int getySpeed() {
        return ySpeed;
    }

    public void setySpeed(int ySpeed) {
        this.ySpeed = ySpeed;
    }
}






/*package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;


public class Ball2 {
	private int x;
    private int y;
    private int xSpeed;
    private int ySpeed;
    private Sprite spr;

    public Ball2(int x, int y, int size, int xSpeed, int ySpeed, Texture tx) {
    	spr = new Sprite(tx);
    	this.x = x; 
 	
        //validar que borde de esfera no quede fuera
    	if (x-size < 0) this.x = x+size;
    	if (x+size > Gdx.graphics.getWidth())this.x = x-size;
         
        this.y = y;
        //validar que borde de esfera no quede fuera
    	if (y-size < 0) this.y = y+size;
    	if (y+size > Gdx.graphics.getHeight())this.y = y-size;
    	
        spr.setPosition(x, y);
        this.setXSpeed(xSpeed);
        this.setySpeed(ySpeed);
    }
    public void update() {
        x += getXSpeed();
        y += getySpeed();

        if (x+getXSpeed() < 0 || x+getXSpeed()+spr.getWidth() > Gdx.graphics.getWidth())
        	setXSpeed(getXSpeed() * -1);
        if (y+getySpeed() < 0 || y+getySpeed()+spr.getHeight() > Gdx.graphics.getHeight())
        	setySpeed(getySpeed() * -1);
        spr.setPosition(x, y);
    }
    
    public Rectangle getArea() {
    	return spr.getBoundingRectangle();
    }
    public void draw(SpriteBatch batch) {
    	spr.draw(batch);
    }
    
    public void checkCollision(Ball2 b2) {
        if(spr.getBoundingRectangle().overlaps(b2.spr.getBoundingRectangle())){
        	// rebote
            if (getXSpeed() ==0) setXSpeed(getXSpeed() + b2.getXSpeed()/2);
            if (b2.getXSpeed() ==0) b2.setXSpeed(b2.getXSpeed() + getXSpeed()/2);
        	setXSpeed(- getXSpeed());
            b2.setXSpeed(-b2.getXSpeed());
            
            if (getySpeed() ==0) setySpeed(getySpeed() + b2.getySpeed()/2);
            if (b2.getySpeed() ==0) b2.setySpeed(b2.getySpeed() + getySpeed()/2);
            setySpeed(- getySpeed());
            b2.setySpeed(- b2.getySpeed()); 
        }
    }
	public int getXSpeed() {
		return xSpeed;
	}
	public void setXSpeed(int xSpeed) {
		this.xSpeed = xSpeed;
	}
	public int getySpeed() {
		return ySpeed;
	}
	public void setySpeed(int ySpeed) {
		this.ySpeed = ySpeed;
	}
	
    
}*/

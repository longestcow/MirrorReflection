package com.mirror.reflection;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;


public class MirrorReflection extends ApplicationAdapter {
	
	private OrthographicCamera camera;
	private SpriteBatch batch;
	public ShapeRenderer sr;
	Rectangle bounds = new Rectangle();
	
	Pixmap mirCur, lightCur;
	
	public boolean mir = true;
	public Vector3 mousePoint3D; public Vector2 mousePoint = new Vector2(), mStartPoint = new Vector2(), lStartPoint = new Vector2();
	public boolean mPressed,lPressed;
	
	HashMap<Vector2, Vector2> lines = new HashMap<>();
	
	Array<Light> lights = new Array<>();
	
	Color mirColor = new Color(131f/255f,131f/255f,131f/255f,1), lightColor = new Color(229f/255f,229/255f,229/255f,1);
	double angle = 45.0f, rad; Vector2 direction = new Vector2();
	float lightSpeed = 20;
	


	@Override
	public void create () {
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 800);
		bounds.set(0, 0, 800, 800);
		batch = new SpriteBatch();
		sr = new ShapeRenderer();
        sr.setProjectionMatrix(camera.combined);

		mirCur = new Pixmap(Gdx.files.internal("mirrorCursor.png"));
		lightCur = new Pixmap(Gdx.files.internal("lightCursor.png"));

		Gdx.graphics.setCursor(Gdx.graphics.newCursor(mirCur, 16/2, 16/2));
		
		
	}

	@Override
	public void render () {
		if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			if(mir)
				Gdx.graphics.setCursor(Gdx.graphics.newCursor(lightCur, 4/2, 4/2));
			else
				Gdx.graphics.setCursor(Gdx.graphics.newCursor(mirCur, 16/2, 16/2));
			mir=!mir;
		}

		
		ScreenUtils.clear(58f/255f,58f/255f,58f/255f,1);
		
		camera.update();
		//set Mouse position
	    mousePoint.x=Gdx.input.getX(); mousePoint.y=camera.viewportHeight - Gdx.input.getY();
	    
	    if(mir) {
		    if(mPressed) {
		    	batch.begin();
		    	drawLine(mStartPoint, mousePoint, 16, mirColor);
		    	batch.end();
		    }
			
			if(Gdx.input.isButtonPressed(Input.Buttons.LEFT) && !mPressed) {
				Vector2 touchPos = new Vector2(Gdx.input.getX(), camera.viewportHeight - Gdx.input.getY());
				mStartPoint=touchPos;
				mPressed=true;
				
			}
			else if(!Gdx.input.isButtonPressed(Input.Buttons.LEFT) && mPressed) {
				Vector2 touchPos = new Vector2(Gdx.input.getX(), camera.viewportHeight - Gdx.input.getY());
				lines.put(mStartPoint, touchPos);
				mPressed=false;
			}
	    }
	    else { 
	    	
			if(!Gdx.input.isButtonPressed(Input.Buttons.LEFT) && lPressed) {
				Vector2 touchPos = new Vector2(Gdx.input.getX(), camera.viewportHeight - Gdx.input.getY());
				direction = touchPos.sub(lStartPoint);
				if(direction.isZero()) {
					rad=Math.toRadians(ThreadLocalRandom.current().nextInt(0, 361));
					direction=new Vector2((float)Math.cos(rad), (float)Math.sin(rad));
				}
				
				lights.add(new Light(new Vector2(Gdx.input.getX(), camera.viewportHeight - Gdx.input.getY()),direction.nor().scl(lightSpeed)));
				lPressed=false;
			}
			else if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !lPressed) {
				Vector2 touchPos = new Vector2(Gdx.input.getX(), camera.viewportHeight - Gdx.input.getY());
				lStartPoint = touchPos;
				lPressed=true;
			}

	    }
	    
	    //lights stuff
	    for(Light l : lights) {
	    	//check collision
	    	if(l.equals(lights.get(0))) {
	    		System.out.println("aaaa");
	    	}
	    	//move light
	    	l.setPos(l.getPos().add(l.getDir()));
	    	
	    	//render
	    	drawCircle(l.getPos().x, l.getPos().y, 5, lightColor);
	    	
	    	//remove if out of bounds
            if (!bounds.contains(l.getPos()))
                lights.removeValue(l, false);
	    }
	    
		
		
		for(Vector2 key : lines.keySet()) {
	    	drawLine(key, lines.get(key), 16, mirColor);
		}
		
		
	}
	


	@Override
	public void dispose () {
      batch.dispose();
      mirCur.dispose();
      lightCur.dispose();
	}
	
	
    public void drawLine(Vector2 start, Vector2 end, int lineWidth, Color color) {
        Gdx.gl.glLineWidth(lineWidth);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(color);
        sr.line(start, end);
        sr.end();
        Gdx.gl.glLineWidth(1);
        
    }
    
    public void drawCircle(float x, float y, float radius, Color color) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(color);
    	sr.circle(x, y, radius);	
        sr.end();
    }


}


class Light {
	private Vector2 pos, dir;
	public Light(Vector2 pos, Vector2 dir) {
		this.pos=pos;
		this.dir=dir;
	}
	
	public void setPos(Vector2 pos) {
		this.pos=pos;
	}
	public void setDir(Vector2 dir) {
		this.dir=dir;
	}
	public Vector2 getPos() {
		return pos;
	}
	public Vector2 getDir() {
		return dir;
	}
	
}




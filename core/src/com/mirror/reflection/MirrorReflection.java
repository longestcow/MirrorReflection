package com.mirror.reflection;

import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Intersector;
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
	
	
	Array<Light> lights = new Array<>();
	Array<Mirror> mirrors = new Array<>();


	Color mirColor = new Color(131f/255f,131f/255f,131f/255f,1), lightColor = new Color(229f/255f,229/255f,229/255f,1);
	double angle = 45.0f, rad; Vector2 direction = new Vector2();
	float lightSpeed = 20;
	Vector2 lightNext = new Vector2(), collision = new Vector2();
	
	@Override
	public void create () {
		mirrors.add(new Mirror(new Vector2(0,0), new Vector2(800,0)), new Mirror(new Vector2(0,1), new Vector2(0,800)), new Mirror(new Vector2(0,800), new Vector2(800,800)), new Mirror(new Vector2(800,800), new Vector2(800,0)));


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
		    	drawLine(new Mirror(mStartPoint, mousePoint), 16, mirColor);
		    	batch.end();
		    }
			
			if(Gdx.input.isButtonPressed(Input.Buttons.LEFT) && !mPressed) {
				Vector2 touchPos = new Vector2(Gdx.input.getX(), camera.viewportHeight - Gdx.input.getY());
				mStartPoint=touchPos;
				mPressed=true;
				
			}
			else if(!Gdx.input.isButtonPressed(Input.Buttons.LEFT) && mPressed) {
				Vector2 touchPos = new Vector2(Gdx.input.getX(), camera.viewportHeight - Gdx.input.getY());
				mirrors.add(new Mirror(mStartPoint, touchPos));
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
				
				lights.add(new Light(lStartPoint,direction.nor().scl(lightSpeed)));
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
	    	for(Mirror mirror : mirrors) {
	    		if(checkCollision(mirror, l)) 
	    			break;
	    		
	    	}
	    	//move light
	    	l.setPos(l.getPos().add(l.getDir()));
	    	
	    	//render
	    	drawCircle(l, 5, lightColor);
	    	
	    	//remove if out of bounds
            if (!bounds.contains(l.getPos()))
                lights.removeValue(l, false);
	    }
	    
		
		
		for(Mirror key : mirrors) {
	    	drawLine(key, 16, mirColor);
		}
		
		
	}
	



	private boolean checkCollision(Mirror m, Light l) {
		lightNext.set(l.getPos().x + l.getDir().x, l.getPos().y + l.getDir().y);
		if(Intersector.intersectSegments(m.getStart(), m.getEnd(), l.getPos(), lightNext, collision)) {
			
			System.out.println(collision);
            return true;
		}
			
		return false;
	}

	@Override
	public void dispose () {
      batch.dispose();
      mirCur.dispose();
      lightCur.dispose();
	}
	
	
    public void drawLine(Mirror mirror, int lineWidth, Color color) {
        Gdx.gl.glLineWidth(lineWidth);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(color);
        sr.line(mirror.getStart(), mirror.getEnd());
        sr.end();
        Gdx.gl.glLineWidth(1);
        
    }
    
    public void drawCircle(Light l, float radius, Color color) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(color);
    	sr.circle(l.getPos().x, l.getPos().y, radius);	
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

class Mirror {
	private Vector2 start, end;
	public Mirror(Vector2 start, Vector2 end) {
		this.start=start;
		this.end=end;
	}
	
	public void setStart(Vector2 start) {
		this.start=start;
	}
	public void setEnd(Vector2 end) {
		this.end=end;
	}
	public Vector2 getStart() {
		return start;
	}
	public Vector2 getEnd() {
		return end;
	}
	
}








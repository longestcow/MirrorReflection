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
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;


public class MirrorReflection extends ApplicationAdapter {
	
	//changeable stuff:
	float lightSpeed = 20;
	int mirrorWidth = 16, lightRadius = 5;
	
	
	private OrthographicCamera camera;
	private SpriteBatch batch;
	public ShapeRenderer sr;
	Rectangle bounds = new Rectangle();
	Pixmap mirCur, lightCur;
	Color mirColor = new Color(131f/255f,131f/255f,131f/255f,1), lightColor = new Color(229f/255f,229/255f,229/255f,1);

	public Vector2 mousePoint = new Vector2(), mStartPoint = new Vector2(), lStartPoint = new Vector2();
	public boolean mPressed,lPressed,mir = true;
	
	
	Array<Light> lights = new Array<>();
	Array<Mirror> mirrors = new Array<>();

 
	double rad;
	
	//empty vectors
	Vector2 direction = new Vector2(), lightNext = new Vector2(), tempDir1 = new Vector2(), tempDir2 = new Vector2(), normal = new Vector2();
	
	@Override
	public void create () {
		//set boundary mirrors
		mirrors.add(new Mirror(new Vector2(-10,0), new Vector2(810,0)), new Mirror(new Vector2(0,1), new Vector2(0,810)), new Mirror(new Vector2(0,800), new Vector2(800,800)), new Mirror(new Vector2(800,800), new Vector2(800,-10)));
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 800);
		bounds.set(0, 0, 800, 800);
		batch = new SpriteBatch();
		sr = new ShapeRenderer();
        sr.setProjectionMatrix(camera.combined);

		mirCur = new Pixmap(Gdx.files.internal("mirrorCursor.png"));
		lightCur = new Pixmap(Gdx.files.internal("lightCursor.png"));

		Gdx.graphics.setCursor(Gdx.graphics.newCursor(mirCur, mirrorWidth/2, mirrorWidth/2));
		
		
	}

	@Override
	public void render () {
		if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			if(mir)
				Gdx.graphics.setCursor(Gdx.graphics.newCursor(lightCur, 4/2, 4/2));
			else
				Gdx.graphics.setCursor(Gdx.graphics.newCursor(mirCur, mirrorWidth/2, mirrorWidth/2));
			mir=!mir;
		}

		ScreenUtils.clear(58f/255f,58f/255f,58f/255f,1);
		
		camera.update();
		
		//set Mouse position
	    mousePoint.x=Gdx.input.getX(); mousePoint.y=camera.viewportHeight - Gdx.input.getY();
	    
	    if(mir) {
		    if(mPressed) {
		    	batch.begin();
		    	drawLine(new Mirror(mStartPoint, mousePoint), mirrorWidth, mirColor);
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
			
			
			if(Gdx.input.isKeyJustPressed(Input.Keys.C)) {
				if(mirrors.size==4) 
					mirrors.clear();
				
				else {
					mirrors.clear();
					mirrors.add(new Mirror(new Vector2(0,0), new Vector2(800,0)), new Mirror(new Vector2(0,1), new Vector2(0,800)), new Mirror(new Vector2(0,800), new Vector2(800,800)), new Mirror(new Vector2(800,800), new Vector2(800,0)));
				}	
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
			
			if(Gdx.input.isKeyJustPressed(Input.Keys.C)) 
				lights.clear();

			

	    }
	    
	    for(Light l : lights) {
	    	
	    	//remove if out of bounds
            if (!bounds.contains(l.getPos())) {
                lights.removeValue(l, false);
                break;
            }
	    	
	    	//collision with mirror
	    	for(Mirror m : mirrors) {
	    		lightNext.set(l.getPos().x + l.getDir().x, l.getPos().y + l.getDir().y);
	    		//https://math.stackexchange.com/questions/13261/how-to-get-a-reflection-vector formula for reflection
	    		if(Intersector.intersectSegments(m.getStart(), m.getEnd(), l.getPos(), lightNext, null)) {
	    			//https://stackoverflow.com/a/1243676 normal formula (i just used the first normal because it works so i guess i dont need the second normal)
	    			normal.set(-(m.getEnd().y - m.getStart().y), (m.getEnd().x - m.getStart().x)).nor();
	    			tempDir1 = l.getDir(); tempDir2 = l.getDir();
	    			l.setDir(tempDir1.sub(normal.scl(2 * tempDir2.dot(normal))));
	    			break;
	    		}
	    	}
	    	
	    	//move 
	    	l.setPos(l.getPos().add(l.getDir()));
	    	
	    	//render
	    	drawCircle(l, lightRadius, lightColor);
	    	
	    }
	    
		
		
		for(Mirror key : mirrors) {
	    	drawLine(key, mirrorWidth, mirColor);
		}
		
		
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








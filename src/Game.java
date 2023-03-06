
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import java.awt.*;


import game2D.*;

// Game demonstrates how we can override the GameCore class
// to create our own 'game'. We usually need to implement at
// least 'draw' and 'update' (not including any local event handling)
// to begin the process. You should also add code to the 'init'
// method that will initialise event handlers etc.

// Student ID: ???????


@SuppressWarnings("serial")


public class Game extends GameCore
{
    // Useful game constants
    static int screenWidth = 512;
    static int screenHeight = 400;

    // Game constants
    float 	lift = 0.005f;
    float	gravity = 0.001f;
    float	fly = -0.1f;
    float	moveSpeed = 0.1f;

    // Game state flags
    boolean jump = false;
    boolean moveRight = false;
    boolean moveLeft = false;
    boolean attack = false;
    boolean debug = true;

    long lastAttack = 10000;

    // Game resources
    Animation standing, runningRight, runningLeft, jumping, death, attacking;

    Sprite	player = null;
    ArrayList<Sprite> clouds = new ArrayList<Sprite>();

    TileMap tmap = new TileMap();	// Our tile map, note that we load it in init()

    long total;         			// The score will be the total time elapsed since a crash

    int attackAnimationDuration = 1000; // 1 second in milliseconds
    long attackStartTime = 0;


    /**
     * The obligatory main method that creates
     * an instance of our class and starts it running
     *
     * @param args	The list of parameters this program might use (ignored)
     */
    public static void main(String[] args) {

        Game gct = new Game();
        gct.init();
        // Start in windowed mode with the given screen height and width
        gct.run(false,screenWidth,screenHeight);
    }

    /**
     * Initialise the class, e.g. set up variables, load images,
     * create animations, register event handlers.
     *
     * This shows you the general principles but you should create specific
     * methods for setting up your game that can be called again when you wish to
     * restart the game (for example you may only want to load animations once
     * but you could reset the positions of sprites each time you restart the game).
     */
    public void init()
    {
        Sprite s;	// Temporary reference to a sprite

        // Load the tile map and print it out so we can check it is valid
        tmap.loadMap("maps", "map.txt");

        setSize(tmap.getPixelWidth()/4, tmap.getPixelHeight());
        setVisible(true);

        // Create a set of background sprites that we can
        // rearrange to give the illusion of motion

        standing = new Animation();
        standing.loadAnimationFromSheet("images/Biker_idle.png", 4, 1, 60);

        attacking = new Animation();
        attacking.loadAnimationFromSheet("images/Biker_attack1.png", 6, 1, 60);

        runningRight = new Animation();
        runningRight.loadAnimationFromSheet("images/Biker_run_right.png", 6, 1, 60);

        runningLeft = new Animation();
        runningLeft.loadAnimationFromSheet("images/Biker_run_left.png", 6, 1, 60);

        jumping = new Animation();
        jumping.loadAnimationFromSheet("images/Biker_jump.png", 4, 1, 60);

        death = new Animation();
        death.loadAnimationFromSheet("images/Biker_death.png", 6, 1, 60);

        // Initialise the player with an animation
        player = new Sprite(standing);

        // Load a single cloud animation
        Animation ca = new Animation();
        ca.addFrame(loadImage("images/cloud.png"), 1000);

        // Create 3 clouds at random positions off the screen
        // to the right
        for (int c=0; c<3; c++)
        {
            s = new Sprite(ca);
            s.setX(screenWidth + (int)(Math.random()*200.0f));
            s.setY(30 + (int)(Math.random()*150.0f));
            s.setVelocityX(-0.02f);
            s.show();
            clouds.add(s);
        }

        initialiseGame();

        System.out.println(tmap);
    }

    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it when restarting
     * the game when the player loses.
     */
    public void initialiseGame()
    {
        total = 0;

        player.setPosition(200,200);
        player.setVelocity(0,0);
        player.show();
    }

    /**
     * Draw the current state of the game. Note the sample use of
     * debugging output that is drawn directly to the game screen.
     */
    public void draw(Graphics2D g)
    {

        // Be careful about the order in which you draw objects - you
        // should draw the background first, then work your way 'forward'

        // First work out how much we need to shift the view in order to
        // see where the player is. To do this, we adjust the offset so that
        // it is relative to the player's position along with a shift
        int xo = -(int)player.getX() + 200;
        int yo = 0; //-(int)player.getY() + 200;

        //g.setColor(Color.white);
        //g.fillRect(0, 0, getWidth(), getHeight());

        //draw the background using Background - Large.png
        g.drawImage(loadImage("images/Background - Large.png"), 0, 0, null);

        // Apply offsets to sprites then draw them
        for (Sprite s: clouds)
        {
            s.setOffsets(xo,yo);
            s.draw(g);
        }

        // Apply offsets to tile map and draw  it
        tmap.draw(g,xo,yo);

        // Apply offsets to player and draw
        player.setOffsets(xo, yo);


        AffineTransform transform = new AffineTransform();

        // Draw the player
        if (moveRight)
        {
            player.setAnimation(runningRight);
            player.draw(g);
        }
        else if (moveLeft)
        {
            player.setAnimation(runningRight);
            // Flip image horizontally
            transform.translate(player.getWidth(), 0);
            transform.scale(-1, 1);
            player.draw(g);
        }
        else if (attack)
        {
            player.setAnimation(attacking);
            player.draw(g);
        }
        else if (jump)
        {
            player.setAnimation(jumping);
            transform.translate(player.getWidth(), 0);
            transform.scale(-1, 1);
            player.draw(g);
        }
        else
        {
            player.setAnimation(standing);
            player.draw(g);
        }



        // Show score and status information
        String msg = String.format("Score: %d", total/100);
        g.setColor(Color.darkGray);
        g.drawString(msg, getWidth() - 100, 50);

        if (debug)
        {

            // When in debug mode, you could draw borders around objects
            // and write messages to the screen with useful information.
            // Try to avoid printing to the console since it will produce
            // a lot of output and slow down your game.
            tmap.drawBorder(g, xo, yo, Color.black);

            g.setColor(Color.red);
            player.drawBoundingBox(g);

            g.drawString(String.format("Player: %.0f,%.0f", player.getX(),player.getY()),
                    getWidth() - 100, 70);
        }

    }

    /**
     * Update any sprites and check for collisions
     *
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */
    public void update(long elapsed)
    {
        // Make adjustments to the speed of the sprite due to gravity
        player.setVelocityY(player.getVelocityY()+(gravity*elapsed));

        // Then check for any collisions that may have occurred
        handleScreenEdge(player, tmap, elapsed);
        checkTileCollision(player, tmap);

        player.setAnimationSpeed(1.0f);

        if (jump)
        {
            player.setAnimationSpeed(1.8f);
            player.setVelocityY(fly);
            player.setAnimation(jumping);
        }

        if (moveRight)
        {
            player.setVelocityX(moveSpeed);
            player.setAnimation(runningRight);
            player.setFlipped(false);
        }
        else if (moveLeft)
        {
            player.setVelocityX(-moveSpeed);
            player.setAnimation(runningLeft);
            player.setFlipped(true);
        }
        else if (attack)
        {
            player.setAnimation(attacking);

        }
        else
        {
            player.setVelocityX(0);
        }

        for (Sprite s: clouds)
            s.update(elapsed);

        // Now update the sprites animation and position
        player.update(elapsed);


    }


    /**
     * Checks and handles collisions with the edge of the screen. You should generally
     * use tile map collisions to prevent the player leaving the game area. This method
     * is only included as a temporary measure until you have properly developed your
     * tile maps.
     *
     * @param s			The Sprite to check collisions for
     * @param tmap		The tile map to check
     * @param elapsed	How much time has gone by since the last call
     */
    public void handleScreenEdge(Sprite s, TileMap tmap, long elapsed)
    {
        // This method just checks if the sprite has gone off the bottom screen.
        // Ideally you should use tile collision instead of this approach

        float difference = s.getY() + s.getHeight() - tmap.getPixelHeight();
        if (difference > 0)
        {
            // Put the player back on the map according to how far over they were
            s.setY(tmap.getPixelHeight() - s.getHeight() - (int)(difference));

            // and make them bounce
            s.setVelocityY(-s.getVelocityY()*0.75f);
        }
    }

    /**
     * Override of the keyPressed event defined in GameCore to catch our
     * own events
     *
     *  @param e The event that has been generated
     */
    public void keyPressed(KeyEvent e)
    {
        int key = e.getKeyCode();

        switch (key)
        {
            case KeyEvent.VK_UP     : jump = true; break;
            case KeyEvent.VK_RIGHT  : moveRight = true; break;
            case KeyEvent.VK_LEFT   : moveLeft = true; break;

            case KeyEvent.VK_DOWN 	: attack = true;
            // set timer so that the player can only attack once every half second
            if (System.currentTimeMillis() - lastAttack > 500)
            {
                lastAttack = System.currentTimeMillis();
                attack = true;
            }
            else
            {
                attack = false;
            }
            player.setAnimation(attacking);
            // ensure animation plays for its full length
            try {
                Thread.sleep(375);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            break;



            case KeyEvent.VK_S 		: Sound s = new Sound("sounds/caw.wav");
                s.start();
                break;
            case KeyEvent.VK_ESCAPE : stop(); break;
            case KeyEvent.VK_B 		: debug = !debug; break; // Flip the debug state
            default :  break;
        }

    }

    public void keyReleased(KeyEvent e) {

        int key = e.getKeyCode();

        switch (key)
        {
            case KeyEvent.VK_ESCAPE : stop(); break;
            case KeyEvent.VK_UP     : jump = false; break;

            case KeyEvent.VK_DOWN 		: attack = false; break;
            case KeyEvent.VK_RIGHT  : moveRight = false;  break;
            case KeyEvent.VK_LEFT   : moveLeft = false;  break;
            default :  break;
        }
    }

    /** Use the sample code in the lecture notes to properly detect
     * a bounding box collision between sprites s1 and s2.
     *
     * @return	true if a collision may have occurred, false if it has not.
     */
    public boolean boundingBoxCollision(Sprite s1, Sprite s2)
    {
        // Get the bounding boxes for each sprite
        //Rectangle r1 = s1.getBoundingBox();
       // Rectangle r2 = s2.getBoundingBox();

        // Check if the bounding boxes intersect
       // if (r1.intersects(r2))
        {
            // If they do, then check if the individual pixels
            // also intersect
        //    return s1.collidesWith(s2);
        }

        return false;
    }

    /**
     * Check and handles collisions with a tile map for the
     * given sprite 's'. Initial functionality is limited...
     *
     * @param s			The Sprite to check collisions for
     * @param tmap		The tile map to check
     */
    public void checkTileCollision(Sprite s, TileMap tmap) {
        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();
        Rectangle spriteBounds = new Rectangle((int) s.getX(), (int) s.getY(), (int) (s.getWidth() * 0.5f), s.getHeight());
        for (int row = 0; row < tmap.getMapHeight(); row++) {
            for (int col = 0; col < tmap.getMapWidth(); col++) {
                char tileChar = tmap.getTileChar(col, row);
                if (tileChar != '.') {
                    Rectangle tileBounds = new Rectangle((int) (col * tileWidth), (int) (row * tileHeight), (int) tileWidth, (int) tileHeight);
                    if (spriteBounds.intersects(tileBounds)) {
                        // Determine which side of the sprite collided with the tile
                        float xDiff = Math.abs((s.getX() + s.getWidth() / 2) - (col * tileWidth + tileWidth / 2));
                        float yDiff = Math.abs((s.getY() + s.getHeight() / 2) - (row * tileHeight + tileHeight / 2));
                        float w = s.getWidth() / 2 + tileWidth / 2;
                        float h = s.getHeight() / 2 + tileHeight / 2;
                        float dx = w - xDiff;
                        float dy = h - yDiff;
                        // Only move the sprite back in the direction of the collision
                        if (dx < dy) {
                            if (s.getX() < col * tileWidth) {
                                s.setX(s.getX() - (dx/2));
                            } else {
                                s.setX(s.getX() + (dx/2));
                            }
                            s.setVelocity(0, s.getVelocityY());
                        } else {
                            if (s.getY() < row * tileHeight) {
                                s.setY(s.getY() - dy);
                            } else {
                                s.setY(s.getY() + dy);
                            }
                            s.setVelocity(s.getVelocityX(), 0);
                        }}}}}}
}

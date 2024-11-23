import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
  int boardWidth = 360;
  int boardHeight = 640;

  Image backgroundImg;
  Image birdImg;
  Image topPipeImg;
  Image bottomPipeImg;

  int birdX = boardWidth / 8;
  int birdY = boardHeight / 2;
  int birdWidth = 34;
  int birdHeight = 24;

  class Bird {
    int x = birdX;
    int y = birdY;
    int width = birdWidth;
    int height = birdHeight;
    Image img;

    Bird(Image img) {
      this.img = img;
    }
  }

  // PIPES
  int pipeX = boardWidth;
  int pipeY = 0;
  int pipeWidth = 64; // Scaled by 1/6
  int pipeHeight = 512;

  public class Pipe {
    int x = pipeX;
    int y = pipeY;
    int width = pipeWidth;
    int height = pipeHeight;
    Image img;
    boolean passed = false;

    Pipe(Image img) {
      this.img = img;
    }
  }

  // GAME LOGIC
  Bird bird;
  int velocityX = -4;
  int velocityY = 0;
  int gravity = 1;

  ArrayList<Pipe> pipes;
  Random random = new Random();

  // GAME TIMERS
  Timer gameLoop;
  Timer placePipesTimer;
  boolean gameOver = false;
  double score = 0;

  FlappyBird() {
    setPreferredSize(new Dimension(boardWidth, boardHeight));
    // setBackground(Color.blue);
    setFocusable(true);
    addKeyListener(this);

    // LOAD IMAGES
    backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
    birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
    topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
    bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

    // BIRD
    bird = new Bird(birdImg);

    // PIPE
    pipes = new ArrayList<Pipe>();

    // PLACE PIPES TIMER
    placePipesTimer = new Timer(1500, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        placePipes();
      }
    });
    placePipesTimer.start();

    // GAME TIMER
    gameLoop = new Timer(1000 / 60, this);
    gameLoop.start();
  }

  /**
   * Place pipes on the board
   */
  public void placePipes() {
    int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
    int openingSpace = boardHeight / 4;

    Pipe topPipe = new Pipe(topPipeImg);
    topPipe.y = randomPipeY;
    pipes.add(topPipe);

    Pipe bottomPipe = new Pipe(bottomPipeImg);
    bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
    pipes.add(bottomPipe);
  }

  public void paintComponent (Graphics g) {
    super.paintComponent(g);
    draw(g);
  }

  public void draw(Graphics g) {
    // DRAW BACKGROUND
    g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

    // DRAW BIRD
    g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

    // DRAW PIPES
    for (int i = 0; i < pipes.size(); i++) {
      Pipe pipe = pipes.get(i);
      g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
    }

    // SCORE
    g.setColor(Color.white);
    g.setFont(new Font("Arial", Font.PLAIN, 32));
    if (gameOver) {
      g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
    } else {
      g.drawString(String.valueOf((int) score), 10, 35);
    }
  }

  public void move() {
    // MOVE BIRD
    velocityY += gravity;
    bird.y += velocityY;
    bird.y = Math.max(bird.y, 0);

    // MOVE PIPES
    for (int i = 0; i < pipes.size(); i++) {
      Pipe pipe = pipes.get(i);
      pipe.x += velocityX;

      if (!pipe.passed && bird.x > pipe.x + pipe.width) {
        pipe.passed = true;
        score += 0.5; // Added for each pipe passed, which is two per set.
      }

      if (collision(bird,  pipe)) {
        gameOver = true;
      }
    }

    if (bird.y > boardHeight) {
      gameOver = true;
    }
  }

  public boolean collision(Bird a, Pipe b) {
    return a.x < b.x + b.width &&
      a.x + a.width > b.x &&
      a.y < b.y + b.height &&
      a.y + a.height > b.y;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    move();
    repaint();
    if (gameOver) {
      placePipesTimer.stop();
      gameLoop.stop();
    }
  }

  @Override
  public void keyTyped(KeyEvent e) {}

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
      velocityY = -9;
      if (gameOver) {
        // Restart game
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        score = 0;
        gameOver = false;
        gameLoop.start();
        placePipesTimer.start();
      }
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {}
}
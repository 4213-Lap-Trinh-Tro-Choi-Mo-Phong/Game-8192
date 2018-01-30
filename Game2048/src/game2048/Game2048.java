package game2048;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Game2048 extends JPanel 
{
  private static final Color BG_COLOR = new Color(0xa9a9a9);
  private static final String FONT_NAME = "Arial";
  private static final int TILE_SIZE = 64;
  private static final int TILES_MARGIN = 16;

  private Tile[] myTiles;
  boolean myWin = false;
  boolean myLose = false;
  int myScore = 0;

  public Game2048() 
  {
    setFocusable(true);
    addKeyListener(new KeyAdapter() 
    {
      @Override
      public void keyPressed(KeyEvent e) 
      {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) 
        {
          resetGame();
        }
        if (!canMove()) 
        {
          myLose = true;
        }

        if (!myWin && !myLose) 
        {
          switch (e.getKeyCode()) 
          {
            case KeyEvent.VK_LEFT:
              left();
              break;
            case KeyEvent.VK_RIGHT:
              right();
              break;
            case KeyEvent.VK_DOWN:
              down();
              break;
            case KeyEvent.VK_UP:
              up();
              break;
          }
        }

        if (!myWin && !canMove()) 
        {
          myLose = true;
        }

        repaint();
      }
    });
    resetGame();
  }
  
// reset Game
  
  public void resetGame() 
  {
    myScore = 0;
    myWin = false;
    myLose = false;
    myTiles = new Tile[6 * 6];
    for (int i = 0; i < myTiles.length; i++) 
    {
      myTiles[i] = new Tile();
    }
    addTile();
    addTile();
  }

// Di chuyển trái
  
  public void left() 
  {
    boolean needAddTile = false;
    for (int i = 0; i < 6; i++) 
    {
      Tile[] line = getLine(i);
      Tile[] merged = mergeLine(moveLine(line));
      setLine(i, merged);
      if (!needAddTile && !compare(line, merged)) 
      {
        needAddTile = true;
      }
    }

    if (needAddTile) 
    {
      addTile();
    }
  }

  public void right() 
  {
    myTiles = rotate(180);
    left();
    myTiles = rotate(180);
  }

  public void up() 
  {
    myTiles = rotate(270);
    left();
    myTiles = rotate(90);
  }

  public void down() 
  {
    myTiles = rotate(90);
    left();
    myTiles = rotate(270);
  }

  private Tile tileAt(int x, int y) 
  {
    return myTiles[x + y *6];
  }

  private void addTile() {
    List<Tile> list = availableSpace();
    if (!availableSpace().isEmpty()) 
    {
      int index = (int) (Math.random() * list.size()) % list.size();
      Tile emptyTime = list.get(index);
      emptyTime.value = Math.random() < 0.9 ? 2 : 4;
    }
  }

  private List<Tile> availableSpace() 
  {
    final List<Tile> list = new ArrayList<Tile>(36);
    for (Tile t : myTiles) 
    {
      if (t.isEmpty()) 
      {
        list.add(t);
      }
    }
    return list;
  }

  private boolean isFull() 
  {
    return availableSpace().size() == 0;
  }

  boolean canMove() {
    if (!isFull()) {
      return true;
    }
    for (int x = 0; x < 6; x++) 
    {
      for (int y = 0; y < 6; y++) 
      {
        Tile t = tileAt(x, y);
        if ((x < 5 && t.value == tileAt(x + 1, y).value)
          || ((y < 5) && t.value == tileAt(x, y + 1).value)) 
        {
          return true;
        }
      }
    }
    return false;
  }

  private boolean compare(Tile[] line1, Tile[] line2) 
  {
    if (line1 == line2) 
    {
      return true;
    } else if (line1.length != line2.length) 
    {
      return false;
    }

    for (int i = 0; i < line1.length; i++) 
    {
      if (line1[i].value != line2[i].value) 
      {
        return false;
      }
    }
    return true;
  }

  private Tile[] rotate(int angle) 
  {
    Tile[] newTiles = new Tile[6 * 6];
    int offsetX = 5, offsetY = 5;
    if (angle == 90) 
    {
      offsetY = 0;
    } else if (angle == 270) 
    {
      offsetX = 0;
    }

    double rad = Math.toRadians(angle);
    int cos = (int) Math.cos(rad);
    int sin = (int) Math.sin(rad);
    for (int x = 0; x < 6; x++) 
    {
      for (int y = 0; y < 6; y++) 
      {
        int newX = (x * cos) - (y * sin) + offsetX;
        int newY = (x * sin) + (y * cos) + offsetY;
        newTiles[(newX) + (newY) * 6] = tileAt(x, y);
      }
    }
    return newTiles;
  }

  private Tile[] moveLine(Tile[] oldLine) 
  {
    LinkedList<Tile> l = new LinkedList<Tile>();
    for (int i = 0; i < 6; i++) 
    {
      if (!oldLine[i].isEmpty())
        l.addLast(oldLine[i]);
    }
    if (l.size() == 0) 
    {
      return oldLine;
    } else 
    {
      Tile[] newLine = new Tile[6];
      ensureSize(l, 6);
      for (int i = 0; i < 6; i++) 
      {
        newLine[i] = l.removeFirst();
      }
      return newLine;
    }
  }

  private Tile[] mergeLine(Tile[] oldLine) 
  {
    LinkedList<Tile> list = new LinkedList<Tile>();
    for (int i = 0; i < 6 && !oldLine[i].isEmpty(); i++) 
    {
      int num = oldLine[i].value;
      if (i < 5 && oldLine[i].value == oldLine[i + 1].value) 
      {
        num *= 2;
        myScore += num;
        int ourTarget = 8192;
        if (num == ourTarget) 
        {
          myWin = true;
        }
        i++;
      }
      list.add(new Tile(num));
    }
    if (list.size() == 0) 
    {
      return oldLine;
    } else {
      ensureSize(list, 6);
      return list.toArray(new Tile[6]);
    }
  }

  private static void ensureSize(java.util.List<Tile> l, int s) 
  {
    while (l.size() != s) 
    {
      l.add(new Tile());
    }
  }

  private Tile[] getLine(int index) 
  {
    Tile[] result = new Tile[6];
    for (int i = 0; i < 6; i++) 
    {
      result[i] = tileAt(i, index);
    }
    return result;
  }

  private void setLine(int index, Tile[] re) 
  {
    System.arraycopy(re, 0, myTiles, index * 6, 6);
  }

  @Override
  public void paint(Graphics g) 
  {
    super.paint(g);
    g.setColor(BG_COLOR);
    g.fillRect(0, 0, this.getSize().width, this.getSize().height);
    for (int y = 0; y < 6; y++) 
    {
      for (int x = 0; x < 6; x++) 
      {
        drawTile(g, myTiles[x + y * 6], x, y);
      }
    }
  }
 //Giao diện
  
  private void drawTile(Graphics g2, Tile tile, int x, int y) 
  {
    Graphics2D g = ((Graphics2D) g2);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
    int value = tile.value;
    int xOffset = offsetCoors(x);
    int yOffset = offsetCoors(y);
    g.setColor(tile.getBackground());
    g.fillRoundRect(xOffset, yOffset, TILE_SIZE, TILE_SIZE, 14, 14); // các o cach nhau 14
    g.setColor(tile.getForeground());
    final int size = value < 100 ? 36 : value < 1000 ? 32 : 24;
    final Font font = new Font(FONT_NAME, Font.BOLD, size);
    g.setFont(font);

    String s = String.valueOf(value);
    final FontMetrics fm = getFontMetrics(font);

    final int w = fm.stringWidth(s);
    final int h = -(int) fm.getLineMetrics(s, g).getBaselineOffsets()[2];

    if (value != 0)
      g.drawString(s, xOffset + (TILE_SIZE - w) / 2, yOffset + TILE_SIZE - (TILE_SIZE - h) / 2 - 2);

        if (myWin || myLose) 
        {
          g.setColor(new Color(255, 255, 255, 30));
          g.fillRect(0, 0, getWidth(), getHeight());
          g.setColor(new Color(78, 139, 202));
          g.setFont(new Font(FONT_NAME, Font.BOLD, 48));
          if (myWin) 
          {
            g.drawString("Chiến thắng!", 68, 150);   
          }
          if (myLose) 
          {
            g.drawString("Game over!", 110, 130);
            g.drawString("Bạn thua!", 150, 200);
          }
          if (myWin || myLose) 
          {
            g.setFont(new Font(FONT_NAME, Font.PLAIN, 16));
            g.setColor(new Color(128, 128, 128, 128));
            g.drawString("Nhấn ESC để chơi lại", 80, getHeight() - 40);
          }
        }
    g.setFont(new Font(FONT_NAME, Font.PLAIN, 25));
    g.drawString("Điểm: " + myScore, 300, 545);
  }

  private static int offsetCoors(int arg) 
  {
    return arg * (TILES_MARGIN + TILE_SIZE) + TILES_MARGIN;
  }

  static class Tile 
  {
    int value;

    public Tile() 
    {
      this(0);
    }

    public Tile(int num) 
    {
      value = num;
    }

    public boolean isEmpty() 
    {
      return value == 0;
    }

    public Color getForeground() 
    {
      return value < 36 ? new Color(0xffffff) :  new Color(0xf9f6f2);
    }
// Màu 
    public Color getBackground() 
    {
      switch (value) 
      {
        case 2:    return new Color(0x0000ff);
        case 4:    return new Color(0x39b54a);
        case 8:    return new Color(0xfff200);
        case 16:   return new Color(0xf7941d);
        case 32:   return new Color(0x9e0b0f);
        case 64:   return new Color(0x440e62);
        case 128:  return new Color(0xa67c52);
        case 256:  return new Color(0x754c24);
        case 512:  return new Color(0xec008c);
        case 1024: return new Color(0x00bff3);
        case 2048: return new Color(0x7a7676);
        case 4096: return new Color(0xedc000);
        case 8192: return new Color(0xedab32);      
      }
      return new Color(0xffffff);
    }
  }

  public static void main(String[] args) 
  {
    JFrame game = new JFrame();
    game.setTitle("Game 8192");
    game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    game.setSize(500, 600);
    game.setResizable(false);

    game.add(new Game2048());

    game.setLocationRelativeTo(null);
    game.setVisible(true);
  }
}

import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.*;


// Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  // creating cells without adjacents because some cells won't have all adjacent
  Cell(int x, int y, Color color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
  }

  // used in tests only
  Cell(int x, int y, Color color, boolean flooded, Cell left, Cell top, Cell right, Cell bottom) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }

  // adding a cell to the left (if applicable)
  public void addLeft(Cell c) {
    this.left = c;
  }

  // adding a cell to the right (if applicable)
  public void addRight(Cell c) {
    this.right = c;
  }

  // adding a cell to the top (if applicable)
  public void addTop(Cell c) {
    this.top = c;
  }

  // adding a cell to the bottom (if applicable)
  public void addBottom(Cell c) {
    this.bottom = c;
  }

  // making adjacent cells flooded if colors are the same
  public void floodAdj(Cell c) {
    if (c != null && this.color.equals(c.color) && this.flooded) {
      c.flooded = true;
    }
  }

  // drawing the cell
  public RectangleImage drawCell() {
    return new RectangleImage(20, 20, OutlineMode.SOLID, this.color);
  }

  // putting cell on the world scene
  public void draw(WorldScene acc) {
    acc.placeImageXY(this.drawCell(), this.x, this.y);
  }

  // changes colors on board
  public void change(Color clr, Cell toCompare) {
    if (this.flooded) {
      this.color = clr;
    }
    if (toCompare != null && toCompare.flooded && this.color.equals(clr)) {
      this.flooded = true;
      this.color = clr;
    }
  }

  // method to win the game
  public boolean win(ArrayList<Cell> list) {
    boolean flag = true;
    for (int i = 0; i < list.size(); i++) {
      if (!list.get(i).flooded) {
        flag = false;
        break;
      }
    }
    return flag;
  }
}

// represents the world with elements of the game
class FloodItWorld extends World {
  // All the cells of the game
  ArrayList<Cell> board;
  int size; // size of board (how many cells are on each side)
  int colors; // number of colors used

  // list of all possible color choices
  ArrayList<Color> colorChoices = new ArrayList<>(Arrays.asList(Color.MAGENTA, Color.BLUE,
      Color.ORANGE, Color.RED, Color.GREEN, Color.PINK, Color.CYAN, Color.GRAY, Color.YELLOW,
      Color.BLACK, Color.LIGHT_GRAY, Color.DARK_GRAY));

  Random rand;
  boolean shouldChange;
  Color changeCellColor;
  int counter;
  double elapsedTime;
  int maxTurns; // maximum # of clicks allowed
  int currentTurns; // how many clicks have been used?

  FloodItWorld(int size, int colors) {
    this.size = size;
    this.colors = colors;
    this.board = new ArrayList<>();
    this.rand = new Random();
    this.shouldChange = false;
    this.changeCellColor = Color.WHITE;
    this.counter = 0;
    this.elapsedTime = 0.0;
    this.maxTurns = ((this.size * this.size / this.colors) + 8);
    this.currentTurns = 0;

    // makes sure color input is within range
    if (this.colors <= 1) {
      throw new IllegalArgumentException("Too few colors!");
    }
    else if (this.colors >= this.colorChoices.size()) {
      throw new IllegalArgumentException("Too many colors!");
    }
    this.generateBoard();
  }

  // this is a test constructor only
  FloodItWorld(int size, int colors, Random rand) {
    this.size = size;
    this.colors = colors;
    this.board = new ArrayList<>();
    this.rand = rand;
    this.shouldChange = false;
    this.changeCellColor = Color.WHITE;
    this.counter = 0;
    this.elapsedTime = 0.0;
    this.maxTurns = ((this.size * this.size / this.colors) + 8);
    this.currentTurns = 0;

    // makes sure color input is within range
    if (this.colors <= 1) {
      throw new IllegalArgumentException("Too few colors!");
    }
    else if (this.colors >= this.colorChoices.size()) {
      throw new IllegalArgumentException("Too many colors!");
    }
  }

  // adds top, right, bottom, and/or left cells if applicable
  // i is x-axis (row #), j is y-axis (column #) - top left corner is (0,0)
  public void generateBoard() {
    boolean flooded = true;
    for (int i = 0; i < this.size; i++) {
      for (int j = 0; j < this.size; j++) {
        this.board.add(new Cell(150 + (i * 20), 100 + (j * 20),
            this.chooseColor(this.rand), flooded));
        flooded = false;

        // adding a top cell (if possible)
        if (i != 0) {
          // sets top of cell as the cell directly above
          this.board.get(this.board.size() - 1)
            .addTop(this.board.get(this.board.size() - this.size - 1));
        }

        // adding a bottom cell (if possible)
        if (i >= 1) {
          // sets bottom of cell directly above to the most recently added cell
          this.board.get(this.board.size() - this.size - 1)
            .addBottom(this.board.get(this.board.size() - 1));
        }

        // adding a left cell (if possible)
        if (j > 0) {
          // sets left cell as the cell added right before most recent one
          this.board.get(this.board.size() - 1)
            .addLeft(this.board.get(this.board.size() - 2));
        }

        // adding a right cell (if possible)
        if ((j != 0) && (this.board.size() - 2 >= 0)) {
          // sets right cell of previously added cell to the most recently added cell
          this.board.get(this.board.size() - 2)
            .addRight(this.board.get(this.board.size() - 1));
        }
      }
    }
    // flooding adjacent cells
    for (int x = 0; x < this.board.size(); x++) {
      this.board.get(x).floodAdj(this.board.get(x).right);
      this.board.get(x).floodAdj(this.board.get(x).left);
      this.board.get(x).floodAdj(this.board.get(x).top);
      this.board.get(x).floodAdj(this.board.get(x).bottom);
    }
  }

  // randomly chooses a color based on the number of colors the user inputs
  Color chooseColor(Random rand) {
    int clrIdx = rand.nextInt(this.colors);
    return this.colorChoices.get(clrIdx);
  }

  // draws the world scene
  public void draw(WorldScene acc) {
    int i = 0;
    while (i < this.board.size()) {
      this.board.get(i).draw(acc);
      i++;
    }
  }

  // returns number of tiles that are flooded in the game
  public int numFlooded() {
    int numFl = 0;
    for (Cell c: this.board) {
      if (c.flooded) {
        numFl++;
      }
    }
    return numFl;
  }

  // generates the world
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(500, 500);
    this.draw(scene);
    scene.placeImageXY(new TextImage("Timer: "
        + (int) this.elapsedTime + "s",
        20.0, Color.BLACK), 150, 400);
    scene.placeImageXY((new TextImage("Turns: " + this.currentTurns + "/" + this.maxTurns, 20,
        Color.BLACK)), 400, 400);
    if (this.currentTurns == 0) {
      scene.placeImageXY(new TextImage("Score: 0", 20.0, Color.BLACK), 200, 50);
    }
    else {
      scene.placeImageXY(new TextImage(("Score: " + (this.numFlooded() + (int)
          ((this.numFlooded() / this.currentTurns)) * 100)), 20.0, Color.BLACK), 200, 50);
    }
    return scene;
  }

  // handles events when mouse is clicked
  public void onMouseClicked(Posn mouse) {
    Cell currentElem;
    for (int i = 0; i < this.board.size(); i++) {
      currentElem = this.board.get(i);
      if (mouse.x <= currentElem.x + 10 && mouse.x >= currentElem.x - 10
          && mouse.y <= currentElem.y + 10 && mouse.y >= currentElem.y - 10) {
        this.changeCellColor = currentElem.color;
        this.counter = 0;
        if (!this.changeCellColor.equals(this.board.get(0).color)) {
          this.currentTurns++;
          this.shouldChange = true;
        }
      }
    }
  }

  // changes board if it should be changed
  public void change() {
    if (this.shouldChange && this.counter < (this.size * this.size)) {
      Cell x = this.board.get(this.counter);
      x.change(this.changeCellColor, x.left);
      x.change(this.changeCellColor, x.right);
      x.change(this.changeCellColor, x.top);
      x.change(this.changeCellColor, x.bottom);
      this.counter++;
    } else {
      this.shouldChange = false;
      this.counter = 0;
    }
  }

  // resets the game
  public void onKeyEvent(String s) {
    if (s.equals("r")) {
      while (this.board.size() > 0) {
        this.board.remove(this.board.size() - 1);
      }
      this.elapsedTime = 0;
      this.generateBoard();
    }
  }

  // handler for what to do on each tick
  public void onTick() {
    if (this.board.get(0).win(this.board)) {
      this.endOfWorld("You win :)");
    }
    if (this.currentTurns > this.maxTurns) {
      this.endOfWorld("You lost :(");
    }
    this.change();

    this.elapsedTime += 1 / 73.0;
  }

  // displays a game end scene with text
  public WorldScene lastScene(String s) {
    WorldScene scene =  new WorldScene(500, 500);
    scene.placeImageXY(new TextImage(s, 50, Color.RED), 250, 250);
    return scene;
  }
}


// represents examples of cells
class ExamplesCell {
  FloodItWorld world = new FloodItWorld(10, 6);
  FloodItWorld testWorld;
  FloodItWorld fourCells = new FloodItWorld(2, 3, new Random(8));
  Cell c1;
  Cell c2;
  Cell c3;
  Cell c4;
  Cell c5;
  Cell c6;

  // initializes the world
  void init() {
    this.testWorld = new FloodItWorld(5, 3, new Random(8));
    this.c1 = new Cell(150, 100, Color.BLUE, true);
    this.c2 = new Cell(170, 100, Color.BLUE, false);
    this.c3 = new Cell(150, 120, Color.BLUE, false);
    this.c4 = new Cell(170, 120, Color.BLUE, false);
    this.c5 = new Cell(200, 300, Color.RED, false);
    this.c6 = new Cell(150, 100, Color.BLUE, true);
  }

  // test the method to draw each individual cell
  boolean testDrawCell(Tester t) {
    this.init();
    return t.checkExpect(this.c1.drawCell(), new RectangleImage(20, 20, OutlineMode.SOLID,
        Color.BLUE))
        && t.checkExpect(this.c3.drawCell(), new RectangleImage(20, 20, OutlineMode.SOLID,
            Color.BLUE));
  }

  // testing the method to randomly choose color
  boolean testChooseColor(Tester t) {
    this.init();
    return t.checkExpect(this.world.chooseColor(new Random(4)), new Color(255, 200, 0))
        && t.checkExpect(this.world.chooseColor(new Random(12)), new Color(255, 0, 255))
        && t.checkExpect(this.world.chooseColor(new Random(2)), new Color(0, 255, 0));
  }

  // testing constructor exceptions
  boolean testExceptions(Tester t) {
    this.init();
    return t.checkConstructorException(new IllegalArgumentException("Too few colors!"),
        "FloodItWorld", 10, 1)
        && t.checkConstructorException(new IllegalArgumentException("Too many colors!"),
            "FloodItWorld", 8, 12);
  }

  // testing generateBoard
  void testGenerateBoard(Tester t) {
    this.init();
    this.testWorld.generateBoard();
    t.checkExpect(this.testWorld.board.size(), 25);
    t.checkExpect(this.testWorld.board.get(0).right, this.testWorld.board.get(1));
    t.checkExpect(this.testWorld.board.get(0).top, null);
    t.checkExpect(this.testWorld.board.get(0).left, null);
    t.checkExpect(this.testWorld.board.get(0).bottom, this.testWorld.board.get(5));

    t.checkExpect(this.testWorld.board.get(1).left, this.testWorld.board.get(0));
    t.checkExpect(this.testWorld.board.get(9).right, null);
    t.checkExpect(this.testWorld.board.get(21).bottom, null);
    t.checkExpect(this.testWorld.board.get(21).top, this.testWorld.board.get(16));

    t.checkExpect(this.testWorld.board.get(0).flooded, true);
    t.checkExpect(this.testWorld.board.get(1).flooded, true);
    t.checkExpect(this.testWorld.board.get(5).flooded, false);
  }

  // testing the method to add a left cell
  void testAddLeft(Tester t) {
    this.init();
    this.c1.addLeft(this.c2);
    this.c2.addLeft(this.c3);
    t.checkExpect(this.c1.left, this.c2);
    t.checkExpect(this.c2.left, this.c3);
  }

  // testing the method to add a right cell
  void testAddRight(Tester t) {
    this.init();
    this.c3.addRight(this.c1);
    this.c2.addRight(this.c3);
    t.checkExpect(this.c3.right, this.c1);
    t.checkExpect(this.c2.right, this.c3);
  }

  // testing the method to add a top cell
  void testAddTop(Tester t) {
    this.init();
    this.c1.addTop(this.c2);
    this.c3.addTop(this.c2);
    t.checkExpect(this.c1.top, this.c2);
    t.checkExpect(this.c3.top, this.c2);
  }

  // testing the method to add a bottom cell
  void testAddBottom(Tester t) {
    this.init();
    this.c2.addBottom(this.c1);
    this.c3.addBottom(this.c2);
    t.checkExpect(this.c2.bottom, this.c1);
    t.checkExpect(this.c3.bottom, this.c2);
  }

  // testing the method to draw the scene
  void testDraw(Tester t) {
    this.init();
    WorldScene scene = new WorldScene(500, 500);
    WorldScene testScene = new WorldScene(500, 500);
    this.c1.draw(scene);
    testScene.placeImageXY(this.c1.drawCell(), 150, 100);
    t.checkExpect(scene, testScene);
    this.c2.draw(scene);
    testScene.placeImageXY(this.c2.drawCell(), 170, 100);
    t.checkExpect(scene, testScene);

    WorldScene floodItTestWorld = new WorldScene(500, 500);
    WorldScene floodItWorld = new WorldScene(500, 500);

    this.fourCells.generateBoard();
    floodItTestWorld.placeImageXY(this.c1.drawCell(), this.c1.x, this.c1.y);
    floodItTestWorld.placeImageXY(this.c2.drawCell(), this.c2.x, this.c2.y);
    floodItTestWorld.placeImageXY(this.c3.drawCell(), this.c3.x, this.c3.y);
    floodItTestWorld.placeImageXY(this.c4.drawCell(), this.c4.x, this.c4.y);
    this.fourCells.draw(floodItWorld);
    t.checkExpect(floodItTestWorld, floodItWorld);
  }

  // testing the method to flood adjacent cells
  void testFloodAdj(Tester t) {
    this.init();
    this.c1.floodAdj(this.c2);
    this.c3.floodAdj(this.c4);
    this.c3.floodAdj(this.c1);
    this.c1.floodAdj(this.c5);
    this.c1.floodAdj(null); // testing to see if the code will still run if null input is given
    t.checkExpect(this.c2.flooded, true);
    t.checkExpect(this.c4.flooded, false);
    t.checkExpect(this.c1.flooded, true);
    t.checkExpect(this.c5.flooded, false);
  }

  // testing the method to change the colors on the board
  void testChange(Tester t) {
    this.init();
    this.c6.change(Color.ORANGE, this.c2);
    this.c1.change(Color.RED, null); // testing if code will run if null input is given
    this.c5.change(Color.PINK, this.c1);
    t.checkExpect(this.c6.color, Color.ORANGE);
    t.checkExpect(this.c5.color, Color.RED);
  }

  // testing the method to see if the game has been won
  void testWin(Tester t) {
    this.init();
    ArrayList<Cell> lst1 = new ArrayList<>(Arrays.asList(this.c1, this.c2, this.c3, this.c4));
    ArrayList<Cell> lst2 = new ArrayList<>(Arrays.asList(this.c1, this.c6));
    ArrayList<Cell> mt = new ArrayList<>();
    t.checkExpect(this.c1.win(lst1), false);
    t.checkExpect(this.c2.win(lst2), true);
    t.checkExpect(this.c3.win(mt), true);
  }

  // testing the method to return the number of tiles flooded in the game
  void testNumFlooded(Tester t) {
    this.init();
    t.checkExpect(this.testWorld.board, new ArrayList<Cell>());
    t.checkExpect(this.testWorld.numFlooded(), 0);
  }

  // testing the method to handle the mouse click event
  void testOnMouseClicked(Tester t) {
    this.init();
    this.fourCells.onMouseClicked(new Posn(10, 10));
    t.checkExpect(this.fourCells.currentTurns, 0);
    t.checkExpect(this.fourCells.changeCellColor, Color.WHITE);
    t.checkExpect(this.fourCells.counter, 0);
    t.checkExpect(this.fourCells.shouldChange, false);

    Cell newC2 = new Cell(170, 100, Color.RED, false);
    this.testWorld.board.set(1, newC2);
    this.c2.color = Color.RED;
    this.testWorld.board.set(1, this.c2);
    this.testWorld.onMouseClicked(new Posn(161, 125));
    t.checkExpect(this.testWorld.currentTurns, 1);
    t.checkExpect(this.testWorld.changeCellColor, Color.RED);
    t.checkExpect(this.testWorld.shouldChange, true);
  }

  // testing the method to handle on key events
  void testOnKeyEvent(Tester t) {
    this.init();
    this.fourCells.onKeyEvent("s");
    t.checkExpect(this.fourCells.elapsedTime, this.fourCells.elapsedTime);
    this.testWorld.onKeyEvent("r");
    t.checkExpect(this.testWorld.elapsedTime, 0.0);
  }

  // testing the method to handle what to do on each tick
  void testOnTick(Tester t) {
    this.init();
    this.fourCells.onTick();
    t.checkExpect(this.fourCells.elapsedTime, 0.0136986301369863);

    this.testWorld.onTick();
    this.testWorld.onTick();
    t.checkExpect(this.testWorld.elapsedTime, 0.0273972602739726);
  }

  // testing the method to display an end game scene
  void testLastScene(Tester t) {
    this.init();
    WorldScene empty = new WorldScene(500, 500);
    empty.placeImageXY(new TextImage("", 50, Color.RED), 250, 250);
    t.checkExpect(this.testWorld.lastScene(""), empty);

    WorldScene mt2 = new WorldScene(500, 500);
    mt2.placeImageXY(new TextImage("You won :)", 50, Color.RED), 250, 250);
    t.checkExpect(this.fourCells.lastScene("You won :)"), mt2);
  }

  // running the code
  void testBigBang(Tester t) {
    new FloodItWorld(10, 6).bigBang(500, 500, .01);
  }
}

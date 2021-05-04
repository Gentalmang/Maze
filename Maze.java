import edu.princeton.cs.algs4.UF;
import edu.princeton.cs.algs4.Graph;
import edu.princeton.cs.algs4.BreadthFirstPaths;
import edu.princeton.cs.algs4.StdDraw;

import java.util.*;
import java.lang.*;

public class Maze {
    static final int LEFT=0, RIGHT=1, UP=2, DOWN=3;

    static final int CELLSIZE=25;
    static final int twoDBooleanSet=1;
    static final int bitSetSet=2;
    static final int hashSetSet=3;
    static final int bloomFilterSet=4;


    static final int N = 40; // grid is NxN
    static final double GRAPHICSSCALE = 1.0/N;

    int topLeft, bottomRight; // node IDs
    Random r;
    UF uf;  // union find structures
    Set<Integer> removedWallsSouth;
    Set<Integer> removedWallsEast;
    BitSet removedWallsBitSouth;
    BitSet removedWallsBitEast;
    boolean[][] south;
    boolean[][] east;
    BitSet filterSouth;
    BitSet filterEast;
    int setKindCode;
    Graph theMaze;
    BreadthFirstPaths bfs;
    BitSet path;

    boolean drawOnGraphicsScreen;

    public static void main(String [] args) {
        int setKindCode = 1;
        if (setKindCode < 0 || setKindCode > bloomFilterSet)
            throw new RuntimeException("illegal setKind Code on cmd line");

        Maze m = new Maze(setKindCode,true); // will draw on grahics

        m.generate();
        m.solveTheMaze();
        m.printAsciiGraphics();
     }

    // a bunch of methods to map between cell ids (0,1,...N^2-1) and x,y coords
    int xyToId(int x, int y) {
        return y*N + x;
    }

    int idToX(int id) { return id % N;}
    int idToY(int id) { return id / N;}

    int idOfCellAbove(int id) { return id - N;}
    int idOfCellBelow(int id) { return id + N;}
    int idOfCellLeft(int id) { return id-1;}
    int idOfCellRight(int id) { return id+1;}


    public Maze(int setKindCode, boolean graphicsDraw) {
        uf = new UF(N*N);
        theMaze = new Graph(N*N);
        // our maze requires us to get from top left
        // to bottom right.
        this.setKindCode = setKindCode;
        System.out.println("The kind code is :" + this.setKindCode);

        switch(setKindCode){
          case twoDBooleanSet:
            south = new boolean[N][N];
            east = new boolean[N][N];
            for (int i = 0; i < N ; i++ ) {
              for (int k = 0 ; k < N ; k++ ) {
                south[i][k] = false;
                east[i][k] = false;
              }
            }
            break;
          case bitSetSet:
            removedWallsBitSouth = new BitSet();
            removedWallsBitEast = new BitSet();
            break;
          case hashSetSet:
            removedWallsSouth = new HashSet<>();
            removedWallsEast = new HashSet<>();
            break;
          case bloomFilterSet:
            filterSouth = new BitSet();
            filterEast = new BitSet();
            break;
        }

        topLeft = xyToId(0,0);
        bottomRight = xyToId(N-1,N-1);
        r = new Random();

        drawOnGraphicsScreen = graphicsDraw;
        if (graphicsDraw) {
            StdDraw.setCanvasSize(N*CELLSIZE,N*CELLSIZE);
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setPenRadius(0.004);
            StdDraw.clear();

            // initial grid
            // StdDraw puts the origin at the BOTTOM left, so
            // things are basically "upside down" when visualized
            // with it.....unless we do these crazy (1-y) transform to
            // fix this.

            for (int i=0; i <= N; ++i) {
                StdDraw.line(0, 1- i/(double)N, 1, 1-i/(double) N);
                StdDraw.line(i/(double)N, 0, i/(double)N,  1);
            }
            StdDraw.setPenRadius(0.006); // don't want shadow of former line
        }
    }

    int hashAString(String a){
      int hashValue = 0;
      for (int i = 0; i < a.length(); i++) {
        hashValue = (hashValue * 31 + a.charAt(i));
      }
      return hashValue%35000;
    }

    void generate() {
        while (!uf.connected(topLeft, bottomRight)) {
            // choose a random cell
            int randX = r.nextInt(N);
            int randY = r.nextInt(N);
            int randId = xyToId(randX,randY);

            // choose one of its 4 neighbours
            int randDir = r.nextInt(4);

            // knock down a wall, if present, between cell and
            // its chosen neighbour if they are not yet in the
            // same component

            switch(randDir) {
            case LEFT:
                if (randX != 0)
                    connectIfNotConnected(randId, idOfCellLeft(randId));
                break;
            case RIGHT:
                if (randX != N-1)
                    connectIfNotConnected(randId, idOfCellRight(randId));
                break;
            case UP:
                if (randY != 0)
                    connectIfNotConnected(randId, idOfCellAbove(randId));
                break;
            case DOWN:
                if (randY != N-1)
                    connectIfNotConnected(randId, idOfCellBelow(randId));
                break;
            }
        }
        bfs = new BreadthFirstPaths(theMaze,0);
        path = new BitSet();
        Iterable<Integer> iter = bfs.pathTo((N * N-1));
        for (int a : iter) {
          path.set(a);
        }
    }


    void connectIfNotConnected(int id1, int id2) {
        if ( ! uf.connected(id1, id2)) {
            // knock out the wall and thereby merge components
            uf.union(id1,id2);

            theMaze.addEdge(idToY(id1)*N + idToX(id1),idToY(id2)*N + idToX(id2));

            switch(setKindCode){
              case twoDBooleanSet:
                switch(id2 - id1){
                  case 1:
                    east[idToY(id1)][idToX(id1)] = true;
                    break;
                  case -1:
                    east[idToY(id2)][idToX(id2)] = true;
                    break;
                  case -N:
                    south[idToY(id2)][idToX(id2)] = true;
                    break;
                  case N:
                    south[idToY(id1)][idToX(id1)] = true;
                    break;
                }
                break;
              case bitSetSet:
                switch(id2 - id1){
                  case 1:
                    removedWallsBitEast.set(idToY(id1)*N + idToX(id1));
                    break;
                  case -1:
                    removedWallsBitEast.set(idToY(id2)*N + idToX(id2));
                    break;
                  case -N:
                    removedWallsBitSouth.set(idToY(id2)*N + idToX(id2));
                    break;
                  case N:
                    removedWallsBitSouth.set(idToY(id1)*N + idToX(id1));
                    break;
                }
                break;
              case hashSetSet:
                switch(id2 - id1){
                  case 1:
                    removedWallsEast.add(idToY(id1)*N + idToX(id1));
                    break;
                  case -1:
                    removedWallsEast.add(idToY(id2)*N + idToX(id2));
                    break;
                  case -N:
                    removedWallsSouth.add(idToY(id2)*N + idToX(id2));
                    break;
                  case N:
                    removedWallsSouth.add(idToY(id1)*N + idToX(id1));
                    break;
                }
                break;
              case bloomFilterSet:
                String id1String = idToY(id1)+";"+idToX(id1);
                String id2String = idToY(id2)+";"+idToX(id2);
                switch(id2 - id1){
                  case 1:
                    filterEast.set(id1String.hashCode());
                    filterEast.set(hashAString(idToY(id1)+";"+idToX(id1)));
                    break;
                  case -1:
                    filterEast.set(id2String.hashCode());
                    filterEast.set(hashAString(idToY(id2)+";"+idToX(id2)));
                    break;
                  case -N:
                    filterSouth.set(id2String.hashCode());
                    filterSouth.set(hashAString(idToY(id2)+";"+idToX(id2)));
                    break;
                  case N:
                    filterSouth.set(id1String.hashCode());
                    filterSouth.set(hashAString(idToY(id1)+";"+idToX(id1)));
                    break;
                }
                break;
            }


            if (drawOnGraphicsScreen) {
                // erase the wall
                StdDraw.setPenColor(StdDraw.WHITE);
                int x1 = idToX(id1); int y1 = idToY(id1);
                int x2 = idToX(id2); int y2 = idToY(id2);
                if (x1 == x2) {
                    // vertical adjacency needs a horizontal line seg
                    int greaterY=Math.max(y1,y2);
                    StdDraw.line(x1*GRAPHICSSCALE+0.002, 1-greaterY*GRAPHICSSCALE,
                                 (x1+1)*GRAPHICSSCALE-0.002, 1-greaterY*GRAPHICSSCALE);
                } else {  // need a vertical line set
                    int greaterX=Math.max(x1,x2);
                    StdDraw.line(greaterX*GRAPHICSSCALE, 1-(y1*GRAPHICSSCALE+0.002),
                                 greaterX*GRAPHICSSCALE, 1-((y1+1)*GRAPHICSSCALE-0.002));

                }
            }
        }
        else {
            // nothing.  Even if there is a wall here, it does
            // nothing except make the maze harder.  So leave
            // the wall.
        }
    }

    void solveTheMaze(){
      StdDraw.setPenColor(StdDraw.RED);
      for (int i = path.nextSetBit(0); i >= 0; i = path.nextSetBit(i+1)) {
        int x = idToX(i);
        int y = idToY(i);
        StdDraw.filledCircle(x/40.0 + (0.0125), 1-y/40.0 - 0.0125,0.005);
      }
    }

    // for debugging,
    // show members of the connected component of (0,0)
    // with blanks, * for non-members
    // Note: this is not a visualization of the maze

    void print(int howMuch) {
        if (howMuch > N) howMuch = N;
        for (int y=0; y < howMuch; ++y) {
            for (int x=0; x < howMuch; ++x)
                if (uf.connected(xyToId(x,y), topLeft))
                    System.out.print(" ");
                else
                    System.out.print("*");
            System.out.println();
        }
    }


    void printAsciiGraphics() {
      System.out.println("The grid size is : " + N);
      String line1;
      String line2;
      switch(setKindCode){
        case twoDBooleanSet:
          line1 = "";
          line2 = "";
          for (int k = 0; k < N ; k++ ) {
            line1 += "---";
          }
          System.out.println(line1);
          for (int i = 0; i < N ; i ++ ) {
            line1 = "|";
            line2 = "|";
            for (int j = 0; j < N ; j++ ) {
              if (path.get(N*i + j) == true) {
                if (south[i][j] != true && east[i][j] != true) {
                  line1 += " .|";
                  line2 += "--+";
                }else if (south[i][j] == true && east[i][j] != true) {
                  if ( i == N ) {
                    line1 += " .|";
                    line2 += "  |";
                  }else {
                    if (south[i+1][j] == true) {
                      line1 += " .|";
                      line2 += "  |";
                    }else {
                      line1 += " .|";
                      line2 += "  +";
                    }
                  }
                }else if (south[i][j] != true && east[i][j] == true) {
                  if ( j == N) {
                    line1 += " . ";
                    line2 += "---";
                  }else{
                    if (east[i][j+1] == true) {
                      line1 += " . ";
                      line2 += "---";
                    }else{
                      line1 += " . ";
                      line2 += "--+";
                    }
                  }
                }else if(south[i][j] == true && east[i][j] == true){
                  line1 += " . ";
                  line2 += " . ";
                }
              }else {
                if (south[i][j] != true && east[i][j] != true) {
                  line1 += "  |";
                  line2 += "--+";
                }else if (south[i][j] == true && east[i][j] != true) {
                  if ( i == N ) {
                    line1 += "  |";
                    line2 += "  |";
                  }else {
                    if (south[i+1][j] == true) {
                      line1 += "  |";
                      line2 += "  |";
                    }else {
                      line1 += "  |";
                      line2 += "  +";
                    }
                  }
                }else if (south[i][j] != true && east[i][j] == true) {
                  if ( j == N) {
                    line1 += "   ";
                    line2 += "---";
                  }else{
                    if (east[i][j+1] == true) {
                      line1 += "   ";
                      line2 += "---";
                    }else{
                      line1 += "   ";
                      line2 += "--+";
                    }
                  }
                }else if(south[i][j] == true && east[i][j] == true){
                  line1 += "   ";
                  line2 += "   ";
                }
              }

            }
            System.out.println(line1);
            System.out.println(line2);
          }
          break;
        case bitSetSet:
            line1 = "";
            line2 = "";
            for (int k = 0; k < N ; k++ ) {
              line1 += "---";
            }
            System.out.println(line1);
            for (int i = 0; i < N ; i ++ ) {
              line1 = "|";
              line2 = "|";
              for (int j = 0; j < N ; j++ ) {
                if (path.get(N*i + j) == true) {
                  if (removedWallsBitSouth.get(N*i + j) != true && removedWallsBitEast.get(N*i + j) != true) {
                    line1 += " .|";
                    line2 += "--+";
                  }else if (removedWallsBitSouth.get(N*i + j) == true && removedWallsBitEast.get(N*i + j) != true) {
                    if ( i == N ) {
                      line1 += " .|";
                      line2 += "  |";
                    }else {
                      if (removedWallsBitSouth.get(N*(i+1) + j) == true) {
                        line1 += " .|";
                        line2 += "  |";
                      }else {
                        line1 += " .|";
                        line2 += "  +";
                      }
                    }
                  }else if (removedWallsBitSouth.get(N*i + j) != true && removedWallsBitEast.get(N*i + j) == true) {
                    if ( j == N) {
                      line1 += " . ";
                      line2 += "---";
                    }else{
                      if (removedWallsBitEast.get(N*i + j+1) == true) {
                        line1 += " . ";
                        line2 += "---";
                      }else{
                        line1 += " . ";
                        line2 += "--+";
                      }
                    }
                  }else if(removedWallsBitSouth.get(N*i + j) == true && removedWallsBitEast.get(N*i + j) == true){
                    line1 += " . ";
                    line2 += " . ";
                  }
                }else{
                  if (removedWallsBitSouth.get(N*i + j) != true && removedWallsBitEast.get(N*i + j) != true) {
                    line1 += "  |";
                    line2 += "--+";
                  }else if (removedWallsBitSouth.get(N*i + j) == true && removedWallsBitEast.get(N*i + j) != true) {
                    if ( i == N ) {
                      line1 += "  |";
                      line2 += "  |";
                    }else {
                      if (removedWallsBitSouth.get(N*(i+1) + j) == true) {
                        line1 += "  |";
                        line2 += "  |";
                      }else {
                        line1 += "  |";
                        line2 += "  +";
                      }
                    }
                  }else if (removedWallsBitSouth.get(N*i + j) != true && removedWallsBitEast.get(N*i + j) == true) {
                    if ( j == N) {
                      line1 += "   ";
                      line2 += "---";
                    }else{
                      if (removedWallsBitEast.get(N*i + j+1) == true) {
                        line1 += "   ";
                        line2 += "---";
                      }else{
                        line1 += "   ";
                        line2 += "--+";
                      }
                    }
                  }else if(removedWallsBitSouth.get(N*i + j) == true && removedWallsBitEast.get(N*i + j) == true){
                    line1 += "   ";
                    line2 += "   ";
                  }
                }
              }
              System.out.println(line1);
              System.out.println(line2);
            }
            break;
          case hashSetSet:
          line1 = "";
          line2 = "";
          for (int k = 0; k < N ; k++ ) {
            line1 += "---";
          }
          System.out.println(line1);
          for (int i = 0; i < N ; i ++ ) {
            line1 = "|";
            line2 = "|";
            for (int j = 0; j < N ; j++ ) {
              if (path.get(N*i + j) == true) {
                if (removedWallsSouth.contains(N*i + j) != true && removedWallsEast.contains(N*i + j) != true) {
                  line1 += " .|";
                  line2 += "--+";
                }else if (removedWallsSouth.contains(N*i + j) == true && removedWallsEast.contains(N*i + j) != true) {
                  if ( i == N ) {
                    line1 += " .|";
                    line2 += "  |";
                  }else {
                    if (removedWallsSouth.contains(N*(i+1) + j) == true) {
                      line1 += " .|";
                      line2 += "  |";
                    }else {
                      line1 += " .|";
                      line2 += "  +";
                    }
                  }
                }else if (removedWallsSouth.contains(N*i + j) != true && removedWallsEast.contains(N*i + j) == true) {
                  if ( j == N) {
                    line1 += " . ";
                    line2 += "---";
                  }else{
                    if (removedWallsEast.contains(N*i + j+1) == true) {
                      line1 += " . ";
                      line2 += "---";
                    }else{
                      line1 += " . ";
                      line2 += "--+";
                    }
                  }
                }else if(removedWallsSouth.contains(N*i + j) == true && removedWallsEast.contains(N*i + j) == true){
                  line1 += " . ";
                  line2 += "   ";
                }
              }else{
                if (removedWallsSouth.contains(N*i + j) != true && removedWallsEast.contains(N*i + j) != true) {
                  line1 += "  |";
                  line2 += "--+";
                }else if (removedWallsSouth.contains(N*i + j) == true && removedWallsEast.contains(N*i + j) != true) {
                  if ( i == N ) {
                    line1 += "  |";
                    line2 += "  |";
                  }else {
                    if (removedWallsSouth.contains(N*(i+1) + j) == true) {
                      line1 += "  |";
                      line2 += "  |";
                    }else {
                      line1 += "  |";
                      line2 += "  +";
                    }
                  }
                }else if (removedWallsSouth.contains(N*i + j) != true && removedWallsEast.contains(N*i + j) == true) {
                  if ( j == N) {
                    line1 += "   ";
                    line2 += "---";
                  }else{
                    if (removedWallsEast.contains(N*i + j+1) == true) {
                      line1 += "   ";
                      line2 += "---";
                    }else{
                      line1 += "   ";
                      line2 += "--+";
                    }
                  }
                }else if(removedWallsSouth.contains(N*i + j) == true && removedWallsEast.contains(N*i + j) == true){
                  line1 += "   ";
                  line2 += "   ";
                }
              }

            }
            System.out.println(line1);
            System.out.println(line2);
          }
          break;
        case bloomFilterSet:
          line1 = "";
          line2 = "";
          for (int k = 0; k < N ; k++ ) {
            line1 += "---";
          }
          System.out.println(line1);
          for (int i = 0; i < N ; i ++ ) {
            line1 = "|";
            line2 = "|";
            for (int j = 0; j < N ; j++ ) {
              String coord = i +";"+j;
              String coordi = (i+1) + ";" + j;
              String coordj = i + ";" + (j+1);
              if (path.get(N*i + j) == true) {
                if (filterSouth.get(coord.hashCode()) != true && filterSouth.get(hashAString(coord)) != true && filterEast.get(coord.hashCode()) != true && filterEast.get(hashAString(coord)) != true) {
                  line1 += " .|";
                  line2 += "--+";
                }else if (filterSouth.get(coord.hashCode()) == true && filterSouth.get(hashAString(coord)) == true && filterEast.get(coord.hashCode()) != true && filterEast.get(hashAString(coord)) != true) {
                  if ( i == N ) {
                    line1 += " .|";
                    line2 += " .|";
                  }else {
                    if (filterSouth.get(coordi.hashCode()) == true && filterSouth.get(hashAString(coordi)) == true) {
                      line1 += " .|";
                      line2 += "  |";
                    }else {
                      line1 += " .|";
                      line2 += "  +";
                    }
                  }
                }else if (filterSouth.get(coord.hashCode()) != true && filterSouth.get(hashAString(coord)) != true && filterEast.get(coord.hashCode()) == true && filterEast.get(hashAString(coord)) == true) {
                  if ( j == N) {
                    line1 += " . ";
                    line2 += "---";
                  }else{
                    if (filterEast.get(coordj.hashCode()) == true && filterEast.get(hashAString(coordj)) == true) {
                      line1 += " . ";
                      line2 += "---";
                    }else{
                      line1 += " . ";
                      line2 += "--+";
                    }
                  }
                }else if(filterSouth.get(coord.hashCode()) == true && filterSouth.get(hashAString(coord)) == true && filterEast.get(coord.hashCode()) == true && filterEast.get(hashAString(coord)) == true){
                  line1 += " . ";
                  line2 += "   ";
                }
              }else {
                if (filterSouth.get(coord.hashCode()) != true && filterSouth.get(hashAString(coord)) != true && filterEast.get(coord.hashCode()) != true && filterEast.get(hashAString(coord)) != true) {
                  line1 += "  |";
                  line2 += "--+";
                }else if (filterSouth.get(coord.hashCode()) == true && filterSouth.get(hashAString(coord)) == true && filterEast.get(coord.hashCode()) != true && filterEast.get(hashAString(coord)) != true) {
                  if ( i == N ) {
                    line1 += "  |";
                    line2 += "  |";
                  }else {
                    if (filterSouth.get(coordi.hashCode()) == true && filterSouth.get(hashAString(coordi)) == true) {
                      line1 += "  |";
                      line2 += "  |";
                    }else {
                      line1 += "  |";
                      line2 += "  +";
                    }
                  }
                }else if (filterSouth.get(coord.hashCode()) != true && filterSouth.get(hashAString(coord)) != true && filterEast.get(coord.hashCode()) == true && filterEast.get(hashAString(coord)) == true) {
                  if ( j == N) {
                    line1 += "   ";
                    line2 += "---";
                  }else{
                    if (filterEast.get(coordj.hashCode()) == true && filterEast.get(hashAString(coordj)) == true) {
                      line1 += "   ";
                      line2 += "---";
                    }else{
                      line1 += "   ";
                      line2 += "--+";
                    }
                  }
                }else if(filterSouth.get(coord.hashCode()) == true && filterSouth.get(hashAString(coord)) == true && filterEast.get(coord.hashCode()) == true && filterEast.get(hashAString(coord)) == true){
                  line1 += "   ";
                  line2 += "   ";
                }
              }
            }
            System.out.println(line1);
            System.out.println(line2);
          }
          break;
      }
    }

}

package demo01;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

//主类
public class Tetris extends JPanel {

    //声明正下落
    private Tetromino currentOne = Tetromino.randomOne();
    //将下落
    private Tetromino nextOne = Tetromino.randomOne();
    //游戏主区域
    private Cell[][] wall = new Cell[18][9];
    //单元格值
    private static final int CELL_SIZE = 48;
    //分数池
    private static int[] scores_pool = {0, 1, 2, 5, 10};
    //分数
    private static int totalScore = 0;
    //消除行数
    private static int totalLine = 0;

    //游戏状态
    public static final int PLAYING = 0;
    public static final int PAUSE = 1;
    public static final int GAMEOVER = 2;
    public static int game_state = 0;
    String[] show_state = {"P[pause]", "C[continue]", "S[replay]"};

    //载入方块图片
    public static BufferedImage I;
    public static BufferedImage J;
    public static BufferedImage L;
    public static BufferedImage O;
    public static BufferedImage S;
    public static BufferedImage T;
    public static BufferedImage Z;
    public static BufferedImage backImage;

    static {
        try {
            I = ImageIO.read(new File("images/I.png"));
            J = ImageIO.read(new File("images/J.png"));
            L = ImageIO.read(new File("images/L.png"));
            O = ImageIO.read(new File("images/O.png"));
            S = ImageIO.read(new File("images/S.png"));
            T = ImageIO.read(new File("images/T.png"));
            Z = ImageIO.read(new File("images/Z.png"));
            backImage = ImageIO.read(new File("images/background.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(backImage, 0, 0, null);

        //平移坐标轴
        g.translate(22, 15);
        //绘制游戏主区域
        paintWall(g);
        //绘制正下落的方格
        paintCurrentOne(g);
        //绘制下一个方格
        paintNextOne(g);
        //绘制得分
        paintScore(g);
        //绘制游戏状态
        paintState(g);
    }

    public void start() {
        game_state = PLAYING;
        KeyListener l = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                switch (code) {
                    case KeyEvent.VK_DOWN:
                        sortDropAction();
                        repaint();
                        break;
                    case KeyEvent.VK_LEFT:
                        moveLeftAction();
                        repaint();
                        break;
                    case KeyEvent.VK_RIGHT:
                        moveRightAction();
                        repaint();
                        break;
                    case KeyEvent.VK_UP:
                        rotateRight();
                        repaint();
                        break;
                    case KeyEvent.VK_SPACE:
                        handDropAction();
                        repaint();
                        break;
                    case KeyEvent.VK_P:
                        if (game_state == PLAYING) {
                            game_state = PAUSE;
                        }
                        break;
                    case KeyEvent.VK_C:
                        if (game_state == PAUSE) {
                            game_state = PLAYING;
                        }
                        break;
                    case KeyEvent.VK_S:
                        game_state = PLAYING;
                        wall = new Cell[18][9];
                        currentOne = Tetromino.randomOne();
                        nextOne = Tetromino.randomOne();
                        totalLine = 0;
                        totalScore = 0;
                        break;

                }
            }
        };
        //将窗口设为焦点
        this.addKeyListener(l);
        this.requestFocus();

        while (true) {
            //判断游戏状态
            if (game_state == PLAYING) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //判断能否下落
                if (canDrop()) {
                    currentOne.moveDrop();
                } else {
                    landToWall();
                    destroyLine();
                    if (isGameOver()) {
                        game_state = GAMEOVER;
                    } else {
                        currentOne = nextOne;
                        nextOne = Tetromino.randomOne();
                    }
                }
            }
            repaint();
        }
    }

    public void paintWall(Graphics g) {
        for (int i = 0; i < wall.length; i++) {
            for (int i1 = 0; i1 < wall[i].length; i1++) {
                int x = i1 * CELL_SIZE;
                int y = i * CELL_SIZE;
                Cell cell = wall[i][i1];
                //判断当前单元格是否有方块，没有就绘制矩形，否则小方块放入
                if (cell == null) {
                    g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                } else {
                    g.drawImage(cell.getImage(), x, y, null);
                }
            }
        }
    }

    public void paintCurrentOne(Graphics g) {
        Cell[] cells = currentOne.cells;
        for (Cell cell : cells) {
            int x = cell.getCol() * CELL_SIZE;
            int y = cell.getRow() * CELL_SIZE;
            g.drawImage(cell.getImage(), x, y, null);
        }
    }

    public void paintNextOne(Graphics g) {
        Cell[] cells = nextOne.cells;
        for (Cell cell : cells) {
            int x = cell.getCol() * CELL_SIZE + 370;
            int y = cell.getRow() * CELL_SIZE + 25;
            g.drawImage(cell.getImage(), x, y, null);

        }
    }

    public void paintScore(Graphics g) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        g.drawString("分数：" + totalScore, 500, 240);
        g.drawString("行数：" + totalScore, 500, 430);
    }

    public void paintState(Graphics g) {
        int x = 500;
        int y = 660;
        if (game_state == PLAYING) {
            g.drawString(show_state[0], x, y);
        } else if (game_state == PAUSE) {
            g.drawString(show_state[1], x, y);
        } else if (game_state == GAMEOVER) {
            g.drawString(show_state[2], x, y);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 60));
            g.setColor(Color.red);
            g.drawString("GAME OVER", 30, 400);
        }
    }

    //判断是否出界
    public boolean outOfRange() {
        Cell[] cells = currentOne.cells;
        for (Cell cell : cells) {
            int col = cell.getCol();
            int row = cell.getRow();
            if (row < 0 || row > wall.length - 1 || col < 0 || col > wall[0].length - 1) {
                return true;
            }
        }
        return false;
    }

    //判断是否重合
    public boolean coincide() {
        Cell[] cells = currentOne.cells;
        for (Cell cell : cells) {
            int col = cell.getCol();
            int row = cell.getRow();
            if (wall[row][col] != null) {
                return true;
            }
        }
        return false;
    }

    //按键移动
    public void moveLeftAction() {
        currentOne.moveLeft();
        if (outOfRange() || coincide()) {
            currentOne.moveRight();
        }
    }

    public void moveRightAction() {
        currentOne.moveRight();
        if (outOfRange() || coincide()) {
            currentOne.moveLeft();
        }
    }

    //判断当前行是否满
    public boolean isFullLine(int row) {
        Cell[] cells = wall[row];
        for (Cell cell : cells) {
            if (cell == null) {
                return false;
            }
        }
        return true;
    }

    //判断四方格是否能下落
    public boolean canDrop() {
        Cell[] cells = currentOne.cells;
        for (Cell cell : cells) {
            int row = cell.getRow();
            int col = cell.getCol();
            if (row == wall.length - 1) {
                return false;
            } else if (wall[row + 1][col] != null) {
                return false;
            }
        }
        return true;
    }

    //按键一次四方格下落一格
    public void sortDropAction() {
        if (canDrop()) {
            currentOne.moveDrop();
        } else {
            landToWall();
            destroyLine();
            if (isGameOver()) {
                game_state = GAMEOVER;
            } else {
                currentOne = nextOne;
                nextOne = Tetromino.randomOne();
            }
        }
    }

    //瞬间下落
    public void handDropAction() {
        while (canDrop()) {
            currentOne.moveDrop();
        }
        landToWall();
        destroyLine();
        if (isGameOver()) {
            game_state = GAMEOVER;
        } else {
            currentOne = nextOne;
            nextOne = Tetromino.randomOne();
        }
    }

    //嵌入地图
    private void landToWall() {
        Cell[] cells = currentOne.cells;
        for (Cell cell : cells) {
            int row = cell.getRow();
            int col = cell.getCol();
            wall[row][col] = cell;
        }
    }

    //消除行
    public void destroyLine() {
        //统计哦当前消除行数
        int line = 0;
        Cell[] cells = currentOne.cells;
        for (Cell cell : cells) {
            int row = cell.getRow();
            if (isFullLine(row)) {
                line++;
                for (int i = row; i > 0; i--) {
                    System.arraycopy(wall[i - 1], 0, wall[i], 0, wall[0].length);
                }
                wall[0] = new Cell[9];
            }
        }
        totalScore += scores_pool[line];
        totalLine += line;
    }

    //是否结束
    public boolean isGameOver() {
        Cell[] cells = nextOne.cells;
        for (Cell cell : cells) {
            if (wall[cell.getRow()][cell.getCol()] != null) {
                return true;
            }
        }
        return false;
    }

    //创建顺时针旋转
    public void rotateRight() {
        currentOne.rotateRight();
        if (outOfRange() || coincide()) {
            currentOne.rotateLeft();
        }
    }

    public static void main(String[] args) {
        //窗口
        JFrame frame = new JFrame("俄罗斯方块");

        //创建游戏界面
        Tetris panel = new Tetris();
        //嵌入窗口
        frame.add(panel);

        //设置可见
        frame.setVisible(true);
        //设置窗口尺寸
        frame.setSize(810, 940);
        //设置窗口居中
        frame.setLocationRelativeTo(null);
        //设置窗口关闭程序关闭
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //游戏主要逻辑封装
        panel.start();

    }

}


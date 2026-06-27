import java.awt.*; 
import java.awt.event.*; 
import java.awt.image.BufferedImage; 
import java.io.File; 
import java.util.ArrayList; 
import java.util.Iterator; 
import java.util.Random; 
import javax.imageio.ImageIO; 
import javax.swing.*; 

public class lolproject extends JPanel implements Runnable, KeyListener, MouseListener {
 
    public static final int WIDTH = 1900; 
    public static final int HEIGHT = 1000;
    private int gameState = 0; 
    private BufferedImage bgImg, heroImg, ballImg, ghostImg, flashImg;
    private boolean isRunning = true;  
    private Hero player;
    private ArrayList<Bullet> bullets;
    private int score = 0; 
    private int frameScoreCounter = 0; 
    private int frameCount = 0; 
    private int spawnRate = 50; 
    private double bulletSpeed = 7.0; 
    private int nextDifficultyScore = 20; 
    private long lastGhostTime = -10000;
    private long lastFlashTime = -10000;
    private boolean isGhostActive = false; 
    private final int GHOST_CD = 10000;   
    private final int GHOST_DUR = 3000;   
    private final int FLASH_CD = 8000;
    
    public lolproject() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT)); 
        setFocusable(true); 
        addKeyListener(this); 
        enableInputMethods(false);
        addMouseListener(this);
        loadResources();
    }

    private void loadResources() {
        try {
            
            bgImg = ImageIO.read(new File("assets/map.jpg"));
            heroImg = ImageIO.read(new File("assets/hero.png"));
            ballImg = ImageIO.read(new File("assets/fireball.png"));
            ghostImg = ImageIO.read(new File("assets/ghost.png")); 
            flashImg = ImageIO.read(new File("assets/flash.png"));
        } catch (Exception e) {
            
            System.err.println("資源載入失敗");
        }
    }

    public void startGame() {
        player = new Hero(WIDTH / 2, HEIGHT / 2); 
        bullets = new ArrayList<>();
        score = 0;
        frameScoreCounter = 0;
        spawnRate = 50;
        bulletSpeed = 7.0;
        frameCount = 0;
        nextDifficultyScore = 20;

        lastGhostTime = 10000000 - GHOST_CD;
        lastFlashTime = 10000000 - FLASH_CD;
        isGhostActive = false;

        gameState = 1;
    }

    public void run() {
        while (isRunning) {

            if (gameState == 1) 
            	update();
            
            repaint();
            
            try { Thread.sleep(16); } catch (Exception e) {}
        }
    }

    private void update() {
        frameScoreCounter++;
        if (frameScoreCounter >= 10) {
            score++;
            frameScoreCounter = 0;
        }

        isGhostActive = (System.currentTimeMillis() - lastGhostTime < GHOST_DUR);
        
        player.update();

        double calculatedSpeed = 7.0 + (score / 30.0) * 0.2;
        bulletSpeed = Math.min(calculatedSpeed, 8.0);

        if (score > 0 && score % 30 == 0 && frameScoreCounter == 0) {
            if (spawnRate > 38) {
                spawnRate -= 2;
            }
        }

        frameCount++;
        if (frameCount >= spawnRate) {
            int bulletsToSpawn = 1 + (score / 60);
            if (bulletsToSpawn > 3) bulletsToSpawn = 3; 

            for (int i = 0; i < bulletsToSpawn; i++) {
                spawnBullet();
            }
            frameCount = 0;
        }

        Iterator<Bullet> it = bullets.iterator(); 
        while (it.hasNext()) {
            Bullet b = it.next();
            b.x += b.dx; 
            b.y += b.dy; 


            if (!isGhostActive) {

                double dist = Math.hypot(player.x - b.x, player.y - b.y);
                if (dist < 45) {
                    gameState = 2; 
                }
            }


            if (b.x < -150 || b.x > WIDTH + 150 || b.y < -150 || b.y > HEIGHT + 150) {
                it.remove();
            }
        }
    }

    protected void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        if (bgImg != null) g2d.drawImage(bgImg, 0, 0, WIDTH, HEIGHT, null);


        if (gameState == 0) drawMenu(g2d); 
        else if (gameState == 3) drawInstructions(g2d); 
        else {

            player.draw(g2d);
            for (Bullet b : bullets) b.draw(g2d);
            drawUI(g2d);
            drawSkills(g2d);
        }
    }


    private void drawMenu(Graphics2D g) {
        g.setColor(new Color(0,0,0,150)); 
        g.fillRect(0,0,WIDTH,HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 80)); 
        g.drawString("極限走位", WIDTH/2 - 160, 250);

        g.setColor(new Color(70,130,180));
        g.fillRoundRect(WIDTH/2 - 100, HEIGHT/2 - 60, 200, 60, 15, 15);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
        g.drawString("開始遊戲", WIDTH/2 - 50, HEIGHT/2 - 22);

        g.setColor(new Color(100,149,237));
        g.fillRoundRect(WIDTH/2 - 100, HEIGHT/2 + 30, 200, 60, 15, 15);
        g.setColor(Color.WHITE);
        g.drawString("遊戲說明", WIDTH/2 - 50, HEIGHT/2 + 68);
    }

    private void drawInstructions(Graphics2D g) {
        g.setColor(new Color(0,0,0,200));
        g.fillRect(0,0,WIDTH,HEIGHT);
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 50));
        g.drawString("遊戲說明", WIDTH/2 - 100, 200);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 28));
        int startY = 320;
        g.drawString("● 移動：點擊滑鼠右鍵英雄會往該處移動。", WIDTH/2 - 300, startY);
        g.drawString("● [D] 鬼步：增加移動速度並免疫攻擊。", WIDTH/2 - 300, startY+180);
        g.drawString("● [F] 閃現：瞬間往目標方向傳送一小段距離。", WIDTH/2 - 300, startY + 360);

        g.setColor(Color.CYAN);
        g.drawString("點擊任何地方回到主選單", WIDTH/2 - 150, HEIGHT - 200);
    }


    private void drawUI(Graphics2D g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("Score: " + score, 30, 50);


        if (gameState == 2) {
            g.setColor(new Color(0,0,0,200));
            g.fillRect(0,0,WIDTH,HEIGHT);
            g.setColor(Color.RED);
            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 100));
            g.drawString("戰敗", WIDTH/2 - 100, HEIGHT/2 - 50);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 30));
            g.drawString("最終得分: " + score, WIDTH/2 - 100, HEIGHT/2 + 50);
            g.drawString("按下 [空白鍵] 回到主選單", WIDTH/2 - 200, HEIGHT/2 + 120);
        }
    }


    private void drawSkills(Graphics2D g) {
        long now = System.currentTimeMillis();

        drawSkillIcon(g, WIDTH/2 - 60, HEIGHT - 100, now - lastGhostTime, ghostImg, isGhostActive, GHOST_CD);
        drawSkillIcon(g, WIDTH/2 + 20, HEIGHT - 100, now - lastFlashTime, flashImg, false, FLASH_CD);
    }

    // 畫單個技能圖示的通用方法
    private void drawSkillIcon(Graphics2D g, int x, int y, long elapsed, BufferedImage img, boolean active, int cooldown) {
        g.drawImage(img, x, y, 50, 50, null);
        // 如果還在冷卻中 (elapsed < cooldown)
        if (elapsed < cooldown) {
            g.setColor(new Color(0,0,0,180)); // 半透明黑
            // 計算冷卻遮罩的高度比例
            int h = (int)(50 * (1.0 - elapsed / (double)cooldown));
            g.fillRect(x, y, 50, h);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
            // 顯示剩餘秒數
            g.drawString(String.format("%.1f", (cooldown - elapsed) / 1000.0), x+8, y+30);
        }
        // 如果技能啟動中，框框變青色，否則白色
        g.setColor(active ? Color.CYAN : Color.WHITE);
        g.drawRect(x, y, 50, 50);
    }

    // --- 生成子彈 ---
    private void spawnBullet() {
        Random r = new Random();
        double sx=0, sy=0;
        // 隨機決定子彈從哪一個邊緣出現 (上、右、下、左)
        switch (r.nextInt(4)) {
            case 0: sx=r.nextInt(WIDTH); sy=-80; break; // 上邊界外
            case 1: sx=WIDTH+80; sy=r.nextInt(HEIGHT); break; // 右邊界外
            case 2: sx=r.nextInt(WIDTH); sy=HEIGHT+80; break; // 下邊界外
            case 3: sx=-80; sy=r.nextInt(HEIGHT); // 左邊界外
        }
        // 計算子彈射向玩家的角度 (Math.atan2 計算兩點間的弧度)
        double angle = Math.atan2(player.y - sy, player.x - sx);
        bullets.add(new Bullet(sx, sy, angle, bulletSpeed));
    }

    // --- 滑鼠點擊事件 ---
    @Override public void mousePressed(MouseEvent e) {
        if (gameState == 0) {
            // 檢查是否點擊到「開始遊戲」按鈕範圍
            if (e.getX() >= WIDTH/2 - 100 && e.getX() <= WIDTH/2 + 100 &&
                e.getY() >= HEIGHT/2 - 60 && e.getY() <= HEIGHT/2) {
                startGame();
            }
            // 檢查是否點擊到「遊戲說明」
            else if (e.getX() >= WIDTH/2 - 100 && e.getX() <= WIDTH/2 + 100 &&
                     e.getY() >= HEIGHT/2 + 30 && e.getY() <= HEIGHT/2 + 90) {
                gameState = 3;
            }
        }
        else if (gameState == 3) {
            gameState = 0; // 說明畫面點任意處回主選單
        }
        // 遊戲中按右鍵：設定玩家移動目標
        else if (gameState == 1 && SwingUtilities.isRightMouseButton(e)) {
            player.targetX=e.getX();
            player.targetY=e.getY();
            player.isMoving=true;
        }
    }

    // --- 鍵盤按下事件 ---
    @Override public void keyPressed(KeyEvent e) {
        // 戰敗時按空白鍵重置
        if (gameState==2 && e.getKeyCode()==KeyEvent.VK_SPACE) gameState=0;
        
        if (gameState==1) {
            long now=System.currentTimeMillis();
            // 按 D 開鬼步
            if (e.getKeyCode()==KeyEvent.VK_D && now-lastGhostTime > GHOST_CD) {
                lastGhostTime=now;
            }
            // 按 F 閃現
            if (e.getKeyCode()==KeyEvent.VK_F && now-lastFlashTime > FLASH_CD) {
                // 計算向量 (dx, dy)
                double dx=player.targetX-player.x, dy=player.targetY-player.y;
                double d=Math.hypot(dx,dy);
                // 向量正規化並乘以距離 (350) 
                if (d>1){player.x+=(dx/d)*350; player.y+=(dy/d)*350;}
                player.isMoving=false; // 閃現後停止移動
                lastFlashTime=now; // 更新冷卻時間
            }
        }
    }

    // --- 內部類別：英雄 ---
    class Hero {
        double x,y,targetX,targetY; // 位置與移動目標
        boolean isMoving=false; // 是否正在移動中

        Hero(double x,double y){
            this.x=x;
            this.y=y;
            targetX=x;
            targetY=y;
        }

        void update(){
            if(!isMoving)return;
            // 如果開了鬼步，速度變快 (9.5)，否則 (6.5)
            double speed=isGhostActive?9.5:6.5;
            double dx=targetX-x, dy=targetY-y;
            double d=Math.hypot(dx,dy); // 計算與目標點的距離

            // 如果距離小於一步的速度，直接瞬移到目標點 (避免抖動)
            if(d<speed){
                x=targetX;
                y=targetY;
                isMoving=false;
            } else {
                // 否則，依照比例移動一步
                x+=(dx/d)*speed;
                y+=(dy/d)*speed;
            }
        }

        void draw(Graphics2D g){
            // 如果開了鬼步，畫一個藍色光圈
            if (isGhostActive) {
                g.setColor(new Color(0, 191, 255, 100));
                g.fillOval((int)x-65, (int)y-65, 130, 130);
                g.setColor(new Color(135, 206, 250, 150));
                g.fillOval((int)x-40, (int)y-40, 80, 80);
            }
            // 畫英雄圖片 (置中繪製，所以減去寬高的一半)
            g.drawImage(heroImg,(int)x-50,(int)y-50,100,100,null);

            // 如果正在移動，畫出目標點的小圈圈
            if(isMoving){
                g.setColor(Color.CYAN);
                g.drawOval((int)targetX-10,(int)targetY-5,20,10);
            }
        }
    }

    // --- 內部類別：子彈 ---
    class Bullet {
        double x,y,dx,dy,angle;
        Bullet(double x,double y,double angle,double speed){
            this.x=x;
            this.y=y;
            this.angle=angle;
            // 利用三角函數計算 X 和 Y 軸的分量速度
            dx=Math.cos(angle)*speed;
            dy=Math.sin(angle)*speed;
        }
        void draw(Graphics2D g){
            // 保存目前的畫布狀態 (平移旋轉前)
            java.awt.geom.AffineTransform old = g.getTransform();
            g.translate(x,y); // 將畫布原點移動到子彈位置
            g.rotate(angle-Math.toRadians(45)); // 旋轉畫布以配合子彈飛行方向
            g.drawImage(ballImg,-32,-32,64,64,null);
            g.setTransform(old); // 還原畫布狀態，以免影響下一個物件繪製
        }
    }

    // --- 程式進入點 ---
    public static void main(String[] args) {
        JFrame f=new JFrame("LoL Pro Dodge"); // 建立視窗
        lolproject g=new lolproject(); // 建立遊戲面板
        f.add(g); // 把面板放入視窗
        f.pack(); // 自動調整視窗大小以符合面板
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 設定關閉視窗即結束程式
        f.setLocationRelativeTo(null); // 視窗置中
        f.setVisible(true); // 顯示視窗
        new Thread(g).start(); // 啟動遊戲迴圈執行緒
    }

    // --- 介面規定必須實作但沒用到的方法 (留空即可) ---
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}
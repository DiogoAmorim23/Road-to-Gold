import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.awt.FontFormatException;

public final class GamePanel extends JPanel implements MouseListener, MouseMotionListener {
    private enum Screen { MENU, SELECT_1V1, SELECT_SIMULATION, MATCH, PREDICTION }
    private static final int W = 1920, H = 1080;
    private final Map<String, BufferedImage> images = new HashMap<>();
    private Font regularFont = new Font("SansSerif", Font.PLAIN, 12);
    private Font boldFont = new Font("SansSerif", Font.BOLD, 12);
    private Screen screen = Screen.MENU;
    private int homeIndex = 0, awayIndex = 1, selectedCard = 0, menuHover = -1;
    private String hoveredButton = "";
    private Team home, away;
    private int minute, homeGoals, awayGoals;
    private int[] selectedPlayer = {-1, -1}, benchIndex = {0, 0}, substitutions = {0, 0};
    private boolean substitutesOpen;
    private final java.util.List<String> commentary = new ArrayList<>();
    private boolean running;
    private boolean matchFinished;
    private boolean matchStarted;
    private final javax.swing.Timer timer;
    private final JTextField[] customNames = {new JTextField("A minha equipa"), new JTextField("A minha equipa")};
    private final JSpinner[] customStrengths = {new JSpinner(new SpinnerNumberModel(70, 40, 99, 1)), new JSpinner(new SpinnerNumberModel(70, 40, 99, 1))};
    private MatchMath.Prediction prediction;

    public GamePanel() {
        setPreferredSize(new Dimension(1280, 720));
        setBackground(new Color(6, 26, 56));
        setFocusable(true);
        addMouseListener(this); addMouseMotionListener(this);
        loadFonts();
        for (int i=0;i<2;i++) {
            ((AbstractDocument)customNames[i].getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override public void insertString(FilterBypass fb,int offset,String string,AttributeSet attr)throws BadLocationException { if(string!=null && fb.getDocument().getLength()+string.length()<=16) super.insertString(fb,offset,string,attr); }
                @Override public void replace(FilterBypass fb,int offset,int length,String string,AttributeSet attrs)throws BadLocationException { if(string!=null && fb.getDocument().getLength()-length+string.length()<=16) super.replace(fb,offset,length,string,attrs); }
            });
            customNames[i].setFont(regularFont.deriveFont(16f));
            customStrengths[i].setFont(regularFont.deriveFont(16f));
            add(customNames[i]); add(customStrengths[i]); customNames[i].setVisible(false); customStrengths[i].setVisible(false);
        }
        for (String path : new String[]{"backgrounds/menu.png", "backgrounds/1v1.png", "backgrounds/simulator.png", "assets/kicking_ball.png", "assets/game_logo.png"}) load(path);
        for (TeamCatalog.TeamInfo team : TeamCatalog.teams()) load(team.logo());
        timer = new javax.swing.Timer(350, e -> advanceMinute());
    }

    /** Carrega a fonte regular e a bold guardadas na pasta font do projeto. */
    private void loadFonts() {
        try {
            Path regularPath = Path.of("font", "GlacialIndifference-Regular.otf");
            Path boldPath = Path.of("font", "GlacialIndifference-Bold.otf");
            if (!regularPath.toFile().isFile()) regularPath = Path.of("..", "font", "GlacialIndifference-Regular.otf");
            if (!boldPath.toFile().isFile()) boldPath = Path.of("..", "font", "GlacialIndifference-Bold.otf");
            regularFont = Font.createFont(Font.TRUETYPE_FONT, regularPath.toFile());
            boldFont = Font.createFont(Font.TRUETYPE_FONT, boldPath.toFile());
        } catch (IOException | FontFormatException ex) {
            System.err.println("Não foi possível carregar a fonte Glacial Indifference: " + ex.getMessage());
        }
    }

    private void load(String path) {
        try {
            Path local = Path.of(path);
            if (!local.toFile().isFile()) local = Path.of("..", path);
            images.put(path, ImageIO.read(local.toFile()));
        } catch (IOException ignored) { }
    }
    private double sx() { return getWidth() / (double) W; }
    private double sy() { return getHeight() / (double) H; }

    @Override protected void paintComponent(Graphics raw) {
        super.paintComponent(raw); Graphics2D g = (Graphics2D) raw.create();
        g.scale(sx(), sy()); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        switch (screen) { case MENU -> drawMenu(g); case SELECT_1V1, SELECT_SIMULATION -> drawSelection(g); case MATCH -> drawMatch(g); case PREDICTION -> drawPrediction(g); }
        layoutCustomFields();
        g.dispose();
    }

    private void layoutCustomFields() {
        boolean selection = screen == Screen.SELECT_1V1 || screen == Screen.SELECT_SIMULATION;
        int[] indices = {homeIndex, awayIndex};
        int[] lefts = {135,1114};
        for (int i=0;i<2;i++) {
            boolean show = selection && indices[i] == TeamCatalog.teams().size()-1;
            customNames[i].setVisible(show); customStrengths[i].setVisible(show);
            if (show) {
                int x=(int)((lefts[i]+211)*sx()), y=(int)(432*sy());
                customNames[i].setBounds(x,y,(int)(250*sx()),(int)(40*sy()));
                customStrengths[i].setBounds((int)((lefts[i]+390)*sx()),(int)(824*sy()),(int)(92*sx()),(int)(38*sy()));
            }
        }
    }

    private void background(Graphics2D g, String path) { BufferedImage i=images.get(path); if(i!=null)g.drawImage(i,0,0,W,H,null); else {g.setColor(new Color(6,26,56));g.fillRect(0,0,W,H);} }
    private void rect(Graphics2D g, int x, int y, int width, int height, Color color) { g.setColor(color); g.fillRect(x,y,width,height); }
    private void text(Graphics2D g, String text, int x, int y, int size, Color color, boolean bold) { g.setColor(color); g.setFont((bold?boldFont:regularFont).deriveFont((float)size)); g.drawString(text,x,y); }
    private void center(Graphics2D g, String text, int x, int y, int size, Color color, boolean bold) { g.setFont((bold?boldFont:regularFont).deriveFont((float)size)); g.setColor(color); g.drawString(text,x-g.getFontMetrics().stringWidth(text)/2,y); }
    private void image(Graphics2D g, String path, int x, int y, int maxW, int maxH) { BufferedImage i=images.get(path); if(i==null)return; double s=Math.min(maxW/(double)i.getWidth(),maxH/(double)i.getHeight()); int w=(int)(i.getWidth()*s),h=(int)(i.getHeight()*s);g.drawImage(i,x-w/2,y-h/2,w,h,null); }

    private void drawMenu(Graphics2D g) {
        background(g,"backgrounds/menu.png");
        image(g,"assets/game_logo.png",190,185,340,310);
        String[] options={"JOGAR MODO 1V1","JOGAR MODO SIMULAÇÃO","JOGAR MODO LIGA","DEFINIÇÕES","SAIR"};
        for(int i=0;i<options.length;i++){int y=490+i*101;if(menuHover==i)drawArrow(g,15,y-44,52,Color.WHITE,true);text(g,options[i],76,490+i*101,56,Color.WHITE,menuHover==i||i==4&&"menu-exit".equals(hoveredButton));}
        text(g,"Diogo Amorim",14,1060,27,Color.WHITE,false);
    }

    private void drawSelection(Graphics2D g) {
        boolean sim=screen==Screen.SELECT_SIMULATION; background(g,sim?"backgrounds/simulator.png":"backgrounds/1v1.png");
        text(g,sim?"MODO SIMULAÇÃO RÁPIDA":"MODO 1V1",110,132,74,Color.WHITE,true);
        text(g,sim?"Escolhe qualquer equipa e descobre quais os resultados mais prováveis.":"Escolhe as equipas e acompanha a partida minuto a minuto.",110,194,39,Color.WHITE,false);
        drawTeamCard(g,135,267,"Casa",homeIndex,0); drawTeamCard(g,1114,267,"Fora",awayIndex,1);
        if(!sim){int selectedX=selectedCard==0?135:1114;rect(g,selectedX-10,902,692,70,new Color(8,124,112));center(g,"SELECIONADO",selectedX+336,948,28,Color.WHITE,true);}
        boolean backHover="selection-back".equals(hoveredButton);
        boolean actionHover="selection-action".equals(hoveredButton);
        text(g,"Voltar",45,1034,42,Color.WHITE,backHover); text(g,sim?"Simular":"Jogar",1665,1013,38,Color.WHITE,actionHover); drawArrow(g,1810,951,84,Color.WHITE,true);
    }

    private void drawArrow(Graphics2D g,int x,int y,int size,Color color,boolean right){g.setColor(color);Polygon p=new Polygon();if(right){p.addPoint(x,y);p.addPoint(x+size,y+size/2);p.addPoint(x,y+size);}else{p.addPoint(x+size,y);p.addPoint(x,y+size/2);p.addPoint(x+size,y+size);}g.fillPolygon(p);}
    private void drawTeamCard(Graphics2D g,int x,int y,String title,int index,int side){boolean selected=screen==Screen.SELECT_1V1&&selectedCard==side;rect(g,x-10,y-10,692,655,selected?new Color(8,124,112):new Color(6,26,56));rect(g,x,y,672,635,Color.WHITE);rect(g,x+191,y-38,290,74,new Color(201,211,220));center(g,title,x+336,y+12,42,Color.BLACK,true);TeamCatalog.TeamInfo team=TeamCatalog.teams().get(index);boolean custom=index==TeamCatalog.teams().size()-1;String shown=custom?customNames[side].getText().trim():team.name();int font=shown.length()>15?51:68;center(g,shown,x+336,y+(custom?119:140),font,Color.BLACK,true);image(g,team.logo(),x+336,y+(custom?345:380),custom?210:280,custom?230:310);if(custom){text(g,"Força:",x+220,y+580,53,Color.BLACK,true);}else{center(g,"Força: "+team.strength(),x+336,y+580,53,Color.BLACK,true);}drawArrow(g,x+40,y+360,78,new Color(201,211,220),false);drawArrow(g,x+555,y+360,78,new Color(201,211,220),true);}

    private void drawMatch(Graphics2D g) {
        background(g,"backgrounds/1v1.png");
        boolean exitHover="match-exit".equals(hoveredButton);
        center(g,"Sair",193,119,39,Color.WHITE,exitHover);
        rect(g,538,37,843,149,Color.WHITE); image(g,home.logoPath(),670,86,80,80);image(g,away.logoPath(),1250,86,80,80);
        center(g,home.name(),670,164,30,Color.BLACK,false);center(g,away.name(),1250,164,30,Color.BLACK,false);center(g,homeGoals+" - "+awayGoals,960,126,58,Color.BLACK,true);center(g,String.format("%02d'",minute),960,165,25,Color.BLACK,false);
        rect(g,85,219,1750,529,new Color(11,125,45));g.setColor(new Color(0,27,18));g.setStroke(new BasicStroke(8));g.drawRect(85,219,1750,529);
        drawSquad(g,150,233,home,0); drawSquad(g,1057,233,away,1);
        rect(g,0,758,1515,322,new Color(29,43,54));text(g,"Relato da partida",18,793,21,Color.WHITE,true);rect(g,18,808,1487,252,new Color(20,33,43));int first=Math.max(0,commentary.size()-8);for(int i=first;i<commentary.size();i++)text(g,commentary.get(i),18,832+(i-first)*27,18,Color.WHITE,false);
        boolean playHover="match-play".equals(hoveredButton)&&!matchFinished;
        rect(g,1570,764,258,181,matchFinished||playHover?new Color(190,190,190):Color.WHITE);
        if(matchFinished||playHover) imageDisabled(g,"assets/kicking_ball.png",1680,854,180,150);
        else image(g,"assets/kicking_ball.png",1680,854,180,150);
        drawPlayControl(g,1780,820,running,matchFinished,"match-play".equals(hoveredButton));
        if(substitutesOpen) drawSubstituteMenu(g);
    }

    private void drawSquad(Graphics2D g,int x,int y,Team team,int side){
        text(g,"Jogador",x+28,y+31,31,Color.WHITE,true);text(g,"Pos.",x+215,y+31,31,Color.WHITE,true);text(g,"Energia",x+355,y+31,31,Color.WHITE,true);text(g,"Qualidade",x+520,y+31,31,Color.WHITE,true);rect(g,x,y+49,713,4,Color.WHITE);
        for(int i=0;i<team.starters().size();i++){Player p=team.starters().get(i);int yy=y+81+i*34;if(selectedPlayer[side]==i)rect(g,x+4,yy-20,705,33,new Color(7,102,36));text(g,p.name(),x+5,yy,24,Color.WHITE,false);text(g,p.position(),x+237,yy,22,Color.WHITE,false);rect(g,x+325,yy-14,103,28,Color.WHITE);Color energyColor=p.energy()>=70?new Color(80,215,109):(p.energy()>=30?new Color(255,210,55):new Color(225,55,55));rect(g,x+325,yy-14,(int)(103*p.energy()/100.0),28,energyColor);text(g,p.energy()+"%",x+437,yy,21,Color.WHITE,false);text(g,String.valueOf(p.quality()),x+583,yy,22,Color.WHITE,false);}
        int yy=y+458;boolean canManage=(side==selectedCard);Color controlBg=canManage?Color.WHITE:new Color(195,195,195);rect(g,x,yy,436,44,controlBg);Player b=team.substitutes().get(benchIndex[side]);text(g,b.name()+" - "+b.position()+" - "+b.energy()+"%",x+5,yy+28,20,Color.BLACK,false);if(canManage)drawDownArrow(g,x+398,yy+10,22,Color.BLACK);rect(g,x+444,yy,236,44,controlBg);center(g,"Substituir selecionado",x+562,yy+28,18,canManage?Color.BLACK:Color.GRAY,false);
    }

    private void drawPlayControl(Graphics2D g,int cx,int top,boolean paused,boolean disabled,boolean hovered){g.setColor(disabled?Color.GRAY:Color.BLACK);if(paused){g.fillRect(cx-10,top,7,34);g.fillRect(cx+4,top,7,34);}else{Polygon p=new Polygon(new int[]{cx-8,cx+20,cx-8},new int[]{top,top+17,top+34},3);g.fillPolygon(p);}}
    private void drawDownArrow(Graphics2D g,int x,int y,int size,Color color){g.setColor(color);g.fillPolygon(new int[]{x,x+size,x+size/2},new int[]{y,y,y+size},3);}
    private void imageDisabled(Graphics2D g,String path,int x,int y,int maxW,int maxH){BufferedImage source=images.get(path);if(source==null)return;double scale=Math.min(maxW/(double)source.getWidth(),maxH/(double)source.getHeight());int w=(int)(source.getWidth()*scale),h=(int)(source.getHeight()*scale);BufferedImage gray=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);Graphics2D copy=gray.createGraphics();copy.drawImage(source,0,0,w,h,null);copy.dispose();for(int py=0;py<h;py++)for(int px=0;px<w;px++){int rgb=gray.getRGB(px,py),r=(rgb>>16)&255,gr=(rgb>>8)&255,b=rgb&255;int light=(r+gr+b)/3;if(light>225)light=190;gray.setRGB(px,py,(light<<16)|(light<<8)|light);}g.drawImage(gray,x-w/2,y-h/2,null);}
    private void drawSubstituteMenu(Graphics2D g){int side=selectedCard,left=side==0?150:1057;Team team=side==0?home:away;int y=736;rect(g,left,y,436,team.substitutes().size()*38+8,Color.WHITE);for(int i=0;i<team.substitutes().size();i++){Player p=team.substitutes().get(i);int rowY=y+4+i*38;if(i==benchIndex[side])rect(g,left+2,rowY,432,36,new Color(215,232,218));text(g,p.name()+" - "+p.position()+" - "+p.energy()+"%",left+8,rowY+25,19,Color.BLACK,false);}}

    private void drawPrediction(Graphics2D g){
        background(g,"backgrounds/simulator.png");rect(g,135,94,1650,424,Color.WHITE);rect(g,680,51,560,82,new Color(201,211,220));center(g,"Probabilidades de desfecho",960,105,39,Color.BLACK,true);
        String[] names={"Vitória da casa","Empate","Vitória fora"};double[] percentages={prediction.homeWin(),prediction.draw(),prediction.awayWin()};Color[] colors={new Color(0,189,112),new Color(201,211,220),new Color(223,23,45)};
        for(int i=0;i<3;i++){int y=185+i*107;text(g,names[i],185,y,33,colors[i],false);text(g,String.format("%.1f%%",percentages[i]),1658,y,33,Color.BLACK,false);g.setColor(Color.BLACK);g.setStroke(new BasicStroke(3));g.drawRect(185,y+12,1550,48);rect(g,187,y+14,(int)(1546*percentages[i]/100),44,colors[i]);}
        rect(g,135,566,1143,406,Color.WHITE);rect(g,460,534,558,82,new Color(201,211,220));center(g,"Resultados mais prováveis",739,588,39,Color.BLACK,true);for(int i=0;i<3;i++){MatchMath.ScoreProbability s=prediction.likelyScores().get(i);text(g,(i+1)+". "+home.name()+" "+s.homeGoals()+" - "+s.awayGoals()+" "+away.name()+": "+String.format("%.1f",s.chance()*100)+"% de probabilidade",205,680+i*100,36,Color.BLACK,false);}
        rect(g,1328,566,457,406,Color.WHITE);rect(g,1443,540,229,82,new Color(201,211,220));center(g,"Vencedor",1558,594,39,Color.BLACK,true);Team winner=prediction.homeWin()>=prediction.awayWin()?home:away;image(g,winner.logoPath(),1558,780,260,260);boolean backHover="prediction-back".equals(hoveredButton);text(g,"Voltar",45,1034,42,Color.WHITE,backHover);
    }

    private void start(){if(homeIndex==awayIndex)awayIndex=(awayIndex+1)%TeamCatalog.teams().size();home=createSelectedTeam(0);away=createSelectedTeam(1);if(screen==Screen.SELECT_SIMULATION){prediction=MatchMath.predict(home,away);screen=Screen.PREDICTION;}else{screen=Screen.MATCH;minute=homeGoals=awayGoals=0;matchFinished=false;matchStarted=false;running=false;selectedPlayer=new int[]{-1,-1};benchIndex=new int[]{0,0};substitutions=new int[]{0,0};commentary.clear();}repaint();}
    private Team createSelectedTeam(int side){int index=side==0?homeIndex:awayIndex;if(index!=TeamCatalog.teams().size()-1)return TeamCatalog.create(index);return TeamCatalog.createCustom(customNames[side].getText().trim().isEmpty()?"A minha equipa":customNames[side].getText().trim(),(Integer)customStrengths[side].getValue());}
    private void changeTeam(int side,int direction){int n=TeamCatalog.teams().size();if(side==0)homeIndex=(homeIndex+direction+n)%n;else awayIndex=(awayIndex+direction+n)%n;}
    private void advanceMinute(){
        if(!running||matchFinished)return;
        minute++;
        for(Team team:new Team[]{home,away})for(Player player:team.starters())player.tire();
        boolean goalScored=false;
        for(int side=0;side<2;side++){
            Team team=side==0?home:away;
            double current=team.starters().stream().mapToDouble(p->p.quality()*p.energy()/100.0).average().orElse(team.strength());
            if(!goalScored&&Math.random()<.022*current/75*(side==0?1.08:.96)){
                if(side==0)homeGoals++;else awayGoals++;
                commentary.add("GOLO aos "+minute+"'! "+team.name()+".");
                goalScored=true;
            }
        }
        if(goalScored){
            timer.stop();
            SoundPlayer.play("goal_sound.WAV");
        }
        if(minute==45){commentary.add("Intervalo.");running=false;timer.stop();if(goalScored)afterGoalPause(()->SoundPlayer.play("referee_whistle.wav"));else SoundPlayer.play("referee_whistle.wav");}
        if(minute>=90){commentary.add("Apito final.");running=false;matchFinished=true;timer.stop();if(goalScored)afterGoalPause(()->SoundPlayer.play("referee_whistle.wav"));else SoundPlayer.play("referee_whistle.wav");}
        repaint();
        if(goalScored&&minute<45||goalScored&&minute>45&&minute<90)afterGoalPause(()->{if(running&&!matchFinished&&screen==Screen.MATCH)timer.start();});
    }

    private void afterGoalPause(Runnable afterPause){javax.swing.Timer pause=new javax.swing.Timer(2000,e->afterPause.run());pause.setRepeats(false);pause.start();}
    private void substitute(int side){if(substitutions[side]>=5||selectedPlayer[side]<0)return;Team team=side==0?home:away;int i=selectedPlayer[side], b=benchIndex[side];Player out=team.starters().get(i), in=team.substitutes().get(b);if(!out.position().equals(in.position())){commentary.add("Substituição inválida: as posições têm de coincidir.");return;}team.starters().set(i,in);team.substitutes().set(b,out);substitutions[side]++;commentary.add(team.name()+": entra "+in.name()+", sai "+out.name()+".");}
    private Point logical(MouseEvent e){return new Point((int)(e.getX()/sx()),(int)(e.getY()/sy()));}

    private void setHoveredButton(String button){if(Objects.equals(hoveredButton,button))return;hoveredButton=button;repaint();}
    @Override public void mouseMoved(MouseEvent e){Point p=logical(e);int next=-1;String button="";
        if(screen==Screen.MENU){for(int i=0;i<5;i++)if(p.x<900&&p.y>=430+i*101&&p.y<=520+i*101)next=i;if(next==4)button="menu-exit";}
        else if(screen==Screen.SELECT_1V1||screen==Screen.SELECT_SIMULATION){if(p.y>960&&p.x<260)button="selection-back";else if(p.y>930&&p.x>1580)button="selection-action";}
        else if(screen==Screen.MATCH){if(p.x>=86&&p.x<=300&&p.y>=66&&p.y<=145)button="match-exit";else if(p.x>=1570&&p.x<=1828&&p.y>=764&&p.y<=945&&!matchFinished)button="match-play";}
        else if(screen==Screen.PREDICTION&&p.y>960&&p.x<260)button="prediction-back";
        if(next!=menuHover){menuHover=next;repaint();}setHoveredButton(button);
    }
    @Override public void mouseClicked(MouseEvent e){Point p=logical(e);int x=p.x,y=p.y;if(screen==Screen.MENU){for(int i=0;i<5;i++)if(x<900&&y>=430+i*101&&y<=520+i*101){SoundPlayer.play("click_sound.wav");if(i==0)screen=Screen.SELECT_1V1;else if(i==1)screen=Screen.SELECT_SIMULATION;else if(i==4)SwingUtilities.getWindowAncestor(this).dispose();repaint();return;}}
        else if(screen==Screen.SELECT_1V1||screen==Screen.SELECT_SIMULATION){if(y>960&&x<260){SoundPlayer.play("click_sound.wav");screen=Screen.MENU;}else if(y>930&&x>1580){SoundPlayer.play("click_sound.wav");start();return;}else handleSelectionClick(x,y);repaint();}
        else if(screen==Screen.PREDICTION&&y>960){screen=Screen.SELECT_SIMULATION;repaint();}
        else if(screen==Screen.MATCH)handleMatchClick(x,y);
    }
    private void handleSelectionClick(int x,int y){int side=-1,start=0,end=0;if(x>=135&&x<=807){side=0;start=135;end=807;}else if(x>=1114&&x<=1786){side=1;start=1114;end=1786;}if(side<0||y<520||y>760)return;selectedCard=side;if(x<start+180)changeTeam(side,-1);else if(x>end-180)changeTeam(side,1);}
    private void handleMatchClick(int x,int y){if(x>=86&&x<=300&&y>=66&&y<=145){SoundPlayer.play("click_sound.wav");timer.stop();running=false;substitutesOpen=false;screen=Screen.SELECT_1V1;repaint();return;}if(x>=1570&&x<=1828&&y>=764&&y<=945){if(matchFinished)return;SoundPlayer.play("click_sound.wav");if(!matchStarted){matchStarted=true;commentary.add("Apito inicial. Boa partida!");SoundPlayer.play("referee_whistle.wav");}running=!running;if(running)timer.start();else timer.stop();repaint();return;}int side=selectedCard,left=side==0?150:1057;if(substitutesOpen&&x>=left&&x<=left+436&&y>=736&&y<736+7*38+8){benchIndex[side]=Math.min(6,(y-740)/38);substitutesOpen=false;repaint();return;}if(x<left||x>left+713)return;if(y>=290&&y<=665){int row=(y-290)/34;if(row<11)selectedPlayer[side]=row;}else if(y>=691&&y<=735){if(x<=left+436)substitutesOpen=!substitutesOpen;else substitute(side);}repaint();}
    @Override public void mouseExited(MouseEvent e){if(menuHover!=-1){menuHover=-1;repaint();}setHoveredButton("");}
    @Override public void mouseEntered(MouseEvent e){} @Override public void mousePressed(MouseEvent e){} @Override public void mouseReleased(MouseEvent e){} @Override public void mouseDragged(MouseEvent e){}
}

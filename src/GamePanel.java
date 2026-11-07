package roadtogold;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public final class GamePanel extends JPanel implements MouseListener, MouseMotionListener {
    private enum Screen { MENU, SELECT_1V1, SELECT_SIMULATION, MATCH, PREDICTION }
    private static final int W = 1920, H = 1080;
    private final Map<String, BufferedImage> images = new HashMap<>();
    private Screen screen = Screen.MENU;
    private int homeIndex = 0, awayIndex = 1, selectedCard = 0, menuHover = -1;
    private Team home, away;
    private int minute, homeGoals, awayGoals;
    private int[] selectedPlayer = {-1, -1}, benchIndex = {0, 0}, substitutions = {0, 0};
    private final java.util.List<String> commentary = new ArrayList<>();
    private boolean running;
    private final javax.swing.Timer timer;

    public GamePanel() {
        setPreferredSize(new Dimension(1280, 720));
        setBackground(new Color(6, 26, 56));
        setFocusable(true);
        addMouseListener(this); addMouseMotionListener(this);
        for (String path : new String[]{"backgrounds/menu.jpg", "backgrounds/1v1.png", "backgrounds/simulator.png", "assets/kicking_ball.png"}) load(path);
        for (TeamCatalog.TeamInfo team : TeamCatalog.teams()) load(team.logo());
        timer = new javax.swing.Timer(350, e -> advanceMinute());
    }

    private void load(String path) { try { images.put(path, ImageIO.read(new File(path))); } catch (IOException ignored) { } }
    private double sx() { return getWidth() / (double) W; }
    private double sy() { return getHeight() / (double) H; }

    @Override protected void paintComponent(Graphics raw) {
        super.paintComponent(raw); Graphics2D g = (Graphics2D) raw.create();
        g.scale(sx(), sy()); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        switch (screen) { case MENU -> drawMenu(g); case SELECT_1V1, SELECT_SIMULATION -> drawSelection(g); case MATCH -> drawMatch(g); case PREDICTION -> drawPrediction(g); }
        g.dispose();
    }

    private void background(Graphics2D g, String path) { BufferedImage i=images.get(path); if(i!=null)g.drawImage(i,0,0,W,H,null); else {g.setColor(new Color(6,26,56));g.fillRect(0,0,W,H);} }
    private void rect(Graphics2D g, int x, int y, int width, int height, Color color) { g.setColor(color); g.fillRect(x,y,width,height); }
    private void text(Graphics2D g, String text, int x, int y, int size, Color color, boolean bold) { g.setColor(color); g.setFont(new Font("Segoe UI", bold?Font.BOLD:Font.PLAIN,size)); g.drawString(text,x,y); }
    private void center(Graphics2D g, String text, int x, int y, int size, Color color, boolean bold) { g.setFont(new Font("Segoe UI",bold?Font.BOLD:Font.PLAIN,size)); g.setColor(color); g.drawString(text,x-g.getFontMetrics().stringWidth(text)/2,y); }
    private void image(Graphics2D g, String path, int x, int y, int maxW, int maxH) { BufferedImage i=images.get(path); if(i==null)return; double s=Math.min(maxW/(double)i.getWidth(),maxH/(double)i.getHeight()); int w=(int)(i.getWidth()*s),h=(int)(i.getHeight()*s);g.drawImage(i,x-w/2,y-h/2,w,h,null); }

    private void drawMenu(Graphics2D g) {
        background(g,"backgrounds/menu.jpg");
        text(g,"Road",39,110,86,Color.WHITE,true); text(g,"To",39,221,86,Color.WHITE,true); text(g,"Gold",39,332,86,new Color(249,194,56),true);
        String[] options={"JOGAR MODO 1V1","JOGAR MODO SIMULAÇÃO","JOGAR MODO LIGA","DEFINIÇÕES","SAIR"};
        for(int i=0;i<options.length;i++){int y=490+i*101;if(menuHover==i)text(g,"▶",15,y,60,Color.WHITE,true);text(g,options[i],76,y,56,Color.WHITE,true);}
        text(g,"Diogo Amorim",14,1060,27,Color.WHITE,false);
    }

    private void drawSelection(Graphics2D g) {
        boolean sim=screen==Screen.SELECT_SIMULATION; background(g,sim?"backgrounds/simulator.png":"backgrounds/1v1.png");
        text(g,sim?"MODO SIMULAÇÃO RÁPIDA":"MODO 1V1",110,132,74,Color.WHITE,true);
        text(g,sim?"Escolhe qualquer equipa e descobre quais os resultados mais prováveis.":"Escolhe as equipas e acompanha a partida minuto a minuto.",110,194,39,Color.WHITE,false);
        drawTeamCard(g,135,267,"Casa",homeIndex,0); drawTeamCard(g,1114,267,"Fora",awayIndex,1);
        if(!sim){rect(g,135,902,672,70,new Color(8,124,112));if(selectedCard==0)center(g,"SELECIONADO",471,948,28,Color.WHITE,true);}
        text(g,"Voltar",45,1034,42,Color.WHITE,false); text(g,"Jogar",1665,1013,38,Color.WHITE,false); center(g,"▶",1840,1016,92,Color.WHITE,false);
    }

    private void drawTeamCard(Graphics2D g,int x,int y,String title,int index,int side){boolean selected=screen==Screen.SELECT_1V1&&selectedCard==side;rect(g,x-10,y-10,692,655,selected?new Color(8,124,112):new Color(6,26,56));rect(g,x,y,672,635,Color.WHITE);rect(g,x+191,y-38,290,74,new Color(201,211,220));center(g,title,x+336,y+12,42,Color.BLACK,true);TeamCatalog.TeamInfo team=TeamCatalog.teams().get(index);int font=team.name().length()>15?51:68;center(g,team.name(),x+336,y+140,font,Color.BLACK,true);image(g,team.logo(),x+336,y+380,280,310);center(g,"Força: "+team.strength(),x+336,y+580,53,Color.BLACK,true);center(g,"◀",x+78,y+400,78,new Color(201,211,220),false);center(g,"▶",x+594,y+400,78,new Color(201,211,220),false);}

    private void drawMatch(Graphics2D g) {
        background(g,"backgrounds/1v1.png");
        rect(g,86,66,214,79,Color.WHITE);center(g,"Sair",193,119,39,Color.BLACK,false);
        rect(g,418,37,843,149,Color.WHITE); image(g,home.logoPath(),550,86,80,80);image(g,away.logoPath(),1150,86,80,80);
        center(g,home.name(),550,164,30,Color.BLACK,false);center(g,away.name(),1150,164,30,Color.BLACK,false);center(g,homeGoals+" - "+awayGoals,840,126,58,Color.BLACK,true);center(g,String.format("%02d'",minute),840,165,25,Color.BLACK,false);
        rect(g,85,219,1500,529,new Color(11,125,45));g.setColor(new Color(0,27,18));g.setStroke(new BasicStroke(8));g.drawRect(85,219,1500,529);
        drawSquad(g,93,233,home,0); drawSquad(g,884,233,away,1);
        rect(g,0,758,1340,322,new Color(29,43,54));text(g,"Relato da partida",18,793,21,Color.WHITE,true);rect(g,18,808,1312,252,new Color(20,33,43));int first=Math.max(0,commentary.size()-8);for(int i=first;i<commentary.size();i++)text(g,commentary.get(i),18,832+(i-first)*27,18,Color.WHITE,false);
        rect(g,1390,764,258,181,Color.WHITE);image(g,"assets/kicking_ball.png",1510,854,180,150);center(g,running?"Ⅱ":"▶",1604,875,52,Color.BLACK,true);
    }

    private void drawSquad(Graphics2D g,int x,int y,Team team,int side){
        text(g,"Jogador",x+28,y+31,31,Color.WHITE,true);text(g,"Pos.",x+215,y+31,31,Color.WHITE,true);text(g,"Energia",x+355,y+31,31,Color.WHITE,true);text(g,"Qualidade",x+520,y+31,31,Color.WHITE,true);rect(g,x,y+49,713,4,Color.WHITE);
        for(int i=0;i<team.starters().size();i++){Player p=team.starters().get(i);int yy=y+81+i*34;if(selectedPlayer[side]==i)rect(g,x+4,yy-20,705,33,new Color(7,102,36));text(g,p.name(),x+5,yy,24,Color.WHITE,false);text(g,p.position(),x+237,yy,22,Color.WHITE,false);rect(g,x+325,yy-14,103,28,Color.WHITE);rect(g,x+325,yy-14,(int)(103*p.energy()/100.0),28,new Color(80,215,109));text(g,p.energy()+"%",x+437,yy,21,Color.WHITE,false);text(g,String.valueOf(p.quality()),x+583,yy,22,Color.WHITE,false);}
        int yy=y+458;rect(g,x,yy,436,44,Color.WHITE);Player b=team.substitutes().get(benchIndex[side]);text(g,b.name()+" - "+b.position()+" - "+b.energy()+"%",x+5,yy+28,20,Color.BLACK,false);center(g,"▼",x+410,yy+29,28,Color.BLACK,false);rect(g,x+444,yy,236,44,Color.WHITE);center(g,"Substituir selecionado",x+562,yy+28,18,Color.BLACK,false);
    }

    private void drawPrediction(Graphics2D g){
        background(g,"backgrounds/simulator.png");MatchMath.Prediction prediction=MatchMath.predict(home,away);rect(g,135,94,1650,424,Color.WHITE);rect(g,680,51,560,82,new Color(201,211,220));center(g,"Probabilidades de desfecho",960,105,39,Color.BLACK,true);
        String[] names={"Vitória da casa","Empate","Vitória fora"};double[] percentages={prediction.homeWin(),prediction.draw(),prediction.awayWin()};Color[] colors={new Color(0,189,112),new Color(201,211,220),new Color(223,23,45)};
        for(int i=0;i<3;i++){int y=185+i*107;text(g,names[i],185,y,33,colors[i],false);text(g,String.format("%.1f%%",percentages[i]),1658,y,33,Color.BLACK,false);g.setColor(Color.BLACK);g.setStroke(new BasicStroke(3));g.drawRect(185,y+12,1550,48);rect(g,187,y+14,(int)(1546*percentages[i]/100),44,colors[i]);}
        rect(g,135,566,1143,406,Color.WHITE);rect(g,460,534,558,82,new Color(201,211,220));center(g,"Resultados mais prováveis",739,588,39,Color.BLACK,true);for(int i=0;i<3;i++){MatchMath.ScoreProbability s=prediction.likelyScores().get(i);text(g,(i+1)+". "+home.name()+" "+s.homeGoals()+" - "+s.awayGoals()+" "+away.name()+": "+String.format("%.1f",s.chance()*100)+"% de probabilidade",205,680+i*100,36,Color.BLACK,false);}
        rect(g,1328,566,457,406,Color.WHITE);rect(g,1443,540,229,82,new Color(201,211,220));center(g,"Vencedor",1558,594,39,Color.BLACK,true);Team winner=prediction.homeWin()>=prediction.awayWin()?home:away;image(g,winner.logoPath(),1558,780,260,260);text(g,"Voltar",45,1034,42,Color.WHITE,false);
    }

    private void start(){if(homeIndex==awayIndex)awayIndex=(awayIndex+1)%TeamCatalog.teams().size();home=TeamCatalog.create(homeIndex);away=TeamCatalog.create(awayIndex);if(screen==Screen.SELECT_SIMULATION){screen=Screen.PREDICTION;}else{screen=Screen.MATCH;minute=homeGoals=awayGoals=0;selectedPlayer=new int[]{-1,-1};benchIndex=new int[]{0,0};substitutions=new int[]{0,0};commentary.clear();commentary.add("Apito inicial. Boa partida!");}repaint();}
    private void changeTeam(int side,int direction){int n=TeamCatalog.teams().size();if(side==0)homeIndex=(homeIndex+direction+n)%n;else awayIndex=(awayIndex+direction+n)%n;}
    private void advanceMinute(){if(!running)return;minute++;for(Team team:new Team[]{home,away})for(Player player:team.starters())player.tire();for(int side=0;side<2;side++){Team team=side==0?home:away;double current=team.starters().stream().mapToDouble(p->p.quality()*p.energy()/100.0).average().orElse(team.strength());if(Math.random()<.022*current/75*(side==0?1.08:.96)){if(side==0)homeGoals++;else awayGoals++;commentary.add("GOLO aos "+minute+"'! "+team.name()+".");}}if(minute==45){commentary.add("Intervalo.");running=false;timer.stop();}if(minute>=90){commentary.add("Apito final.");running=false;timer.stop();}repaint();}
    private void substitute(int side){if(substitutions[side]>=5||selectedPlayer[side]<0)return;Team team=side==0?home:away;int i=selectedPlayer[side], b=benchIndex[side];Player out=team.starters().get(i), in=team.substitutes().get(b);if(!out.position().equals(in.position())){commentary.add("Substituição inválida: as posições têm de coincidir.");return;}team.starters().set(i,in);team.substitutes().set(b,out);substitutions[side]++;commentary.add(team.name()+": entra "+in.name()+", sai "+out.name()+".");}
    private Point logical(MouseEvent e){return new Point((int)(e.getX()/sx()),(int)(e.getY()/sy()));}

    @Override public void mouseMoved(MouseEvent e){if(screen!=Screen.MENU)return;Point p=logical(e);int next=-1;for(int i=0;i<5;i++)if(p.x<900&&p.y>=430+i*101&&p.y<=520+i*101)next=i;if(next!=menuHover){menuHover=next;repaint();}}
    @Override public void mouseClicked(MouseEvent e){Point p=logical(e);int x=p.x,y=p.y;if(screen==Screen.MENU){for(int i=0;i<5;i++)if(x<900&&y>=430+i*101&&y<=520+i*101){if(i==0)screen=Screen.SELECT_1V1;else if(i==1)screen=Screen.SELECT_SIMULATION;else if(i==4)SwingUtilities.getWindowAncestor(this).dispose();repaint();return;}}
        else if(screen==Screen.SELECT_1V1||screen==Screen.SELECT_SIMULATION){if(y>960&&x<260){screen=Screen.MENU;}else if(y>930&&x>1580){start();return;}else handleSelectionClick(x,y);repaint();}
        else if(screen==Screen.PREDICTION&&y>960){screen=Screen.SELECT_SIMULATION;repaint();}
        else if(screen==Screen.MATCH)handleMatchClick(x,y);
    }
    private void handleSelectionClick(int x,int y){int side=-1,start=0,end=0;if(x>=135&&x<=807){side=0;start=135;end=807;}else if(x>=1114&&x<=1786){side=1;start=1114;end=1786;}if(side<0||y<520||y>760)return;if(x<start+180)changeTeam(side,-1);else if(x>end-180)changeTeam(side,1);else selectedCard=side;}
    private void handleMatchClick(int x,int y){if(x>=86&&x<=300&&y>=66&&y<=145){timer.stop();running=false;screen=Screen.SELECT_1V1;repaint();return;}if(x>=1390&&x<=1648&&y>=764&&y<=945){running=!running;if(running)timer.start();else timer.stop();repaint();return;}for(int side=0;side<2;side++){int left=side==0?93:884;if(x<left||x>left+713)continue;if(y>=290&&y<=665){int row=(y-290)/34;if(row<11)selectedPlayer[side]=row;}else if(y>=691&&y<=735){if(x<=left+436)benchIndex[side]=(benchIndex[side]+1)%7;else substitute(side);}repaint();return;}}
    @Override public void mouseExited(MouseEvent e){if(screen==Screen.MENU&&menuHover!=-1){menuHover=-1;repaint();}}
    @Override public void mouseEntered(MouseEvent e){} @Override public void mousePressed(MouseEvent e){} @Override public void mouseReleased(MouseEvent e){} @Override public void mouseDragged(MouseEvent e){}
}

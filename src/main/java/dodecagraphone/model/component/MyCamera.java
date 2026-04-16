package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.*;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * The Camera includes the score, the chord symbol line and the play bar. 
 * The camera shows the visible part of the score and the chord symbol line.
 * When the camera is playing the score and the chord symbol line move to 
 * the left. In the current version (2.0), the play bar has two positions,
 * depending on the position of the keyboard. When the score hits the playbar
 * the corresponding column of the score is played. To control the tempo,
 * the MyCamera::checkTick() method returns true only if the elapsed time fits
 * the actual tempo (see MyTempo). The score within the camera is organized
 * in pages, and there are methods to change the page.
 * 
 * The part of the score that is visible depends on MyGridScore::currentCol.
 * It is the first score column that is hidden to the right of the camera.
 * When the keyboard is on the right, the currentCol is initialized
 * just to the right of the playBar.
 *
 * @author Pau
 */
public class MyCamera extends MyComponent {

    /** The score. The camera shows only the visible part of the score. */
    public MyGridScore score;
    /** The chord symbol line. An horizontal strip where chords can be
     * displayed at their corresponding beat and measure (as in a fake book).*/
    public MyChordSymbolLine chordSymbolLine;
    /** The lyrics strip below the score. */
    public MyLyrics lyrics;
    /** The camera column of the play bar. The score column that hits the
     * playbar is the column that is being played. 
     * When the keyboard is on the left,
     * the playBar is at column 0. When the keyboard is on the right, the
     * play bar is at the right side, hidden under the keyboard. In this way
     * the user does not see the column that is beeing played until after
     * a couple of columns (useful for guessing exercises). See 
     * Settings.R_PLAYBAR_OFFSET
     */
    public int playBarCol; 
    /** A timer to control the speed of the execution. */
//    private final MyTimer timer;  
    /** A flag that is on when the camera is playing. */
    private volatile boolean playing;
    /** The page of the score that is currently on display.*/
    private int currentPage;
    
    /**
     * Sets camera screen coordinates, and creates the timer.
     *
     * @param firstCol
     * @param firstRow
     * @param nCols
     * @param nRows
     * @param parent
     * @param contr
     */
    public MyCamera(int firstCol, int firstRow, int nCols, int nRows, MyComponent parent,MyController contr) {
        super(firstCol, firstRow, nCols, nRows, parent, contr);
//        this.timer = new MyTimer();
        this.playing = false;
        this.currentPage = 1;
        this.score = null;
    }

    /** 
     * Sets the score. After setting the score and the chord symbol
     * line, the camera should be reset.
     * @param score 
     */
    public void setScore(MyGridScore score) {
        this.score = score;
   }

    /**
     * Sets the chord symbol line.
     * @param chords
     */
    public void setSymbolLine(MyChordSymbolLine chords) {
        this.chordSymbolLine = chords;
    }

    /**
     * Sets the lyrics strip.
     * @param lyrics
     */
    public void setLyrics(MyLyrics lyrics) {
        this.lyrics = lyrics;
    }

    /**
     * getter.
     * @return 
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * setter.
     * @param col 
     */
    public void setPlayBar(int col) {
        playBarCol = col;
    }

    /**
     * getter.
     * @return 
     */
    public int getPlayBar() {
        return playBarCol;
    }

//    public MyTimer getTimer() {
//        return timer;
//    }
//
    /**
     * Resets the timer, the playing flag, the current col of the score and
     * the current page.
     */
    public void reset() {
//        this.timer.reset();
        this.playing = false;
        boolean left = !this.controller.getAllPurposeScore().isUseScreenKeyboardRight();
        this.score.setCurrentCol(Settings.getInitialCurrentCol(left,score));
        this.currentPage = 1;
    }

    /**
     * Sets the score currentCol to the firts column of the page.
     * @param page
     */
    public void goToFirstColOfPage(int page) {
        int nColsPage = score.getNumColsPage();
        boolean left = !this.controller.getAllPurposeScore().isUseScreenKeyboardRight();
        score.setCurrentCol((Settings.getInitialCurrentCol(left,score)) + (page - 1) * nColsPage);
    }

    /**
     * Updates the current page number.
     */
    public void updateCurrentPage(){
        boolean left = !this.controller.getAllPurposeScore().isUseScreenKeyboardRight();
        this.currentPage = 1+(score.getCurrentCol() - (Settings.getInitialCurrentCol(left,score)))/score.getNumColsPage();
    }
    
    /**
     * Moves the score to the next page.
     */
    public void nextPage() {
        if (score.getCurrentCol() < Settings.getnColsScore()) {
            this.currentPage++;
        }
        goToFirstColOfPage(currentPage);
        this.playing = false;
//        this.timer.reset();
    }

    /**
     * Moves the score to the previous page.
     */
    public void prevPage() {
        if (this.getCurrentPage() > 1) {
            this.currentPage--;
        }
        goToFirstColOfPage(currentPage);
        this.playing = false;
//        this.timer.reset();
    }

    /**
     * Moves the score to the left, by incrementing the score's currentCol.
     */
    public void updateCurrentCol() {
//        System.out.println("MyCamera::entering updateCurrentCol currentCol ="+ score.getCurrentCol());
        if (score.getCurrentCol() < Settings.getnColsScore()) {
//            synchronized (score.getOffscreenGraphics()) {
                //score.setDrawNewCol(true);
                score.incrementCurrentCol();
//                if (score.isDrawNewCol()) {
////                   score.drawFullGridinOffscreen();
//                    score.drawNewColInOffscreen();
//                    score.setDrawNewCol(false);
//                    if (!SwingUtilities.isEventDispatchThread()) {
//                        SwingUtilities.invokeLater(() -> this.controller.getUi().getPanel().repinta(true));
//                    }  
//                }
                updateCurrentPage();
                //this.controller.getUi().getPanel().repinta(true); //CAL?
//          }
//            System.out.println("MyCamera::updateCurrentCol: if ok");
        } else {
            this.playing = false;
//            System.out.println("MyCamera::updateCurrentCol: entering else");
        }
//        System.out.println("MyCamera::leaving updateCurrentCol currentCol = "+ score.getCurrentCol()+", Thread = "+Thread.currentThread());
    }

    /**
     * When the camera is playing it returns true when the current timer
     * tick is finished, then resets the timer.
     * @return 
     */
//    public long checkTick() {
////       if (this.playing) {
////            try {
////                Thread.sleep(MyTempo.getNanosPerSquareGrid()); // Pausa el fil actual
////            } catch (InterruptedException e) {
////                System.err.println("Timer interrupted!");
////                Thread.currentThread().interrupt(); // Restaura l'estat d'interrupció del fil
////            }
//            double nanosPerSquare = Math.round(MyTempo.getNanosPerSquareGrid());
//            long el = this.timer.elapsedNanos();
//            if (el >= nanosPerSquare) {
//                this.timer.reset();
////        System.out.println("MyCamera::checkTick: playing = " + this.playing + ", reset = "+el);
//  //              return true;
//            }
////        }
////        System.out.println("MyCamera::checkTick: endOfTick");
////        return false;
//    }

    /**
     * Resets the timer and sets playing to true. The actual play command
     * is Controller::playScoreColAtPlayBar().
     */
//    public void play() {
//        this.timer.reset();
//        this.playing = true;
//    }

    /**
     * Sets playing to false.
     */
    public void stop() {
        this.playing = false;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    /**
     * getter.
     * @return 
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * Draws the playbar, a thin vertical rectangle.
     * @param g 
     */
    public void drawPlayBar(Graphics2D g){
        double rh = Settings.getRowHeight();
        Stroke stroke = g.getStroke();
        g.setStroke(new BasicStroke((float) 3));
        g.setColor(java.awt.Color.BLACK);
        int x = (int) getScreenX(playBarCol);
        // Extend from top of camera (chord rows) to bottom (lyrics rows)
        int y = (int) getScreenY(0) + 1;
        int h = (int) (Settings.getnRowsCam() * rh) - 2;
//        if (this.controller.isPrinting()){
            g.drawLine(x, y, x, y + h);
//        }else{
//            g.drawRect(x, y, w, h);
//        }
        g.setStroke(stroke);
    }
    
    /** 
     * Draws the score, the chord symbol line and the play bar.
     * @param g 
     */
    @Override
    public void draw(Graphics2D g) {
        if (Settings.SHOW_DRAW_HIERARCHY) {
            Utilities.printOutWithPriority(5, "MyCamera::draw: drawing " + this.getClass());
        }
//        System.out.println("MyCamera::draw: (pX,pY,w,h) = (" +
//                (int) screenPosX + "," + (int) screenPosY + "," + (int) width + "," + (int) height + ")");
//        g.setColor(java.awt.Color.RED);
//        g.fillRect((int) screenPosX, (int) screenPosY, (int) width, (int) height);
// here
//        if (this.score.isNeedsDrawing()){
            this.score.draw(g);
//            this.score.setNeedsDrawing(false);
//        }
//        if (this.chordSymbolLine.isNeedsDrawing()){
            this.chordSymbolLine.draw(g);
//            this.score.setNeedsDrawing(false);
//        }
        if (this.lyrics != null) {
            this.lyrics.draw(g);
        }
        this.drawPlayBar(g);
        g.setColor(java.awt.Color.BLACK);
        g.drawRect((int) screenPosX, (int) screenPosY, (int) width, (int) height);
    }
}

package dodecagraphone.ui;

import dodecagraphone.MyController;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Manages drawing (paintComponent()), mouse and buttons. Drawing is done on the
 * JPanel (drawing on an Image of the JFrame produces flickering).
 *
 * @author upcnet
 */
public class MyNewPanel extends JPanel implements ActionListener, KeyListener {

    private MyController controller;
    private JButton b, b2;
    private Graphics2D pantalla;
//    private Image buffer; // Imatge en memòria
//    private Graphics bufferGraphics; // Objecte Graphics per a la imatge en memòria

    public MyNewPanel(MyController contr) {
        //               setPreferredSize(new Dimension(500, 500));
//        Dimension d = getPreferredSize();
//        buffer = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
//        bufferGraphics = buffer.getGraphics();

        this.controller = contr;
        this.setDoubleBuffered(true);        // Dibuixa els gràfics a la imatge en memòria
        this.setBorder(BorderFactory.createLineBorder(Color.black));
        this.addMouseListener(new MyNewMouseAdapter(this, this.controller));
        this.addMouseMotionListener(new MyNewMouseAdapter(this, this.controller));

        // ImageIcon leftButtonIcon = createImageIcon("images/right.gif");
        this.b = new JButton("Test Button");//,leftButtonIcon);        
//        b1.setVerticalTextPosition(AbstractButton.CENTER);
//        b1.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
//        b1.setMnemonic(KeyEvent.VK_D);
        this.b.setActionCommand("button");

        //Listen for actions on buttons 1 and 3.
        b.addActionListener(this);
        b.setToolTipText("Click this button to show demo message.");

        //Add Components to this container, using the default FlowLayout.
//        this.add(b);
        this.b2 = new JButton("Test Button");
        this.b2.setActionCommand("button2");
        b2.addActionListener(this);
        b2.setToolTipText("Click this button to show demo 2 message.");
//        this.add(b2);   
        this.addKeyListener(this);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                MyNewPanel.this.controller.setNeedsDrawing(true);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                MyNewPanel.this.controller.setNeedsDrawing(true);
            }
        });

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int)Settings.getScreenWidth(),(int)Settings.getScreenHeight());
    }

    public Graphics2D getPantalla() {
        return pantalla;
    }

    /**
     * The key listener requires the focus on the JPanel.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        requestFocus();
    }

    private static int countPaintComponent = 1;
    private static boolean repintaWasCalled = false;
            
    

    public static boolean isRepintaWasCalled() {
        return repintaWasCalled;
    }

    public static void setRepintaWasCalled(boolean repintaWasCalled) {
        MyNewPanel.repintaWasCalled = repintaWasCalled;
    }

    /**
     * Main paint method. It is automatically invoked at each window change and
     * when repaint() is called (either on the JFrame or the JPanel). It should
     * not be explicitly called.
     *
     * @param g
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        //this.pantalla = (Graphics2D) g;
//        draw(bufferGraphics);
//        if (count%2==1) {
//        SwingUtilities.invokeLater(() -> { /
//        if (countPaintComponent == 2) return;
//        if (this.controller.isNeedsDrawing()) {
//            MyController.setDoRedraw(true);
//            try {
//                SwingUtilities.invokeAndWait(() -> this.repaint());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            //this.panel.paintImmediately(this.panel.getBounds());
            //this.validate(); 
//            System.out.println("MyUserInterface::paintComponent: redraw is now called");
//    Graphics2D g2 = (Graphics2D) g;
//    g2.setColor(Color.RED);
//    g2.fillRect(20, 20, 100, 100);
//    System.out.println("Dibuixant rectangle vermell");
//

        if (!isRepintaWasCalled()){
            if (!this.controller.getCam().isPlaying()) {
                Utilities.printOutWithPriority(3, "MyNewPanel::paintComponent: countPaintComponent = " + countPaintComponent + ", repintaWasCalled " + repintaWasCalled);
                this.controller.redraw((Graphics2D) g);
                countPaintComponent++;
            }
        } else {
            Utilities.printOutWithPriority(3, "MyNewPanel::paintComponent: countPaintComponent = " + countPaintComponent + ", repintaWasCalled " + repintaWasCalled);
            this.controller.redraw((Graphics2D) g);
            countPaintComponent++;
        }
            
            
        setRepintaWasCalled(false);
            
//        });
        //this.controller.setNeedsDrawing(false);
//            this.repaint(); // activa panel.paintComponent() ergo Controller.redraw()
        //this.controller.getMixer().repaintMixer();
//        }
//            count = 0;
//        }
//        System.out.println("MyUserInterface::paintComponent: count = "+ count);

//        // Copia la imatge en memòria a la pantalla
//        g.drawImage(buffer, 0, 0, null);
    }

//    private void draw(Graphics g) {
//        // Aquí pots posar el teu codi per dibuixar els gràfics
//    }
//
    /**
     * Action performed, triggered by a button event.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if ("button".equals(e.getActionCommand())) {
            this.controller.onDemoButtonPressed();
            //        b.setEnabled(false);
        } else if ("button2".equals(e.getActionCommand())) {
            this.controller.onDemoButton2Pressed();
            //        b.setEnabled(false);
        }
    }

//    /** Returns an ImageIcon, or null if the path was invalid. */
//    protected static ImageIcon createImageIcon(String path) {
//        java.net.URL imgURL = ButtonDemo.class.getResource(path);
//        if (imgURL != null) {
//            return new ImageIcon(imgURL);
//        } else {
//            System.err.println("Couldn't find file: " + path);
//            return null;
//        }
//    }
    @Override
    public void keyTyped(KeyEvent e) {
        if (controller.getMyLyrics().isEditMode()) {
            controller.getMyLyrics().handleKeyTyped(e);
            this.repinta(true);
        }
    }

    private static boolean needsRepaint = false;

    public void repinta(boolean immediately) {
        if (!needsRepaint) {
            needsRepaint = true;
            SwingUtilities.invokeLater(() -> {
                setRepintaWasCalled(true);
                this.controller.setNeedsDrawing(true);
                //System.out.println("MyNewPanel::repinta: countRepinta = "+(countRepinta++)+", immediately = "+immediately);
                if (immediately) {
                    this.paintImmediately(0, 0, (int) this.getWidth(), (int) this.getHeight());
                } else {
                    this.repaint();
                }
                needsRepaint = false;
            });
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // While in lyrics edit mode all key events are consumed by the editor
        if (controller.getMyLyrics().isEditMode()) {
            controller.getMyLyrics().handleKeyPressed(e);
            this.repinta(true);
            return;
        }
        controller.getButtons().hideTip();
        // Enter: col·loca el canvi pendent a la columna del playBar
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            controller.clearSelection();
            if (controller.placePendingChangeAtPlayBar()) {
                controller.getAllPurposeScore().drawCurrentCamInOffscreen();
                this.repinta(true);
            }
            return;
        }
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
            controller.clearSelection();
            if (controller.isPendingPaste()) {
                controller.cancelPaste();
            } else if (e.isShiftDown()) {
                controller.redo();
            } else {
                controller.undo();
            }
            controller.getAllPurposeScore().drawCurrentCamInOffscreen();
            controller.redrawChordLine();
            this.repinta(true);
            return;
        }
        if (e.isControlDown() && !e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_C) {
            controller.copySelection();
            controller.getAllPurposeScore().drawCurrentCamInOffscreen();
            controller.showClipboardTip();
            this.repinta(true);
            return;
        }
        if (e.isControlDown() && !e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_X) {
            controller.cutSelection();
            controller.getAllPurposeScore().drawCurrentCamInOffscreen();
            controller.showClipboardTip();
            this.repinta(true);
            return;
        }
        if (e.isControlDown() && !e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_V) {
            if (controller.isSelectionActive()) {
                controller.copySelection();
            }
            controller.startPaste();
            controller.getAllPurposeScore().drawCurrentCamInOffscreen();
            this.repinta(true);
            return;
        }
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_R) {
            boolean toEnd = e.isShiftDown();
            controller.replicateSelection(toEnd);
            controller.getAllPurposeScore().drawCurrentCamInOffscreen();
            this.repinta(true);
            return;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // System.err.println("Key released."); //To change body of generated methods, choose Tools | Templates.
    }

}

/**
 * Overrides callback methods invoked by mouse events.
 *
 * @author upcnet
 */
class MyNewMouseAdapter extends MouseAdapter {

    MyController controller;
    MyNewPanel panel;

    public MyNewMouseAdapter(MyNewPanel panel, MyController contr) {
        super();
        this.panel = panel;
        this.controller = contr;
    }

    @Override
    public void mouseMoved(MouseEvent e){
        double posX = e.getX();
        double posY = e.getY();
        controller.onMouseMoved(posX, posY);
        // this.panel.repinta(true);
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        double posX = e.getX();
        double posY = e.getY();

        if (SwingUtilities.isLeftMouseButton(e)) {
            controller.onMouseDragged(posX, posY);
        } else if (SwingUtilities.isRightMouseButton(e)) {
            controller.onRightMouseDragged(posX, posY);
        }

        controller.getAllPurposeScore().drawCurrentCamInOffscreen();
        this.panel.repinta(true);
    }

    @Override
    public void mousePressed(MouseEvent e) {
//        System.err.println("MyMouseListener.mousePressed()");
        panel.requestFocusInWindow(); // assegura que el JPanel té el focus de teclat
        double posX = e.getX();
        double posY = e.getY();
        int pointer = e.getButton();
        if (pointer == 1) {
            this.controller.onMousePressed(posX, posY, e.isShiftDown(), e.isControlDown(), e.isAltDown());
        } else if (pointer == 3) {
            this.controller.onRightMousePressed(posX, posY);
        }
        controller.getAllPurposeScore().drawCurrentCamInOffscreen();
        this.panel.repinta(true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int pointer = e.getButton();
        double posX = e.getX();
        double posY = e.getY();
        if (pointer == 1) {
            this.controller.onMouseReleased(posX, posY);
        } else if (pointer == 3) {
            this.controller.onRightMouseReleased(posX, posY);
        }
        //this.panel.paintImmediately(0, 0, panel.getWidth(), panel.getHeight());
        controller.getAllPurposeScore().drawCurrentCamInOffscreen();
        this.panel.repinta(true);
        this.panel.requestFocusInWindow();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
            int pointer = e.getButton();
            double posX = e.getX();
            double posY = e.getY();
            this.controller.onDoubleClick(posX, posY);
            controller.getAllPurposeScore().drawCurrentCamInOffscreen();
            this.panel.repinta(true);
        }
    }
}

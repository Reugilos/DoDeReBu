/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.ui;

import dodecagraphone.MyController;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
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
import javax.swing.Timer;

/**
 * [CA] JPanel principal de l'aplicació. Gestiona el dibuix via
 * {@link #paintComponent(Graphics)}, els esdeveniments de ratolí (a través de
 * {@link MyNewMouseAdapter}) i els de teclat. Delega tota la lògica al
 * {@link MyController}. El dibuix es fa directament sobre el panell (no sobre
 * el JFrame) per evitar el flickering.
 * <p>
 * [EN] Main application JPanel. Handles drawing via
 * {@link #paintComponent(Graphics)}, mouse events (through
 * {@link MyNewMouseAdapter}) and keyboard events. Delegates all logic to
 * {@link MyController}. Drawing is done directly on the panel (not on the
 * JFrame) to avoid flickering.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyNewPanel extends JPanel implements ActionListener, KeyListener {

    private MyController controller;
    private JButton b, b2;
    private Graphics2D pantalla;
    private Timer resizeTimer;
//    private Image buffer; // Imatge en memòria
//    private Graphics bufferGraphics; // Objecte Graphics per a la imatge en memòria

    /**
     * [CA] Crea el panell, registra els listeners de ratolí, teclat i
     * redimensionament. El timer de redimensionament evita redibuixos
     * excessius durant l'estirament de la finestra.
     * <p>
     * [EN] Creates the panel, registers mouse, keyboard and resize listeners.
     * The resize timer avoids excessive redraws while the window is being
     * resized.
     *
     * @param contr [CA] controlador principal / [EN] main controller
     */
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

        resizeTimer = new Timer(150, evt -> {
            resizeTimer.stop();
            MyNewPanel.this.controller.onScreenResized();
        });
        resizeTimer.setRepeats(false);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = getWidth();
                int h = getHeight();
                if (w > 0 && h > 0) {
                    Settings.setScreenPixelDimensions(w, h);
                    MyNewPanel.this.controller.onScreenResizedQuick();
                }
                resizeTimer.restart();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                MyNewPanel.this.controller.setNeedsDrawing(true);
            }
        });

    }

    /**
     * [CA] Retorna la mida preferida del panell basada en les dimensions de
     * pantalla configurades a {@link Settings}.
     * <p>
     * [EN] Returns the preferred panel size based on the screen dimensions
     * configured in {@link Settings}.
     *
     * @return [CA] mida preferida / [EN] preferred size
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int)Settings.getScreenWidth(),(int)Settings.getScreenHeight());
    }

    /**
     * [CA] Retorna el context gràfic de pantalla (Graphics2D) del darrer
     * {@link #paintComponent(Graphics)}.
     * <p>
     * [EN] Returns the screen graphics context (Graphics2D) from the last
     * {@link #paintComponent(Graphics)} call.
     *
     * @return [CA] context gràfic de pantalla / [EN] screen graphics context
     */
    public Graphics2D getPantalla() {
        return pantalla;
    }

    /**
     * [CA] Requereix el focus del teclat quan el panell s'afegeix a la jerarquia.
     * <p>
     * [EN] Requests keyboard focus when the panel is added to the hierarchy.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        requestFocus();
    }

    private static int countPaintComponent = 1;
    private static boolean repintaWasCalled = false;

    /**
     * [CA] Indica si {@link #repinta(boolean)} ha estat cridada des de
     * l'últim {@link #paintComponent(Graphics)}.
     * <p>
     * [EN] Indicates whether {@link #repinta(boolean)} was called since the
     * last {@link #paintComponent(Graphics)}.
     *
     * @return [CA] true si repinta ha estat cridada / [EN] true if repinta was called
     */
    public static boolean isRepintaWasCalled() {
        return repintaWasCalled;
    }

    /**
     * [CA] Estableix el flag que indica si {@link #repinta(boolean)} ha estat
     * cridada.
     * <p>
     * [EN] Sets the flag indicating whether {@link #repinta(boolean)} was
     * called.
     *
     * @param repintaWasCalled [CA] nou valor del flag / [EN] new flag value
     */
    public static void setRepintaWasCalled(boolean repintaWasCalled) {
        MyNewPanel.repintaWasCalled = repintaWasCalled;
    }

    /**
     * [CA] Mètode principal de dibuix. S'invoca automàticament en cada canvi
     * de finestra i quan es crida {@link #repaint()}. No s'ha de cridar
     * explícitament. Delega el dibuix al controlador via
     * {@link MyController#redraw(Graphics2D)}.
     * <p>
     * [EN] Main paint method. Invoked automatically at each window change and
     * when {@link #repaint()} is called. Should not be called explicitly.
     * Delegates drawing to the controller via
     * {@link MyController#redraw(Graphics2D)}.
     *
     * @param g [CA] context gràfic proporcionat per Swing /
     *          [EN] graphics context provided by Swing
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
     * [CA] Gestiona les accions dels botons de prova (demo). Invocada
     * automàticament per Swing en fer clic sobre un botó registrat.
     * <p>
     * [EN] Handles demo button actions. Invoked automatically by Swing when a
     * registered button is clicked.
     *
     * @param e [CA] event d'acció / [EN] action event
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

    /**
     * [CA] Gestiona els caràcters tipejats. En mode d'edició de lletra, els
     * remet a {@link dodecagraphone.model.component.MyLyrics}.
     * <p>
     * [EN] Handles typed characters. In lyrics edit mode, forwards them to
     * {@link dodecagraphone.model.component.MyLyrics}.
     *
     * @param e [CA] event de teclat / [EN] key event
     */
    @Override
    public void keyTyped(KeyEvent e) {
        if (controller.getMyLyrics().isEditMode()) {
            controller.getMyLyrics().handleKeyTyped(e);
            this.repinta(true);
        }
    }

    private static boolean needsRepaint = false;

    /**
     * [CA] Sol·licita un repintat del panell de forma segura des de qualsevol
     * fil. Evita encolaments redundants gràcies al flag {@code needsRepaint}.
     * <p>
     * [EN] Requests a panel repaint safely from any thread. Avoids redundant
     * queuing thanks to the {@code needsRepaint} flag.
     *
     * @param immediately [CA] si és true, usa {@code paintImmediately} (bloquejant);
     *                    si és false, usa {@code repaint} (asíncron) /
     *                    [EN] if true, uses {@code paintImmediately} (blocking);
     *                    if false, uses {@code repaint} (asynchronous)
     */
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

    /**
     * [CA] Gestiona les tecles premudes. Processa dreceres de teclat globals:
     * Enter (col·loca canvi pendent), Ctrl+Z/Shift+Z (undo/redo),
     * Ctrl+C/X/V/R (còpia, retalla, enganxa, replica), Ctrl+I/D
     * (insereix/elimina columna). En mode edició de lletra, envia els events
     * a {@link dodecagraphone.model.component.MyLyrics}.
     * <p>
     * [EN] Handles key press events. Processes global keyboard shortcuts:
     * Enter (place pending change), Ctrl+Z/Shift+Z (undo/redo),
     * Ctrl+C/X/V/R (copy, cut, paste, replicate), Ctrl+I/D
     * (insert/delete column). In lyrics edit mode, forwards events to
     * {@link dodecagraphone.model.component.MyLyrics}.
     *
     * @param e [CA] event de teclat / [EN] key event
     */
    @Override
    public void keyPressed(KeyEvent e) {
        // While in lyrics edit mode all key events are consumed by the editor
        if (controller.getMyLyrics().isEditMode()) {
            controller.getMyLyrics().handleKeyPressed(e);
            this.repinta(true);
            return;
        }
        controller.getButtons().hideTip();
        // Enter: col·loca el canvi pendent (scoreChange o column op) al playBar
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            controller.clearSelection();
            if (controller.isPendingColumnOp()) {
                controller.executePendingColumnOpAt(controller.getEditingColPublic());
            } else if (controller.placePendingChangeAtPlayBar()) {
                controller.getAllPurposeScore().drawCurrentCamInOffscreen();
                this.repinta(true);
            }
            return;
        }
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
            controller.clearSelection();
            if (controller.isPendingColumnOp()) {
                controller.cancelPendingColumnOp();
            } else if (controller.isPendingPaste()) {
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
        if (e.isControlDown() && !e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_I) {
            controller.clearSelection();
            controller.handleInsertColumn();
            return;
        }
        if (e.isControlDown() && !e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_D) {
            controller.clearSelection();
            controller.handleDeleteColumn();
            return;
        }
    }

    /**
     * [CA] Gestiona les tecles alliberades (no s'usa de moment).
     * <p>
     * [EN] Handles key release events (currently unused).
     *
     * @param e [CA] event de teclat / [EN] key event
     */
    @Override
    public void keyReleased(KeyEvent e) {
        // System.err.println("Key released."); //To change body of generated methods, choose Tools | Templates.
    }

}

/**
 * [CA] Adaptador de ratolí per al panell principal. Captura els events de
 * moviment, arrossegament, pressió, alliberament i doble clic, i els delega
 * al {@link MyController}. Después de cada acció sol·licita un repintat.
 * <p>
 * [EN] Mouse adapter for the main panel. Captures movement, drag, press,
 * release and double-click events and delegates them to {@link MyController}.
 * After each action requests a repaint.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
class MyNewMouseAdapter extends MouseAdapter {

    MyController controller;
    MyNewPanel panel;

    /**
     * [CA] Crea l'adaptador de ratolí associat al panell i al controlador.
     * <p>
     * [EN] Creates the mouse adapter associated with the panel and controller.
     *
     * @param panel [CA] panell principal / [EN] main panel
     * @param contr [CA] controlador principal / [EN] main controller
     */
    public MyNewMouseAdapter(MyNewPanel panel, MyController contr) {
        super();
        this.panel = panel;
        this.controller = contr;
    }

    /**
     * [CA] Invocada quan el ratolí es mou sense cap botó premut.
     * <p>
     * [EN] Invoked when the mouse moves without any button pressed.
     *
     * @param e [CA] event de ratolí / [EN] mouse event
     */
    @Override
    public void mouseMoved(MouseEvent e){
        double posX = e.getX();
        double posY = e.getY();
        controller.onMouseMoved(posX, posY);
        // this.panel.repinta(true);
    }

    /**
     * [CA] Invocada quan el ratolí s'arrossega amb un botó premut. Distingeix
     * entre botó esquerre i dret.
     * <p>
     * [EN] Invoked when the mouse is dragged with a button pressed.
     * Distinguishes between left and right button.
     *
     * @param e [CA] event de ratolí / [EN] mouse event
     */
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

    /**
     * [CA] Invocada quan es prem un botó del ratolí. Assegura el focus del
     * teclat al panell i delega al controlador.
     * <p>
     * [EN] Invoked when a mouse button is pressed. Ensures keyboard focus on
     * the panel and delegates to the controller.
     *
     * @param e [CA] event de ratolí / [EN] mouse event
     */
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

    /**
     * [CA] Invocada quan s'allibera un botó del ratolí. Delega al controlador
     * i reassigna el focus al panell.
     * <p>
     * [EN] Invoked when a mouse button is released. Delegates to the
     * controller and reassigns focus to the panel.
     *
     * @param e [CA] event de ratolí / [EN] mouse event
     */
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

    /**
     * [CA] Invocada quan es fa un doble clic amb el botó esquerre. Delega
     * l'event de doble clic al controlador.
     * <p>
     * [EN] Invoked when the left button is double-clicked. Delegates the
     * double-click event to the controller.
     *
     * @param e [CA] event de ratolí / [EN] mouse event
     */
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
